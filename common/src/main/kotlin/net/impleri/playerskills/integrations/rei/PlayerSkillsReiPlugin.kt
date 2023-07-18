package net.impleri.playerskills.integrations.rei

import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import net.impleri.playerskills.events.ClientSkillsUpdatedEvent

open class PlayerSkillsReiPlugin : REIClientPlugin {
  init {
    // Subscribe to client skill updates to refresh basic filtering rules
    ClientSkillsUpdatedEvent.EVENT.register {
      PlayerSkillsFiltering.updateHidden(it)
    }
  }

  override fun registerDisplays(registry: DisplayRegistry) {
    registry.registerVisibilityPredicate(SkillsDisplayVisibility())
  }

  override fun registerBasicEntryFiltering(rule: BasicFilteringRule<*>) {
    PlayerSkillsFiltering.register(rule)
  }
}
