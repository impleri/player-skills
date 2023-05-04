package net.impleri.playerskills.restrictions

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player

abstract class AbstractRestriction<Target>(
  val target: Target,
  val condition: (Player) -> Boolean = DEFAULT_CONDITION,
  val includeDimensions: List<ResourceLocation> = ArrayList(),
  val excludeDimensions: List<ResourceLocation> = ArrayList(),
  val includeBiomes: List<ResourceLocation> = ArrayList(),
  val excludeBiomes: List<ResourceLocation> = ArrayList(),
  val replacement: Target?,
) {
  companion object {
    private val DEFAULT_CONDITION = { _: Player -> true }
  }
}
