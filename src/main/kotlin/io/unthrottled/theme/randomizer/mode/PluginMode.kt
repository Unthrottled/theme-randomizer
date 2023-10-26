package io.unthrottled.theme.randomizer.mode

enum class PluginMode(val displayName: String) {
  TIMED("Timed"),
  SYSTEM_MATCH("Match OS");

  override fun toString(): String = displayName

  companion object {
    private val nameToPluginMode = entries.associateBy { it.displayName }
    fun valueFrom(pluginMode: String): PluginMode = nameToPluginMode[pluginMode] ?: TIMED
  }
}

fun String.toPluginMode(): PluginMode = PluginMode.valueFrom(this)
