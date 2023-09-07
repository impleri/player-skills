package net.impleri.playerskills.api

class TeamMode private constructor(private val type: TeamModeType, val rate: Double? = null) {
  val isOff: Boolean
    get() = type == TeamModeType.OFF
  val isShared: Boolean
    get() = type == TeamModeType.SHARED
  val isSplitEvenly: Boolean
    get() = type == TeamModeType.SPLIT_EVENLY
  val isPyramid: Boolean
    get() = type == TeamModeType.PYRAMID
  val isProportional: Boolean
    get() = type == TeamModeType.PROPORTIONAL
  val isLimited: Boolean
    get() = type == TeamModeType.LIMITED

  override fun toString(): String {
    return type.name
  }

  companion object {
    fun off(): TeamMode {
      return TeamMode(TeamModeType.OFF)
    }

    fun shared(): TeamMode {
      return TeamMode(TeamModeType.SHARED)
    }

    fun splitEvenly(): TeamMode {
      return TeamMode(TeamModeType.SPLIT_EVENLY)
    }

    fun pyramid(): TeamMode {
      return TeamMode(TeamModeType.PYRAMID)
    }

    fun proportional(percentage: Double): TeamMode {
      return TeamMode(TeamModeType.PROPORTIONAL, percentage)
    }

    fun limited(amount: Double): TeamMode {
      return TeamMode(TeamModeType.LIMITED, amount)
    }
  }
}
