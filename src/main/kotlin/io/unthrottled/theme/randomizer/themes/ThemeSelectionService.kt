package io.unthrottled.theme.randomizer.themes

import com.intellij.openapi.application.ApplicationManager
import io.unthrottled.theme.randomizer.config.Config

class ThemeSelectionService {
  companion object {
    val instance: ThemeSelectionService
      get() = ApplicationManager.getApplication().getService(ThemeSelectionService::class.java)
  }

  private var _preferredThemeIds: Set<String>

  private var _blackListedThemeIds: Set<String>

  init {
    val currentThemeSelections = getThemeSelectionsFromConfigs()
    _preferredThemeIds = currentThemeSelections.preferredThemeIdSet
    _blackListedThemeIds = currentThemeSelections.blacklistedThemeIdSet
  }

  private fun getThemeSelectionsFromConfigs() = ThemeSelectionExtractor.extractThemeSelectionsFromConfig(Config.instance)

  fun rehydrateSelectionsFromConfig(config: Config) {
    val currentThemeSelections = ThemeSelectionExtractor.extractThemeSelectionsFromConfig(config)
    rehydrateFromSelections(currentThemeSelections)
  }

  private fun rehydrateFromSelections(currentThemeSelections: ThemeSelections) {
    _preferredThemeIds = currentThemeSelections.preferredThemeIdSet
    _blackListedThemeIds = currentThemeSelections.blacklistedThemeIdSet
  }

  fun reHydrateIfNecessary(isLocalSync: Boolean = Config.instance.isLocalSync) {
    if (isLocalSync) {
      rehydrateFromSelections(LocalThemeSelectionService.getThemeSelections())
    } else {
      rehydrateFromSelections(getThemeSelectionsFromConfigs())
    }
  }

  val preferredThemeIds: Set<String>
    get() {
      return _preferredThemeIds
    }

  val blackListedThemeIds: Set<String>
    get() {
      return _blackListedThemeIds
    }
}
