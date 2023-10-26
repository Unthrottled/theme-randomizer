package io.unthrottled.theme.randomizer.config

import com.intellij.util.messages.Topic
import java.util.*

fun interface ConfigListener : EventListener {
  companion object {
    val CONFIG_TOPIC: Topic<ConfigListener> = Topic(ConfigListener::class.java)
  }

  fun pluginConfigUpdated(
    config: Config,
    previousConfig: ConfigSettingsModel
  )
}
