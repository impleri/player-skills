package net.impleri.playerskills.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import net.impleri.playerskills.integration.kubejs.skills.SkillsEventJS;

abstract class EventsBinding {
    public static final EventGroup GROUP = EventGroup.of("SkillEvents");

    public static final EventHandler SKILLS = GROUP.server("skills", () -> SkillsEventJS.class);
}
