package net.impleri.playerskills.integration.kubejs;

import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import net.impleri.playerskills.SkillResourceLocation;
import net.impleri.playerskills.api.Skill;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public abstract class Registries {
    private static final ResourceKey<Registry<Skill<?>>> key = ResourceKey.createRegistryKey(SkillResourceLocation.of("skill_builders_registry"));

    public static final RegistryObjectBuilderTypes<Skill<?>> SKILLS = RegistryObjectBuilderTypes.add(key, Skill.class);
}
