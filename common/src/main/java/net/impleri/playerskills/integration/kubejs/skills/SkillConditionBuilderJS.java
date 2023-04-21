package net.impleri.playerskills.integration.kubejs.skills;

import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.RemapForJS;
import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.integration.kubejs.Registries;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class SkillConditionBuilderJS<T> extends BuilderBase<Skill<T>> {
    protected Skill<T> skill;
    protected @Nullable SkillType<T> type;
    protected Player player;

    private final RandomSource random = RandomSource.create();

    protected double appliedChance = 100.0;

    public @Nullable T min;
    public @Nullable T max;
    public @Nullable Boolean conditionIf;
    public @Nullable Boolean conditionUnless;

    @HideFromJS
    public SkillConditionBuilderJS(Skill<T> skill, Player player) {
        super(skill.getName());
        this.skill = skill;
        this.player = player;

        getType();
    }

    @Override
    public RegistryObjectBuilderTypes<Skill<?>> getRegistryType() {
        return Registries.SKILLS;
    }

    @Override
    public Skill<T> createObject() {
        return null;
    }

    @HideFromJS
    private void getType() {
        try {
            this.type = SkillType.forSkill(skill);
        } catch (RegistryItemNotFound e) {
            PlayerSkills.LOGGER.error("Unable to retrieve SkillType {} for {}", skill.getType(), skill.getName());
        }
    }

    @HideFromJS
    private boolean calculateConditions() {
        // True if there is no if condition or if the condition is true
        boolean hasIf = (conditionIf == null || conditionIf);

        // True if there is no unless condition or if the condition is false
        boolean hasUnless = (conditionUnless == null || !conditionUnless);

        boolean hasChance = (appliedChance >= random.nextIntBetweenInclusive(0, 100));

        PlayerSkills.LOGGER.debug("Checking conditions. IF: {}->{}. UNLESS: {}->{}", conditionIf, hasIf, conditionUnless, hasUnless);

        return (hasIf && hasUnless && hasChance);
    }

    @HideFromJS
    @Nullable
    public boolean shouldChange() {
        return (type != null && calculateConditions());
    }

    @HideFromJS
    @Nullable
    public T calculatePrev() {
        if (shouldChange()) {
            return type.getPrevValue(skill, min, max);
        }

        return null;
    }

    @HideFromJS
    @Nullable
    public T calculateNext() {
        if (shouldChange()) {
            return type.getNextValue(skill, min, max);
        }

        return null;
    }

    public SkillConditionBuilderJS<T> chance(Double value) {
        appliedChance = value;

        return this;
    }

    public SkillConditionBuilderJS<T> min(T value) {
        min = value;

        return this;
    }

    public SkillConditionBuilderJS<T> max(T value) {
        max = value;

        return this;
    }

    @RemapForJS("if")
    public SkillConditionBuilderJS<T> onlyIf(boolean value) {
        conditionIf = value;

        return this;
    }

    public SkillConditionBuilderJS<T> unless(boolean value) {
        conditionUnless = value;

        return this;
    }
}
