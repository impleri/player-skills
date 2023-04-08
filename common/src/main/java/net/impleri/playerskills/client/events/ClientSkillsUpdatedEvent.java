package net.impleri.playerskills.client.events;

import com.google.common.collect.ImmutableList;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.impleri.playerskills.api.Skill;

import java.util.function.Consumer;

public record ClientSkillsUpdatedEvent(ImmutableList<Skill<?>> next,
                                       ImmutableList<Skill<?>> prev,
                                       boolean force) {
    public static final Event<Consumer<ClientSkillsUpdatedEvent>> EVENT = EventFactory.createConsumerLoop();
}
