package net.impleri.playerskills.integration.kubejs.skills;

import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.impleri.playerskills.PlayerSkillsCore;
import net.impleri.playerskills.SkillResourceLocation;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.registry.RegistryItemAlreadyExists;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.impleri.playerskills.registry.Skills;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

public class SkillsEventJS extends EventJS {
    private final Map<String, RegistryObjectBuilderTypes.BuilderType<Skill<?>>> types;

    public SkillsEventJS(Map<String, RegistryObjectBuilderTypes.BuilderType<Skill<?>>> types) {
        super();

        this.types = types;
    }

    @HideFromJS
    @Nullable
    private <T> Skill<T> getSkill(String skillName) {
        var name = SkillResourceLocation.of(skillName);
        Skill<T> skill = null;
        try {
            skill = Skills.find(name);
            return skill;
        } catch (RegistryItemNotFound e) {
            ConsoleJS.SERVER.error("Unable to find skill " + name);
        }

        return null;
    }

    @HideFromJS
    @Nullable
    private <T> GenericSkillBuilderJS<T> getBuilder(String skillType, ResourceLocation name) {
        // Ensure we have a full ResourceLocation before casting to String
        var type = SkillResourceLocation.of(skillType).toString();

        PlayerSkillsCore.LOGGER.debug("Creating skill builder for " + name.toString() + " typed as " + type);
        @Nullable RegistryObjectBuilderTypes.BuilderType<Skill<?>> builderType = types.get(type);
        if (builderType == null) {
            ConsoleJS.SERVER.error("Builder not found for skill type " + type);
            return null;
        }

        var uncastBuilder = builderType.factory().createBuilder(name);
        if (uncastBuilder instanceof GenericSkillBuilderJS) {
            @SuppressWarnings("unchecked") GenericSkillBuilderJS<T> builder = (GenericSkillBuilderJS<T>) uncastBuilder;
            return builder;
        }

        return null;
    }

    /**
     * Add a new skill to the registry
     */
    public <T> boolean add(String skillName, String type, Consumer<GenericSkillBuilderJS<T>> consumer) throws RegistryItemAlreadyExists {
        var name = SkillResourceLocation.of(skillName);

        GenericSkillBuilderJS<T> builder = getBuilder(type, name);
        if (builder == null) {
            return false;
        }

        consumer.accept(builder);

        Skill<T> newSkill = builder.createObject();
        Skills.add(newSkill);
        ConsoleJS.SERVER.info("Created " + type + " skill " + name);

        return true;
    }

    public <T> boolean modify(String name, Consumer<GenericSkillBuilderJS<T>> consumer) {
        return modify(name, null, consumer);
    }

    public <T> boolean modify(String skillName, @Nullable String skillType, Consumer<GenericSkillBuilderJS<T>> consumer) {
        @Nullable Skill<T> skill = getSkill(skillName);
        if (skill == null) {
            return false;
        }

        String type = (skillType != null) ? skillType : skill.getType().toString();
        ResourceLocation name = SkillResourceLocation.of(skillName);

        GenericSkillBuilderJS<T> builder = getBuilder(type, name);
        if (builder == null) {
            return false;
        }

        builder.syncWith(skill);
        consumer.accept(builder);

        Skill<T> newSkill = builder.createObject();
        Skills.upsert(newSkill);
        ConsoleJS.SERVER.info("Updated " + type + " skill " + name);

        return false;
    }

    public <T> boolean remove(String name) {
        @Nullable Skill<T> skill = getSkill(name);
        if (skill == null) {
            return true;
        }

        try {
            Skills.remove(skill);
        } catch (RegistryItemNotFound e) {
            return true;
        }

        ConsoleJS.SERVER.info("Removed skill " + name);

        return true;
    }
}
