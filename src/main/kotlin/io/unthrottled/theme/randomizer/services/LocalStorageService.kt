package io.unthrottled.theme.randomizer.services

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import io.unthrottled.theme.randomizer.tools.runSafely
import io.unthrottled.theme.randomizer.tools.toOptional
import java.io.IOException
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.Optional

object LocalStorageService {
  private val log = Logger.getInstance(this::class.java)
  private const val ASSET_DIRECTORY = "themeRandomizer"

  fun readLocalFile(assetUrl: URI): Optional<String> =
    Optional.ofNullable(Files.readAllBytes(Paths.get(assetUrl)))
      .map { String(it, Charsets.UTF_8) }

  fun constructLocalContentPath(
    assetCategory: AssetCategory,
    assetPath: String
  ): Path =
    Paths.get(
      getContentDirectory(),
      assetCategory.directory,
      assetPath
    ).normalize().toAbsolutePath()

  fun createDirectories(directoriesToCreate: Path) {
    try {
      Files.createDirectories(directoriesToCreate.parent)
    } catch (e: IOException) {
      log.error("Unable to create directories $directoriesToCreate for raisins", e)
    }
  }

  fun getContentDirectory(): String =
    getGlobalConfigAssetDirectory()
      .orElseGet {
        Paths.get(
          PathManager.getConfigPath(),
          ASSET_DIRECTORY
        ).toAbsolutePath().toString()
      }

  private fun getGlobalConfigAssetDirectory(): Optional<String> =
    Paths.get(
      PathManager.getConfigPath(),
      "..",
      ASSET_DIRECTORY
    ).toAbsolutePath()
      .normalize()
      .toOptional()
      .filter { Files.isWritable(it.parent) }
      .map {
        if (Files.exists(it).not()) {
          runSafely({
            Files.createDirectories(it)
          }) {
            log.warn("Unable to create global directory for raisins", it)
          }
        }
        it.toString()
      }
}
