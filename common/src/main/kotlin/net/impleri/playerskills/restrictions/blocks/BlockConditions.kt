package net.impleri.playerskills.restrictions.blocks

import net.impleri.playerskills.api.BlockRestrictions
import net.impleri.playerskills.restrictions.RestrictionConditionsBuilder
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.impleri.playerskills.utils.SkillResourceLocation.of
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

interface BlockConditions<Player> : RestrictionConditionsBuilder<Block, Player, BlockRestriction> {
  var replacement: Block?
  var breakable: Boolean?
  var harvestable: Boolean?
  var usable: Boolean?

  fun replaceWith(target: String?): BlockConditions<Player> {
    target?.let {
      replacement = BlockRestrictions.getValue(of(it))
    } ?: PlayerSkillsLogger.BLOCKS.warn("Could not find any block named $name")

    return this
  }

  fun replaceWithAir(): BlockConditions<Player> {
    replacement = Blocks.AIR
    return this
  }

  fun breakable(): BlockConditions<Player> {
    breakable = true
    return this
  }

  fun unbreakable(): BlockConditions<Player> {
    breakable = false
    return this
  }

  fun harvestable(): BlockConditions<Player> {
    harvestable = true
    return this
  }

  fun unharvestable(): BlockConditions<Player> {
    harvestable = false
    return this
  }

  fun usable(): BlockConditions<Player> {
    usable = true
    return this
  }

  fun unusable(): BlockConditions<Player> {
    usable = false
    return this
  }

  fun nothing(): BlockConditions<Player> {
    breakable = true
    harvestable = true
    usable = true
    return this
  }

  fun everything(): BlockConditions<Player> {
    breakable = false
    harvestable = false
    usable = false
    return this
  }
}
