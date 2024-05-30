package net.impleri.slab.block

import net.impleri.slab.registry.HasName
import net.impleri.slab.registry.IsRegistered
import net.impleri.slab.registry.Registry
import net.impleri.slab.resources.ResourceLocation
import net.minecraft.world.level.block.{Block => McBlock}
import net.minecraft.world.level.block.state.BlockState

case class Block(private val underlying: BlockState, registry: Registry[Block, McBlock] = Registry.Blocks)
  extends IsRegistered[McBlock] with HasName {
  def name: String = getName.fold("unknown block")(_.toString)

  def value: McBlock = underlying.getBlock

  override def getName: Option[ResourceLocation] = registry.getKey(this)
}

object Block {
  def apply(block: McBlock): Block = new Block(block.defaultBlockState())
}
