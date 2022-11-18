package net.impleri.playerskills.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;
import net.impleri.playerskills.integration.kubejs.events.SkillChangedEventJS;
import net.impleri.playerskills.integration.kubejs.skills.SkillsEventJS;

abstract class EventsBinding {
    public static final EventGroup GROUP = EventGroup.of("SkillEvents");

    public static final EventHandler SKILLS = GROUP.server("skills", () -> SkillsEventJS.class);

    public static final EventHandler SKILL_CHANGED = GROUP.server("onChanged", () -> SkillChangedEventJS.class).extra(Extra.ID);
}
