package net.impleri.playerskills.integrations.rei

import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule
import me.shedaniel.rei.api.common.util.EntryStacks
import net.impleri.playerskills.client.api.ItemRestrictionClient
import net.impleri.playerskills.events.ClientSkillsUpdatedEvent
import net.impleri.playerskills.items.ItemSkills
import net.impleri.playerskills.utils.ListDiff
import net.minecraft.world.item.Item

object PlayerSkillsFiltering {
  private var filteringRule: BasicFilteringRule.MarkDirty? = null
  private val currentlyFiltered: MutableList<Item> = ArrayList()
  fun updateHidden(event: ClientSkillsUpdatedEvent) {
    ItemSkills.LOGGER.debug("Client skills list has been updated: ${event.next}")
    val nextHidden: List<Item> = ItemRestrictionClient.hidden

    // Nothing on either list, so don't bother
    if (currentlyFiltered.isEmpty() && nextHidden.isEmpty()) {
      ItemSkills.LOGGER.debug("No changes in restrictions")
      return
    }

    val toHide = !ListDiff.contains(currentlyFiltered, nextHidden)
    val toReveal = !ListDiff.contains(nextHidden, currentlyFiltered)
    if ((toHide || toReveal) && filteringRule != null) {
      ItemSkills.LOGGER.debug("Updating REI filters")
      // Update what we're supposed
      currentlyFiltered.clear()
      currentlyFiltered.addAll(nextHidden)

      // Trigger re-filtering REI entry stacks
      filteringRule?.markDirty()
    }
  }

  fun register(rule: BasicFilteringRule<*>) {
    filteringRule = rule.hide {
      ItemSkills.LOGGER.debug("Updating REI filters")

      currentlyFiltered.map { EntryStacks.of(it) }
    }
  }
}
