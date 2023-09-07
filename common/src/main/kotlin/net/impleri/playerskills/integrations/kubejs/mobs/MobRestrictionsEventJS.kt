package net.impleri.playerskills.integrations.kubejs.mobs

import dev.latvian.mods.kubejs.server.ServerEventJS
import net.impleri.playerskills.restrictions.mobs.MobRestrictionBuilder
import net.minecraft.server.MinecraftServer

class MobRestrictionsEventJS(s: MinecraftServer) : ServerEventJS(s) {
  fun restrict(name: String, consumer: (MobConditionsBuilderJS) -> Unit) {
    val builder = MobConditionsBuilderJS(lazy { server })
    consumer(builder)

    MobRestrictionBuilder.register(name, builder)
  }
}
