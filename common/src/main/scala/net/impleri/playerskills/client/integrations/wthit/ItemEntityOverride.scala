package net.impleri.playerskills.client.integrations.wthit

import mcp.mobius.waila.api.{IEntityAccessor, IEntityComponentProvider, IPluginConfig, ITooltip, WailaConstants}
import net.impleri.playerskills.client.PlayerSkillsClient
import net.impleri.playerskills.client.restrictions.ItemRestrictionOpsClient
import net.impleri.slab.item.Item
import net.minecraft.world.entity.item.ItemEntity

class ItemEntityOverride(
    itemRestrictions: ItemRestrictionOpsClient,
) extends IEntityComponentProvider {
  override def appendHead(tooltip: ITooltip, accessor: IEntityAccessor, config: IPluginConfig): Unit = {
    val canIdentify = for {
      entity <- Option(accessor.getEntity[ItemEntity])
      item = Item(entity.getItem)
    } yield itemRestrictions.isIdentifiable(item, None)

    if (canIdentify.contains(false)) {
      tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, ItemRestrictionOpsClient.UnknownItem.output)

      val hasRegistryName = Option(tooltip.getLine(WailaConstants.REGISTRY_NAME_TAG)).nonEmpty
      if (hasRegistryName) {
        tooltip.setLine(WailaConstants.REGISTRY_NAME_TAG, ItemRestrictionOpsClient.UnknownItem.output)
      }
    }
  }
}

object ItemEntityOverride {
  def apply(): ItemEntityOverride = new ItemEntityOverride(PlayerSkillsClient.STATE.ITEM_RESTRICTIONS)
}
