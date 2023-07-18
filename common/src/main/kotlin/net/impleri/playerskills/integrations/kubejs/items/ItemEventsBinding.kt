package net.impleri.playerskills.integrations.kubejs.items

import dev.latvian.mods.kubejs.event.EventGroup
import dev.latvian.mods.kubejs.event.EventHandler

object ItemEventsBinding {
  val GROUP: EventGroup = EventGroup.of("ItemSkillEvents")
  val RESTRICTIONS: EventHandler = GROUP.server("register") { ItemRestrictionsEventJS::class.java }
}
