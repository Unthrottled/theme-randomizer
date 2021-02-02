package io.unthrottled.theme.randomizer.listeners

import com.intellij.util.messages.Topic
import javax.swing.UIManager

fun interface ThemeChangedListener {
  companion object {
    val TOPIC = Topic.create("Theme Changed", ThemeChangedListener::class.java)
  }

  fun onChanged(lookAndFeelInfo: UIManager.LookAndFeelInfo)
}
