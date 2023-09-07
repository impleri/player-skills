package net.impleri.playerskills.restrictions.fluids

import net.impleri.playerskills.api.FluidFiniteMode
import net.impleri.playerskills.restrictions.AbstractRestriction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.material.Fluid

class FluidRestriction(
  target: Fluid,
  condition: (Player) -> Boolean,
  includeDimensions: List<ResourceLocation>? = null,
  excludeDimensions: List<ResourceLocation>? = null,
  includeBiomes: List<ResourceLocation>? = null,
  excludeBiomes: List<ResourceLocation>? = null,
  bucketable: Boolean? = null,
  producible: Boolean? = null,
  consumable: Boolean? = null,
  identifiable: Boolean? = null,
  finiteMode: FluidFiniteMode? = null,
  replacement: Fluid? = null,
) : AbstractRestriction<Fluid>(
  target,
  condition,
  includeDimensions ?: ArrayList(),
  excludeDimensions ?: ArrayList(),
  includeBiomes ?: ArrayList(),
  excludeBiomes ?: ArrayList(),
  replacement,
) {
  val bucketable: Boolean = bucketable ?: false
  val producible: Boolean = producible ?: false
  val consumable: Boolean = consumable ?: false
  val identifiable: Boolean = identifiable ?: false
  val finiteMode: FluidFiniteMode = finiteMode ?: FluidFiniteMode.DEFAULT
}
