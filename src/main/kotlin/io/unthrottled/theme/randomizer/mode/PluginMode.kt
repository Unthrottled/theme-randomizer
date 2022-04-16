package io.unthrottled.theme.randomizer.mode

enum class PluginMode(val displayName: String) {
  TIMED("Timed"), SYSTEM_MATCH("Match OS");

  override fun toString(): String {
    return displayName
  }

  companion object {

    private val nameToPluginMode = values().associateBy { it.displayName }
    fun valueFrom(pluginMode: String): PluginMode =
      nameToPluginMode[pluginMode] ?: TIMED
  }
}

fun String.toPluginMode(): PluginMode = PluginMode.valueFrom(this)
