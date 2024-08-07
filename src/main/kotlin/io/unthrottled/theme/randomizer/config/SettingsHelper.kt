package io.unthrottled.theme.randomizer.config

object SettingsHelper {
  @JvmStatic
  fun getDisplayMapping(changeIntervals: ChangeIntervals): String =
    when (changeIntervals) {
      ChangeIntervals.MINUTE          -> "Every Minute"
      ChangeIntervals.THIRTY_MINUTES  -> "Every 30 Minutes"
      ChangeIntervals.HOUR            -> "Every Hour"
      ChangeIntervals.DAY             -> /*smoke weed*/ "Every day"
      ChangeIntervals.TWO_DAYS        -> "Every other day"
      ChangeIntervals.WEEK            -> "Every 7 days"
      ChangeIntervals.FIVE_MINUTES    -> "Every 5 minutes"
      ChangeIntervals.TEN_MINUTES     -> "Every 10 minutes"
      ChangeIntervals.FIFTEEN_MINUTES -> "Every 15 minutes"
    }
}

data class IntervalTuple(
  val interval: ChangeIntervals,
  val displayValue: String
) {
  override fun toString(): String = displayValue
}
