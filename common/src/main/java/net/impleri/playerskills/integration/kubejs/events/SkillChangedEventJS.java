package net.impleri.playerskills.integration.kubejs.events;

import dev.latvian.mods.kubejs.server.ServerEventJS;
import dev.latvian.mods.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class SkillChangedEventJS<T> extends ServerEventJS {
    private final Skill<T> prev;
    private final Skill<T> next;
    private final Player player;
    private SkillType<T> type;

    public SkillChangedEventJS(Player player, Skill<T> next, Skill<T> prev) {
        super(UtilsJS.staticServer);
        this.prev = prev;
        this.next = next;
        this.player = player;
        getSkillType();
    }

    @Nullable
    @HideFromJS
    private void getSkillType() {
        try {
            type = SkillType.forSkill(next);
        } catch (RegistryItemNotFound e) {
            e.printStackTrace();
        }
    }

    public boolean getIsImproved() {
        return type.getNextValue(prev) == next.getValue();
    }

    public boolean getIsDegraded() {
        return type.getPrevValue(prev) == next.getValue();
    }

    public boolean getIsUnchanged() {
        return prev.getValue() == next.getValue();
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
