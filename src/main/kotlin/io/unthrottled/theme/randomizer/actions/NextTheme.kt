package io.unthrottled.theme.randomizer.actions

import com.intellij.ide.actions.QuickChangeLookAndFeel
import com.intellij.ide.ui.LafManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import io.unthrottled.theme.randomizer.services.ThemeService

class NextTheme : AnAction() {

  override fun actionPerformed(e: AnActionEvent) {
    ThemeService.instance.nextTheme()
      .ifPresent {
        QuickChangeLookAndFeel.switchLafAndUpdateUI(
          LafManager.getInstance(),
          it,
          true
        )
      }
  }
}
