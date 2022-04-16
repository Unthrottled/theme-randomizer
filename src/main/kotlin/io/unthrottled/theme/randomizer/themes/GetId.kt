package io.unthrottled.theme.randomizer.themes

import com.intellij.ide.ui.laf.UIThemeBasedLookAndFeelInfo
import javax.swing.UIManager

fun UIManager.LookAndFeelInfo.getId(): String =
  when (this) {
    is UIThemeBasedLookAndFeelInfo -> this.theme.id
    else -> this.name
  }
