package io.unthrottled.theme.randomizer.mode

import io.unthrottled.theme.randomizer.system.match.SystemMatchManager
import io.unthrottled.theme.randomizer.themes.SelectableThemeType
import io.unthrottled.theme.randomizer.timed.isTimedOSMatch

fun getCurrentSelectableThemeType(): SelectableThemeType {
  val isSystemMatch = SystemMatchManager.isSystemMatch() || isTimedOSMatch()
  val themeSelection = when {
    isSystemMatch && SystemMatchManager.isDark -> SelectableThemeType.DARK
    isSystemMatch -> SelectableThemeType.LIGHT
    else -> SelectableThemeType.ANY
  }
  return themeSelection
}
