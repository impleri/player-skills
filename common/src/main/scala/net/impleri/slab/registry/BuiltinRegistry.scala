package net.impleri.slab.registry

import net.impleri.slab.resources.ResourceWrapper
import net.impleri.slab.world.Biome
import net.minecraft.data.BuiltinRegistries

class BuiltinRegistry[T <: ResourceWrapper[U], U](
  override val underlying: Registry.Vanilla[U],
  override val f: U => T,
) extends Registry[T, U](underlying, f)

object BuiltinRegistry {
  final val BIOME_VANILLA: Registry.Vanilla[Biome.Vanilla] = BuiltinRegistries.BIOME

  final val BIOME: BuiltinRegistry[Biome, Biome.Vanilla] = new BuiltinRegistry(
    BuiltinRegistries.BIOME,
    Biome(_),
  )
}
