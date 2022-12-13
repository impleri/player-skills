package net.impleri.playerskills.integration.kubejs;

import dev.latvian.mods.rhino.util.HideFromJS;
import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.integration.kubejs.skills.SkillConditionBuilderJS;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.impleri.playerskills.server.ServerApi;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * Skills data that gets attached to Player
 */
public class PlayerDataJS {
    private final Player player;

    public PlayerDataJS(Player player) {
        this.player = player;
    }

    @Nullable
    @HideFromJS
    private <T> Skill<T> getSkill(String skillName) {
        try {
            return ServerApi.getSkill(player, skillName);
        } catch (RegistryItemNotFound e) {
            // TODO: handle error
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    @HideFromJS
    private <T> SkillType<T> getSkillType(@Nullable Skill<T> skill) {
        if (skill == null) {
            return null;
        }

        try {
            return SkillType.forSkill(skill);
        } catch (RegistryItemNotFound e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    @HideFromJS
    private <T> SkillConditionBuilderJS<T> getBuilderFor(Skill<T> skill, @Nullable Consumer<SkillConditionBuilderJS<T>> consumer) {
        if (consumer == null) {
            return null;
        }

        var builder = new SkillConditionBuilderJS<T>(skill, player);
        consumer.accept(builder);

        return builder;
    }

    @HideFromJS
    private <T> boolean handleChange(Skill<T> skill, T newValue) {
        if (newValue != null && skill.areChangesAllowed() && skill.isAllowedValue(newValue)) {
            PlayerSkills.LOGGER.debug("Should change {} to {}", skill.getName(), newValue);
            return ServerApi.set(player, skill.getName(), newValue);
        }

        return false;
    }

    public List<Skill<?>> getAll() {
        return ServerApi.getAllSkills(player);
    }

    public List<Skill<?>> getSkills() {
        return getAll();
    }

    public <T> boolean can(String skillName, @Nullable T expectedValue) {
        var skill = getSkill(skillName);

        if (skill == null) {
            return false;
        }

        return ServerApi.can(player, skill, expectedValue);
    }

    public <T> boolean can(String skill) {
        return can(skill, null);
    }

    public <T> boolean cannot(String skill, @Nullable T expectedValue) {
        return !can(skill, expectedValue);
    }

    public <T> boolean cannot(String skill) {
        return cannot(skill, null);
    }

    public <T> boolean set(String skillName, T newValue, @Nullable Consumer<SkillConditionBuilderJS<T>> consumer) {
        Skill<T> skill = getSkill(skillName);
        SkillType<T> type = getSkillType(skill);

        if (type == null) {
            return false;
        }

        @Nullable SkillConditionBuilderJS<T> builder = getBuilderFor(skill, consumer);
        boolean shouldChange = skill.getValue() != newValue;
        if (builder != null) {
            shouldChange = builder.shouldChange() && shouldChange;
        }

        if (shouldChange) {
            PlayerSkills.LOGGER.debug("Should set {} to {}.", skill.getName(), newValue);
            return handleChange(skill, newValue);
        }

        return false;
    }

    public <T> boolean set(String skillName, T newValue) {
        return set(skillName, newValue, null);
    }

    public <T> boolean improve(String skillName, @Nullable Consumer<SkillConditionBuilderJS<T>> consumer) {
        Skill<T> skill = getSkill(skillName);
        SkillType<T> type = getSkillType(skill);
        @Nullable SkillConditionBuilderJS<T> builder = getBuilderFor(skill, consumer);

        if (type == null) {
            return false;
        }

        @Nullable T newValue = type.getNextValue(skill);
        if (builder != null) {
            newValue = builder.calculateNext();
        }

        return handleChange(skill, newValue);
    }

    public <T> boolean improve(String skillName) {
        return improve(skillName, null);
    }

    public <T> boolean degrade(String skillName, @Nullable Consumer<SkillConditionBuilderJS<T>> consumer) {
        Skill<T> skill = getSkill(skillName);
        SkillType<T> type = getSkillType(skill);
        @Nullable SkillConditionBuilderJS<T> builder = getBuilderFor(skill, consumer);

        if (type == null) {
            return false;
        }

        @Nullable T newValue = type.getPrevValue(skill);
        if (builder != null) {
            newValue = builder.calculatePrev();
        }

        return handleChange(skill, newValue);
    }

    public <T> boolean degrade(String skillName) {
        return degrade(skillName, null);
    }

    public <T> boolean reset(String skillName, @Nullable Consumer<SkillConditionBuilderJS<T>> consumer) throws RegistryItemNotFound {
        Skill<T> skill = getSkill(skillName);
        SkillType<T> type = getSkillType(skill);
        @Nullable SkillConditionBuilderJS<T> builder = getBuilderFor(skill, consumer);

        if (type == null) {
            return false;
        }

        Skill<T> defaultSkill = net.impleri.playerskills.server.api.Skill.find(skillName);
        boolean shouldChange = skill.getValue() != defaultSkill.getValue();
        if (builder != null) {
            shouldChange = builder.shouldChange() && shouldChange;
        }

        if (shouldChange) {
            PlayerSkills.LOGGER.debug("Should reset {}.", skill.getName());
            return ServerApi.reset(player, skill.getName());
        }

        return false;
    }

    public <T> boolean reset(String skill) throws RegistryItemNotFound {
        return reset(skill, null);
    }
}
