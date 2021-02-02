package io.unthrottled.theme.randomizer.tools

import java.util.Optional
import kotlin.random.Random

class ProbabilityTools(
  private val random: Random
) {
  fun <T> pickFromWeightedList(weightedList: List<Pair<T, Long>>): Optional<T> {
    val totalWeight = weightedList.map { it.second }.sum()
    return pickFromWeightedList(
      random.nextLong(1, if (totalWeight <= 1) 2 else totalWeight),
      weightedList
    )
  }

  private fun <T> pickFromWeightedList(
    weightChosen: Long,
    weightedEmotions: List<Pair<T, Long>>
  ): Optional<T> {
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
