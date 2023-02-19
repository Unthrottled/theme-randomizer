package io.unthrottled.theme.randomizer.themes

import io.unthrottled.theme.randomizer.config.Config

data class ThemeSelections(
  val preferredThemeIdSet: Set<String>,
  val blacklistedThemeIdSet: Set<String>
)

object ThemeSelectionExtractor {

  fun extractThemeSelectionsFromConfig(config: Config): ThemeSelections {
    return ThemeSelections(
      preferredThemeIdSet = extractAllowedCharactersFromState(config.selectedThemes),
      blacklistedThemeIdSet = extractAllowedCharactersFromState(config.blacklistedThemes)
    )
  }

  private fun extractAllowedCharactersFromState(characterConfig: String): Set<String> =
    characterConfig.split(Config.DEFAULT_DELIMITER)
      .filter { it.isNotEmpty() }
      .toSet()
}
