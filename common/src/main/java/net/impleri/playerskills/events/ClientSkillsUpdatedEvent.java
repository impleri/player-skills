package net.impleri.playerskills.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventActor;
import dev.architectury.event.EventFactory;
import net.impleri.playerskills.api.Skill;

import java.util.List;

public record ClientSkillsUpdatedEvent(List<Skill<?>> next,
                                       List<Skill<?>> prev) {
    public static final Event<EventActor<ClientSkillsUpdatedEvent>> EVENT = EventFactory.createEventActorLoop();
}
