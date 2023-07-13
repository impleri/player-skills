package net.impleri.playerskills.integrations.kubejs.skills

import dev.latvian.mods.kubejs.event.EventGroup
import dev.latvian.mods.kubejs.event.EventHandler
import dev.latvian.mods.kubejs.event.Extra

object SkillsEventsBinding {
  val GROUP: EventGroup = EventGroup.of("SkillEvents")
  val REGISTRATION: EventHandler = GROUP.startup("registration") { SkillsRegistrationEventJS::class.java }
  val MODIFICATION: EventHandler = GROUP.server("modification") { SkillsModificationEventJS::class.java }
  val SKILL_CHANGED: EventHandler = GROUP.server("onChanged") { PlayerSkillChangedEventJS::class.java }
    .extra(Extra.ID)
}
