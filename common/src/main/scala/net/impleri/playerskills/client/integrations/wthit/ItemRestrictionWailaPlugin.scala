package net.impleri.playerskills.client.integrations.wthit

import mcp.mobius.waila.api.{IClientRegistrar, IWailaClientPlugin}
import net.minecraft.world.entity.item.ItemEntity

class ItemRestrictionWailaPlugin(
    private val provider: ItemEntityOverride = ItemEntityOverride(),
) extends IWailaClientPlugin {
  override def register(registrar: IClientRegistrar): Unit = {
    registrar.head(provider, classOf[ItemEntity], Int.MaxValue)
  }
}
