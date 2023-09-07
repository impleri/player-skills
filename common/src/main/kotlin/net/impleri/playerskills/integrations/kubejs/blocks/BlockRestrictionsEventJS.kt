package net.impleri.playerskills.integrations.kubejs.blocks

import dev.latvian.mods.kubejs.server.ServerEventJS
import net.impleri.playerskills.restrictions.blocks.BlockRestrictionBuilder
import net.minecraft.server.MinecraftServer

class BlockRestrictionsEventJS(s: MinecraftServer) : ServerEventJS(s) {
  fun restrict(name: String, consumer: (BlockConditionsBuilderJS) -> Unit) {
    val builder = BlockConditionsBuilderJS(lazy { server })
    consumer(builder)

    BlockRestrictionBuilder.register(name, builder)
  }
}
