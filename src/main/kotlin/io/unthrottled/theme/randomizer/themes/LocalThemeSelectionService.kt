package io.unthrottled.theme.randomizer.themes

import io.unthrottled.theme.randomizer.config.Config

object LocalThemeSelectionService : LocalPersistenceService<ThemeSelections>(
  "theme-selections.json",
  ThemeSelections::class.java,
) {
  override fun decorateItem(item: ThemeSelections): ThemeSelections = item

  override fun buildDefaultLedger() =
    ThemeSelectionExtractor.extractThemeSelectionsFromConfig(Config.instance)

  override fun combineWithOnDisk(themeObservationLedger: ThemeSelections): ThemeSelections {
    return themeObservationLedger
  }

  fun saveSelections(config: Config) {
    persistLedger(ThemeSelectionExtractor.extractThemeSelectionsFromConfig(config))
  }
}
