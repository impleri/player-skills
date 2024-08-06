package net.impleri.slab.block

import net.impleri.slab.registry.Registry
import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper
import net.minecraft.world.level.block.{Block => McBlock}
import net.minecraft.world.level.block.state.BlockState

case class Block(
  protected val state: Block.VanillaState,
  registry: Registry[Block, Block.Vanilla] = Registry.Blocks,
) extends ResourceWrapper[Block.Vanilla] {
  override val underlying: Block.Vanilla = state.getBlock

  override val name: Option[ResourceLocation] = registry.getKey(this)

  def asString: String = name.fold("unknown block")(_.asString)

}

object Block {
  type Vanilla = McBlock

  type VanillaState = BlockState

  def apply(block: Vanilla): Block = new Block(block.defaultBlockState())
}
