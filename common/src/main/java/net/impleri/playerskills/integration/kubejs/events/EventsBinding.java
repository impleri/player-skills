package net.impleri.playerskills.integration.kubejs.events;

public abstract class EventsBinding {

    public static final String REGISTRATION = "skills.registration"; //, () -> SkillsRegistrationEventJS.class);

    public static final String MODIFICATION = "skills.modification"; //, () -> SkillsModificationEventJS.class);

    public static final String SKILL_CHANGED = "skills.onChanged"; //, () -> PlayerSkillChangedEventJS.class).extra(Extra.ID);
}
