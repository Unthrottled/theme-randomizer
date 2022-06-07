package io.unthrottled.theme.randomizer.themes

import com.google.gson.GsonBuilder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.io.exists
import io.unthrottled.theme.randomizer.config.Config
import io.unthrottled.theme.randomizer.services.AssetCategory
import io.unthrottled.theme.randomizer.services.LocalStorageService
import io.unthrottled.theme.randomizer.services.LocalStorageService.constructLocalContentPath
import io.unthrottled.theme.randomizer.tools.runSafelyWithResult
import io.unthrottled.theme.randomizer.tools.toOptional
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardOpenOption

object LocalThemeSelectionService {
  private val log = Logger.getInstance(LocalThemeSelectionService::class.java)

  private val gson = GsonBuilder()
    .create()

  private val ledgerPath = constructLocalContentPath(
    AssetCategory.META,
    "theme-selections.json"
  )

  fun getThemeSelections(): ThemeSelections =
    if (ledgerPath.exists()) {
      readLedger()
    } else {
      buildDefaultLedger()
    }

  private fun readLedger(): ThemeSelections =
    runSafelyWithResult({
      Files.newInputStream(ledgerPath)
        .use {
          gson.fromJson(
            InputStreamReader(it, StandardCharsets.UTF_8),
            ThemeSelections::class.java
          )
        }
    }) {
      log.warn("Unable to read promotion ledger for raisins.", it)
      buildDefaultLedger()
    }.toOptional()
      .orElseGet {
        buildDefaultLedger()
      }

  private fun buildDefaultLedger() = ThemeSelectionExtractor.extractThemeSelectionsFromConfig(Config.instance)

  fun persistLedger(themeObservationLedger: ThemeSelections): ThemeSelections {
    if (ledgerPath.exists().not()) {
      LocalStorageService.createDirectories(ledgerPath)
    }

    return runSafelyWithResult({
      Files.newBufferedWriter(
        ledgerPath,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING
      ).use {
        val mostCurrentLedger = combineWithOnDisk(themeObservationLedger)
        it.write(
          gson.toJson(mostCurrentLedger)
        )
        mostCurrentLedger
      }
    }) {
      log.warn("Unable to persist ledger for raisins", it)
      themeObservationLedger
    }
  }

  private fun combineWithOnDisk(themeObservationLedger: ThemeSelections): ThemeSelections {
    return themeObservationLedger
  }

  fun saveSelections(config: Config) {
    persistLedger(ThemeSelectionExtractor.extractThemeSelectionsFromConfig(config))
  }
}
