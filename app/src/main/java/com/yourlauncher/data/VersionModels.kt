package com.yourlauncher.data

import com.google.gson.annotations.SerializedName

data class VersionManifest(
    val latest: Latest,
    val versions: List<Version>
)

data class Latest(
    val release: String,
    val snapshot: String
)

data class Version(
    val id: String,
    val type: String,
    val url: String,
    val time: String,
    val releaseTime: String
)

data class VersionDetails(
    val id: String,
    val name: String?,
    val mainClass: String?,
    val minimumLauncherVersion: Int,
    val releaseTime: String?,
    val time: String?,
    val type: String?,
    val assets: String?,
    val downloads: Downloads,
    val libraries: List<Library>,
    val logging: Logging?,
    val arguments: Arguments?
)

data class Downloads(
    val client: Download,
    val server: Download
)

data class Download(
    val sha1: String,
    val size: Long,
    val url: String
)

data class Library(
    val name: String,
    val downloads: LibraryDownloads,
    val rules: List<Rule>?,
    val natives: Map<String, String>?
)

data class LibraryDownloads(
    val artifact: LibraryArtifact?,
    val classifiers: Map<String, LibraryArtifact>?
)

data class LibraryArtifact(
    val path: String,
    val sha1: String,
    val size: Long,
    val url: String
)

data class Rule(
    val action: String,
    val os: Os?,
    val features: Map<String, Boolean>?
)

data class Os(
    val name: String,
    val version: String?,
    val arch: String?
)

data class Logging(
    val client: LoggingClient
)

data class LoggingClient(
    val argument: String,
    val file: Download,
    val type: String
)

data class Arguments(
    val game: List<Argument>?,
    val jvm: List<Argument>?
)

data class Argument(
    val value: String?,
    val rules: List<Rule>?,
    val __comment: String?
)
