package net.impleri.playerskills.integration.kubejs.skills;

import net.impleri.playerskills.api.PlayerSkill;
import net.impleri.playerskills.api.Skill;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayerDataJS {
    private final Player player;

    public PlayerDataJS(Player player) {
        this.player = player;
    }

    public <T> boolean can(ResourceLocation skill, @Nullable T expectedValue) {
        return PlayerSkill.can(player, skill, expectedValue);
    }

    public <T> boolean cannot(ResourceLocation skill, @Nullable T expectedValue) {
        return !PlayerSkill.can(player, skill, expectedValue);
    }

    public <T> boolean grant(ResourceLocation skill, @Nullable T newValue) {
        return PlayerSkill.set(player, skill, newValue);
    }

    public boolean reset(ResourceLocation skill) {
        return PlayerSkill.reset(player, skill);
    }

    public List<Skill<?>> getAll() {
        return PlayerSkill.getAllSkills(player);
    }
}
