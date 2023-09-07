package net.impleri.playerskills.integrations.kubejs.blocks

import net.impleri.playerskills.integrations.kubejs.api.AbstractRestrictionConditionsBuilder
import net.impleri.playerskills.integrations.kubejs.api.PlayerDataJS
import net.impleri.playerskills.restrictions.blocks.BlockConditions
import net.impleri.playerskills.restrictions.blocks.BlockRestriction
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.block.Block

class BlockConditionsBuilderJS(
  server: Lazy<MinecraftServer>,
) : AbstractRestrictionConditionsBuilder<Block, BlockRestriction>(server), BlockConditions<PlayerDataJS> {
  override var replacement: Block? = null
  override var breakable: Boolean? = true
  override var harvestable: Boolean? = true
  override var usable: Boolean? = true
}
