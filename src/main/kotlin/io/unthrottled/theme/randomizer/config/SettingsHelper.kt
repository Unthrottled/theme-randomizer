package io.unthrottled.theme.randomizer.config

object SettingsHelper {

  @JvmStatic
  fun getDisplayMapping(changeIntervals: ChangeIntervals): String =
    when (changeIntervals) {
      ChangeIntervals.DAY -> /*smoke weed*/ "Every day"
      ChangeIntervals.HOUR -> "Every Hour"
      ChangeIntervals.THIRTY_MINUTES -> "Every 30 Minutes"
      ChangeIntervals.FIFTEEN_MINUTES -> "Every 15 Minutes"
      ChangeIntervals.FIVE_MINUTES -> "Every 5 Minutes"
    }
}

data class IntervalTuple(
  val interval: ChangeIntervals,
  val displayValue: String,
) {
  override fun toString(): String = displayValue
}
