package net.impleri.playerskills.client

import dev.architectury.event.events.client.ClientTooltipEvent
import dev.architectury.registry.ReloadListenerRegistry
import net.impleri.playerskills.api.ItemRestrictions
import net.impleri.playerskills.client.api.ItemRestrictionClient
import net.impleri.playerskills.events.ClientSkillsUpdatedEvent
import net.impleri.playerskills.items.ItemSkills
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import java.util.concurrent.ConcurrentHashMap

class ClientEventHandlers : ResourceManagerReloadListener {
  private fun registerEvents() {
    // Tooltip
    ClientTooltipEvent.ITEM.register(
      ClientTooltipEvent.Item { stack: ItemStack, lines: MutableList<Component>, _: TooltipFlag ->
        beforeRenderItemTooltip(
          stack,
          lines,
        )
      },
    )

    ClientSkillsUpdatedEvent.EVENT.register { clearCache() }

    ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, this)
  }

  override fun onResourceManagerReload(resourceManager: ResourceManager) {
    PlayerSkillsClient.resyncSkills()
  }

  private val identifiability = ConcurrentHashMap<Item, Boolean>()

  private fun clearCache() {
    identifiability.clear()
  }

  private fun populateCache(item: Item): Boolean {
    return ItemRestrictionClient.canIdentify(item)
  }

  private fun beforeRenderItemTooltip(stack: ItemStack, lines: MutableList<Component>) {
    val item = ItemRestrictions.getValue(stack)
    if (!identifiability.computeIfAbsent(item) { populateCache(it) }) {
      ItemSkills.LOGGER.debug("Replacing tooltip for $item")
      lines.clear()
      lines.add(Component.translatable("message.playerskills.unknown_item").withStyle(ChatFormatting.RED))
    }
  }

  companion object {
    private val INSTANCE = ClientEventHandlers()

    fun init() {
      INSTANCE.registerEvents()
    }
  }
}
