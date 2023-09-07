package net.impleri.playerskills.integrations.kubejs.blocks

import dev.latvian.mods.kubejs.event.EventGroup
import dev.latvian.mods.kubejs.event.EventHandler

object BlockEventsBinding {
  val GROUP: EventGroup = EventGroup.of("BlockSkillEvents")
  val RESTRICTIONS: EventHandler = GROUP.server("register") { BlockRestrictionsEventJS::class.java }
}
