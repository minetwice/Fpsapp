package com.yourmod.render;

import com.yourmod.PerformanceMonitor;
import com.yourmod.VulkanBridge;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkBufferCreateInfo;
import org.lwjgl.vulkan.VkCommandBufferAllocateInfo;
import org.lwjgl.vulkan.VkCommandBufferBeginInfo;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import net.minecraft.util.math.MathHelper;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.vulkan.VK10.*;

public class GPUDrivenRenderer {
    private static boolean initialized = false;
    private static long indirectBuffer = NULL;
    private static long commandPool = NULL;
    private static long commandBuffer = NULL;
    private static int maxDrawCalls = 2048;
    private static int currentDrawCount = 0;
    private static float[][] drawCommands = new float[maxDrawCalls][8];
    private static List<Integer> drawIndices = new ArrayList<>();

    public static void init() {
        if (initialized) return;
        createIndirectBuffer();
        createCommandBuffer();
        initialized = true;
        VulkanBridge.enableMultiThreading(true);
        VulkanBridge.enableDynamicResolution(true);
    }

    private static void createIndirectBuffer() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pBuffer = stack.mallocInt(1);
            VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO)
                    .size(maxDrawCalls * 32L)
                    .usage(VK_BUFFER_USAGE_INDIRECT_BUFFER_BIT | VK_BUFFER_USAGE_TRANSFER_DST_BIT);
            if (vkCreateBuffer(VulkanBridge.getDevice(), bufferInfo, null, pBuffer) != VK_SUCCESS) {
                System.err.println("Failed to create indirect buffer");
                return;
            }
            indirectBuffer = pBuffer.get(0);
        }
    }

    private static void createCommandBuffer() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pPool = stack.mallocInt(1);
            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO)
                    .flags(VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT)
                    .queueFamilyIndex(0);
            if (vkCreateCommandPool(VulkanBridge.getDevice(), poolInfo, null, pPool) != VK_SUCCESS) {
                System.err.println("Failed to create command pool");
                return;
            }
            commandPool = pPool.get(0);
            IntBuffer pCmd = stack.mallocInt(1);
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO)
                    .commandPool(commandPool)
                    .level(VK_COMMAND_BUFFER_LEVEL_PRIMARY)
                    .commandBufferCount(1);
            if (vkAllocateCommandBuffers(VulkanBridge.getDevice(), allocInfo, pCmd) != VK_SUCCESS) {
                System.err.println("Failed to allocate command buffer");
                return;
            }
            commandBuffer = pCmd.get(0);
        }
    }

    public static void beginFrame() {
        currentDrawCount = 0;
        drawIndices.clear();
        if (commandBuffer != NULL) {
            vkResetCommandBuffer(commandBuffer, 0);
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc()
                    .sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO)
                    .flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
            vkBeginCommandBuffer(commandBuffer, beginInfo);
            beginInfo.free();
        }
    }

    public static void submitDraw(Vec3d center, float radius, int indexCount, int instanceCount, int firstIndex, int vertexOffset) {
        if (currentDrawCount >= maxDrawCalls) return;
        float[] cmd = drawCommands[currentDrawCount];
        cmd[0] = (float) center.x;
        cmd[1] = (float) center.y;
        cmd[2] = (float) center.z;
        cmd[3] = radius;
        cmd[4] = indexCount;
        cmd[5] = instanceCount;
        cmd[6] = firstIndex;
        cmd[7] = vertexOffset;
        drawIndices.add(currentDrawCount);
        currentDrawCount++;
    }

    public static void endFrame() {
        if (commandBuffer == NULL) return;
        if (currentDrawCount == 0) {
            vkEndCommandBuffer(commandBuffer);
            return;
        }
        
        // GPU frustum culling check against camera position
        MinecraftClient client = MinecraftClient.getInstance();
        Vec3d cameraPos = client.player.getPos();
        
        for (int idx : drawIndices) {
            float[] cmd = drawCommands[idx];
            float dx = cmd[0] - (float) cameraPos.x;
            float dy = cmd[1] - (float) cameraPos.y;
            float dz = cmd[2] - (float) cameraPos.z;
            float distSq = dx*dx + dy*dy + dz*dz;
            if (distSq > cmd[3] * cmd[3]) {
                cmd[5] = 0;
            } else {
                cmd[5] = 1;
            }
        }
        
        vkEndCommandBuffer(commandBuffer);
        
        // Update indirect buffer and execute indirect draw
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long mapped = nmemAlloc(currentDrawCount * 32L);
            for (int i = 0; i < currentDrawCount; i++) {
                float[] cmd = drawCommands[i];
                memPutFloat(mapped + i*32, cmd[0]);
                memPutFloat(mapped + i*32 + 4, cmd[1]);
                memPutFloat(mapped + i*32 + 8, cmd[2]);
                memPutFloat(mapped + i*32 + 12, cmd[3]);
                memPutFloat(mapped + i*32 + 16, cmd[4]);
                memPutFloat(mapped + i*32 + 20, cmd[5]);
                memPutFloat(mapped + i*32 + 24, cmd[6]);
                memPutFloat(mapped + i*32 + 28, cmd[7]);
            }
            vkCmdDrawIndirect(commandBuffer, indirectBuffer, 0, currentDrawCount, 32);
            nmemFree(mapped);
        }
    }

    public static void cleanup() {
        if (commandBuffer != NULL) {
            vkFreeCommandBuffers(VulkanBridge.getDevice(), commandPool, 1, commandBuffer);
            commandBuffer = NULL;
        }
        if (commandPool != NULL) {
            vkDestroyCommandPool(VulkanBridge.getDevice(), commandPool, null);
            commandPool = NULL;
        }
        if (indirectBuffer != NULL) {
            vkDestroyBuffer(VulkanBridge.getDevice(), indirectBuffer, null);
            indirectBuffer = NULL;
        }
        initialized = false;
    }

    public static boolean isInitialized() { return initialized; }
}
