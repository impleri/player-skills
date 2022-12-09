package net.impleri.playerskills.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventActor;
import dev.architectury.event.EventFactory;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class SkillChangedEvent<T> {
    public static final Event<EventActor<SkillChangedEvent<?>>> EVENT = EventFactory.createEventActorLoop();

    @Nullable
    private static <V> SkillType<V> getSkillType(Skill<V> skill) {
        try {
            return SkillType.forSkill(skill);
        } catch (RegistryItemNotFound e) {
            e.printStackTrace();
        }

        return null;
    }

    private final Skill<T> prev;
    private final Skill<T> next;
    private final Player player;
    private final SkillType<T> type;

    public SkillChangedEvent(Player player, Skill<T> next, Skill<T> prev) {
        this.prev = prev;
        this.next = next;
        this.player = player;
        type = getSkillType(next);
    }

    public SkillType<T> getType() {
        return type;
    }

    public Skill<T> getSkill() {
        return next;
    }

    public Skill<T> getPrevious() {
        return prev;
    }

    public Player getPlayer() {
        return player;
    }
}
