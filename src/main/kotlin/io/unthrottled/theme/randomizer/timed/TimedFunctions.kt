package io.unthrottled.theme.randomizer.timed

import io.unthrottled.theme.randomizer.config.Config
import io.unthrottled.theme.randomizer.mode.PluginMode
import io.unthrottled.theme.randomizer.mode.toPluginMode

fun isTimedOSMatch(): Boolean =
  Config.instance.pluginMode.toPluginMode() == PluginMode.TIMED && Config.instance.isTimedMatchOS
