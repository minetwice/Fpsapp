package com.yourlauncher.auth

import android.content.Context
import com.microsoft.identity.client.*;
import com.microsoft.identity.client.exception.MsalException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthManager(private val context: Context) {
    private lateinit var publicClientApplication: PublicClientApplication
    private val scopes = listOf("XboxLive.signin", "offline_access")

    init {
        try {
            publicClientApplication = PublicClientApplication.create(
                context,
                R.raw.msal_config
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun loginMicrosoft(): AuthenticationResult? = suspendCancellableCoroutine { continuation ->
        val parameters = AcquireTokenParameters.Builder()
            .startAuthorizationFromActivity(null) // Set your activity
            .withScopes(scopes)
            .withCallback(object : AuthenticationCallback {
                override fun onSuccess(authenticationResult: AuthenticationResult) {
                    continuation.resume(authenticationResult)
                }
                override fun onError(exception: MsalException) {
                    continuation.resumeWithException(exception)
                }
                override fun onCancel() {
                    continuation.resume(null)
                }
            }).build()
        publicClientApplication.acquireToken(parameters)
    }

    suspend fun refreshToken(refreshToken: String): AuthenticationResult? {
        // Implement refresh logic
        return null
    }

    fun logout() {
        publicClientApplication.removeAccounts { /* handle result */ }
    }
}
