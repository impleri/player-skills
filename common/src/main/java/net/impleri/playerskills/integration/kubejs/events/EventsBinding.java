package net.impleri.playerskills.integration.kubejs.events;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;

public abstract class EventsBinding {
    public static final EventGroup GROUP = EventGroup.of("SkillEvents");

    public static final EventHandler REGISTRATION = GROUP.startup("registration", () -> SkillsRegistrationEventJS.class);

    public static final EventHandler MODIFICATION = GROUP.server("modification", () -> SkillsModificationEventJS.class);

    public static final EventHandler SKILL_CHANGED = GROUP.server("onChanged", () -> PlayerSkillChangedEventJS.class).extra(Extra.ID);
}
