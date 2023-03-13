package net.impleri.playerskills.integration.kubejs.events;

import dev.architectury.registry.registries.DeferredRegister;
import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.integration.kubejs.skills.GenericSkillBuilderJS;
import net.impleri.playerskills.registry.RegistryItemAlreadyExists;
import net.impleri.playerskills.server.registry.Skills;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

public class SkillsRegistrationEventJS extends BaseSkillsRegistryEventJS {
    private static final ResourceKey<Registry<Skill<?>>> SKILL_REGISTRY = ResourceKey.createRegistryKey(Skills.REGISTRY_KEY);
    private static final DeferredRegister<Skill<?>> SKILLS = DeferredRegister.create(PlayerSkills.MOD_ID, SKILL_REGISTRY);

    public SkillsRegistrationEventJS(Map<String, RegistryObjectBuilderTypes.BuilderType<Skill<?>>> types) {
        super(types);
    }

    /**
     * Add a new skill to the registry
     */
    public <T> boolean add(String skillName, String type, @Nullable Consumer<GenericSkillBuilderJS<T>> consumer) throws RegistryItemAlreadyExists {
        var name = SkillResourceLocation.of(skillName);

        GenericSkillBuilderJS<T> builder = getBuilder(type, name);
        if (builder == null) {
            return false;
        }

        if (consumer != null) {
            consumer.accept(builder);
        }

        Skill<T> newSkill = builder.createObject();
        SKILLS.register(name, () -> newSkill);
        ConsoleJS.STARTUP.info("Created " + type + " skill " + name);

        return true;
    }

    public boolean add(String skillName, String type) throws RegistryItemAlreadyExists {
        return add(skillName, type, null);
    }

    protected void afterPosted(boolean isCanceled) {
        SKILLS.register();
    }
}
