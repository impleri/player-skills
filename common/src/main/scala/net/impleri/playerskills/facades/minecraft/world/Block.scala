package net.impleri.playerskills.facades.minecraft.world

import net.impleri.playerskills.facades.minecraft.core.Registry
import net.impleri.playerskills.facades.minecraft.HasName
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.minecraft.world.level.block.{Block => McBlock}
import net.minecraft.world.level.block.state.BlockState

case class Block(private val blockState: BlockState, registry: Registry[McBlock] = Registry.Blocks) extends HasName {
  def name: String = getName.fold("unknown block")(_.toString)

  override def getName: Option[ResourceLocation] = registry.getKey(blockState.getBlock)
}

object Block {
  def apply(block: McBlock): Block = new Block(block.defaultBlockState())
}
