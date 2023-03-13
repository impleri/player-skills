package net.impleri.playerskills.integration.kubejs.events;

import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.integration.kubejs.skills.GenericSkillBuilderJS;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class BaseSkillsRegistryEventJS extends EventJS {
    private final Map<String, RegistryObjectBuilderTypes.BuilderType<Skill<?>>> types;

    public BaseSkillsRegistryEventJS(Map<String, RegistryObjectBuilderTypes.BuilderType<Skill<?>>> types) {
        super();

        this.types = types;
    }

    @HideFromJS
    @Nullable
    protected <T> GenericSkillBuilderJS<T> getBuilder(String skillType, ResourceLocation name) {
        // Ensure we have a full ResourceLocation before casting to String
        var type = SkillResourceLocation.of(skillType).toString();

        PlayerSkills.LOGGER.debug("Creating skill builder for " + name.toString() + " typed as " + type);
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
}
