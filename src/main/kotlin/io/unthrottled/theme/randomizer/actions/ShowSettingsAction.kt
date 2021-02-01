package io.unthrottled.theme.randomizer.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware
import io.unthrottled.theme.randomizer.ui.PluginSettingsUI

class ShowSettingsAction : AnAction(), DumbAware {

  override fun actionPerformed(e: AnActionEvent) {
    ShowSettingsUtil.getInstance()
      .showSettingsDialog(e.project, PluginSettingsUI::class.java)
  }
}
