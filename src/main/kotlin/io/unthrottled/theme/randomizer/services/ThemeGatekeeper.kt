package io.unthrottled.theme.randomizer.services

import com.intellij.ide.ui.laf.UIThemeBasedLookAndFeelInfo
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import io.unthrottled.theme.randomizer.config.Config
import io.unthrottled.theme.randomizer.config.ConfigListener
import io.unthrottled.theme.randomizer.config.ConfigListener.Companion.CONFIG_TOPIC
import javax.swing.UIManager

class ThemeGatekeeper : Disposable {
  companion object {
    val instance: ThemeGatekeeper
      get() = ServiceManager.getService(ThemeGatekeeper::class.java)
  }

  private val connection = ApplicationManager.getApplication().messageBus.connect()

  private var preferredCharactersIds: Set<String> =
    extractAllowedCharactersFromState(Config.instance.selectedThemes)

  private fun extractAllowedCharactersFromState(characterConfig: String): Set<String> =
    characterConfig.split(Config.DEFAULT_DELIMITER)
      .filter { it.isNotEmpty() }
      .map { it.toLowerCase() }
      .toSet()

  init {
    connection.subscribe(
      CONFIG_TOPIC,
      ConfigListener { newPluginState ->
        preferredCharactersIds = extractAllowedCharactersFromState(Config.instance.selectedThemes)
      }
    )
  }

  fun hasPreferredCharacter(characters: List<UIManager.LookAndFeelInfo>): Boolean =
    preferredCharactersIds.isEmpty() ||
      characters.any { isPreferred(it) }

  fun isPreferred(character: UIManager.LookAndFeelInfo): Boolean =
    preferredCharactersIds.contains(
      when (character) {
        is UIThemeBasedLookAndFeelInfo -> character.theme.id
        else -> character.name
      }
    )

  override fun dispose() {
    connection.dispose()
  }
}
