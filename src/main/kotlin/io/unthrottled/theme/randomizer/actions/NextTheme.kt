@file:Suppress("UnstableApiUsage")

package io.unthrottled.theme.randomizer.actions

import com.intellij.ide.actions.QuickChangeLookAndFeel
import com.intellij.ide.ui.LafManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import io.unthrottled.theme.randomizer.mode.getCurrentSelectableThemeType
import io.unthrottled.theme.randomizer.themes.ThemeService

class NextTheme : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val themeSelection = getCurrentSelectableThemeType()

    ThemeService.instance.nextTheme(themeSelection)
      ?.ifPresent {
        QuickChangeLookAndFeel.switchLafAndUpdateUI(
          LafManager.getInstance(),
          it,
          true
        )
      }
  }
}
