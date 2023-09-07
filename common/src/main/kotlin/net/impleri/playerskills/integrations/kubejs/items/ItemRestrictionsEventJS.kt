package net.impleri.playerskills.integrations.kubejs.items

import dev.latvian.mods.kubejs.server.ServerEventJS
import net.impleri.playerskills.restrictions.items.ItemRestrictionBuilder
import net.minecraft.server.MinecraftServer

class ItemRestrictionsEventJS(s: MinecraftServer) : ServerEventJS(s) {
  fun restrict(name: String, consumer: (ItemConditionsBuilderJS) -> Unit) {
    val builder = ItemConditionsBuilderJS(lazy { server })
    consumer(builder)

    ItemRestrictionBuilder.register(name, builder)
  }
}
