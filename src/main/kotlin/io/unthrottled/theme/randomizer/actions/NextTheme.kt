package io.unthrottled.theme.randomizer.actions

import com.intellij.ide.actions.QuickChangeLookAndFeel
import com.intellij.ide.ui.LafManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import io.unthrottled.theme.randomizer.config.ui.isDark
import io.unthrottled.theme.randomizer.system.match.SystemMatchManager
import io.unthrottled.theme.randomizer.themes.SelectableThemeType
import io.unthrottled.theme.randomizer.themes.ThemeService

class NextTheme : AnAction() {

  override fun actionPerformed(e: AnActionEvent) {
    val isSystemMatch = SystemMatchManager.isSystemMatch()
    val isCurrentThemeDark = LafManager.getInstance().currentLookAndFeel.isDark()
    val themeSelection = if (isSystemMatch && isCurrentThemeDark) {
      SelectableThemeType.DARK
    } else if (isSystemMatch) {
      SelectableThemeType.LIGHT
    } else {
      SelectableThemeType.ANY
    }

    ThemeService.instance.nextTheme(themeSelection)
      .ifPresent {
        QuickChangeLookAndFeel.switchLafAndUpdateUI(
          LafManager.getInstance(),
          it,
          true
        )
      }
  }
}
