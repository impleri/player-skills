package net.impleri.playerskills.restrictions.blocks

import net.impleri.playerskills.api.BlockRestrictions
import net.impleri.playerskills.restrictions.AbstractRestrictionBuilder
import net.impleri.playerskills.restrictions.RestrictionConditionsBuilder
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block

class BlockRestrictionBuilder : AbstractRestrictionBuilder<Block, BlockRestriction>(
  Registry.BLOCK,
  PlayerSkillsLogger.BLOCKS,
) {
  override fun <Player> restrictOne(targetName: ResourceLocation, builder: RestrictionConditionsBuilder<Block, Player, BlockRestriction>) {
    builder as BlockConditions<Player>

    val block = BlockRestrictions.getValue(targetName)

    if (BlockRestrictions.isEmptyBlock(block)) {
      logger.warn("Could not find any block named $targetName")
      return
    }

    val restriction = BlockRestriction(
      block,
      builder.actualCondition,
      builder.includeDimensions,
      builder.excludeDimensions,
      builder.includeBiomes,
      builder.excludeBiomes,
      builder.breakable,
      builder.harvestable,
      builder.usable,
      builder.replacement,
    )

    BlockRestrictions.add(targetName, restriction)
    logRestriction(targetName, restriction)
  }

  override fun getName(target: Block): ResourceLocation {
    return BlockRestrictions.getName(target)
  }

  override fun isTagged(target: Block, tag: TagKey<Block>): Boolean {
    return target.defaultBlockState().`is`(tag)
  }

  companion object {
    private val instance = BlockRestrictionBuilder()

    fun <Player> register(name: String, builder: BlockConditions<Player>) {
      instance.create(name, builder)
    }

    fun register() {
      instance.register()
    }
  }
}
