package net.impleri.playerskills.integrations.rei

import dev.architectury.event.EventResult
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.client.registry.display.visibility.DisplayVisibilityPredicate
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.entry.EntryStack
import net.impleri.playerskills.client.api.ItemRestrictionClient
import net.impleri.playerskills.events.ClientSkillsUpdatedEvent
import net.impleri.playerskills.utils.PlayerSkillsLogger
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

class SkillsDisplayVisibility : DisplayVisibilityPredicate {
  private val producibility: MutableMap<Item, Boolean> = HashMap()
  private val consumability: MutableMap<Item, Boolean> = HashMap()

  init {
    ClientSkillsUpdatedEvent.EVENT.register {
      clearCache()
    }
  }

  private fun clearCache() {
    PlayerSkillsLogger.ITEMS.debug("Clearing REI Visibility caches")
    producibility.clear()
    consumability.clear()
  }

  override fun getPriority(): Double {
    return 100.0
  }

  override fun handleDisplay(category: DisplayCategory<*>?, display: Display): EventResult {
    if (matchAnyIngredientInList(display.outputEntries) { hasHiddenOutput(it) }) {
      return EventResult.interruptFalse()
    }

    return if (matchAnyIngredientInList(display.inputEntries) { hasHiddenInput(it) }) {
      EventResult.interruptFalse()
    } else {
      EventResult.pass()
    }
  }

  private fun matchAnyIngredientInList(entries: List<EntryIngredient>, predicate: (EntryStack<*>) -> Boolean): Boolean {
    return entries.any {
      it.any(predicate)
    }
  }

  /**
   * Checks every ingredient to see if any are uncraftable
   */
  private fun isntProducible(item: Item): Boolean {
    return !producibility.computeIfAbsent(item, ItemRestrictionClient::canCraft)
  }

  private fun isntConsumable(item: Item): Boolean {
    return !consumability.computeIfAbsent(item, ItemRestrictionClient::canCraftWith)
  }

  private fun isFilteredAs(item: Item, predicate: (Item) -> Boolean): Boolean {
    return predicate(item)
  }

  private fun isFilteredAs(stack: ItemStack, predicate: (Item) -> Boolean): Boolean {
    return !stack.isEmpty && isFilteredAs(stack.item, predicate)
  }

  private fun hasHiddenOutput(entry: EntryStack<*>): Boolean {
    if (entry.isEmpty) {
      return false
    }

    return when (val value = entry.value) {
      is Item -> isFilteredAs(value) { isntProducible(it) }
      is ItemStack -> isFilteredAs(value) { isntProducible(it) }
      else -> false
    }
  }

  /**
   * Checks every ingredient to see if any are supposed to be hidden
   */
  private fun hasHiddenInput(entry: EntryStack<*>): Boolean {
    if (entry.isEmpty) {
      return false
    }
    return when (val value = entry.value) {
      is Item -> isFilteredAs(value) { isntConsumable(it) }
      is ItemStack -> isFilteredAs(value) { isntConsumable(it) }
      else -> false
    }
  }
}
