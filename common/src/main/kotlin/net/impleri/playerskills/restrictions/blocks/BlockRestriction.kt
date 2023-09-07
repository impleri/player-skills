package net.impleri.playerskills.restrictions.blocks

import net.impleri.playerskills.restrictions.AbstractRestriction
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Block

class BlockRestriction(
  target: Block,
  condition: (Player) -> Boolean,
  includeDimensions: List<ResourceLocation>? = null,
  excludeDimensions: List<ResourceLocation>? = null,
  includeBiomes: List<ResourceLocation>? = null,
  excludeBiomes: List<ResourceLocation>? = null,
  breakable: Boolean? = null,
  harvestable: Boolean? = null,
  usable: Boolean? = null,
  replacement: Block? = null,
) : AbstractRestriction<Block>(
  target,
  condition,
  includeDimensions ?: ArrayList(),
  excludeDimensions ?: ArrayList(),
  includeBiomes ?: ArrayList(),
  excludeBiomes ?: ArrayList(),
  replacement,
) {
  val breakable: Boolean = breakable ?: false
  val harvestable: Boolean = harvestable ?: false
  val usable: Boolean = usable ?: false
}
