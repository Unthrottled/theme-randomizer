package io.unthrottled.theme.randomizer.tools

import java.util.*
import kotlin.random.Random

class ProbabilityTools(
  private val random: Random
) {
  fun <T> pickFromWeightedList(weightedList: List<Pair<T, Long>>): Optional<out T>? {
    val totalWeight = weightedList.sumOf { it.second }
    return pickFromWeightedList(
      random.nextLong(1, if (totalWeight <= 1) 2 else totalWeight),
      weightedList
    )
  }

  private fun <T> pickFromWeightedList(
    weightChosen: Long,
    weightedEmotions: List<Pair<T, Long>>
  ): Optional<out T>? {
    var randomWeight = weightChosen
    for ((mood, weight) in weightedEmotions) {
      if (randomWeight <= weight) {
        return mood.toOptional()
      }
      randomWeight -= weight
    }

    return weightedEmotions.first { it.second > 0 }.toOptional()
      .map { it.first }
  }
}
