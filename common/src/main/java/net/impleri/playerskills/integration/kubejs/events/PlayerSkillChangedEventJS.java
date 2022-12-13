package net.impleri.playerskills.integration.kubejs.events;

import dev.latvian.mods.kubejs.server.ServerEventJS;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.server.events.SkillChangedEvent;
import net.minecraft.world.entity.player.Player;

public class PlayerSkillChangedEventJS<T> extends ServerEventJS {
    private final SkillChangedEvent<T> event;

    public PlayerSkillChangedEventJS(SkillChangedEvent<T> event) {
        super(UtilsJS.staticServer);
        this.event = event;
    }

    public boolean getIsImproved() {
        var type = event.getType();

        if (type == null) {
            return false;
        }

        return type.getNextValue(event.getPrevious()) == event.getSkill().getValue();
    }

    public boolean getIsDegraded() {
        var type = event.getType();

        if (type == null) {
            return false;
        }

        return type.getPrevValue(event.getPrevious()) == event.getSkill().getValue();
    }

    public Skill<T> getSkill() {
        return event.getSkill();
    }

    public Skill<T> getPrevious() {
        return event.getPrevious();
    }

    public Player getPlayer() {
        return event.getPlayer();
    }
}
