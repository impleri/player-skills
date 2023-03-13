package net.impleri.playerskills.registry;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.Registries;
import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.SkillType;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public abstract class SkillTypes {
    public static final ResourceLocation REGISTRY_KEY = SkillResourceLocation.of("skill_types_registry");

    private static Registrar<SkillType<?>> REGISTRY = Registries.get(PlayerSkills.MOD_ID)
            .<SkillType<?>>builder(REGISTRY_KEY)
            .build();

    // Dummy method to ensure static elements are created
    public static void buildRegistry() {
        if (!REGISTRY.key().location().equals(REGISTRY_KEY)) {
            PlayerSkills.LOGGER.warn("Skills registry is invalid.");
        }
    }

    /**
     * Get all SkillTypes registered
     */
    public static List<SkillType<?>> entries() {
        return REGISTRY.entrySet().stream().<SkillType<?>>map(Map.Entry::getValue).toList();
    }

    private static <T> @Nullable SkillType<T> maybeFind(ResourceLocation name) {
        @Nullable SkillType<?> type = REGISTRY.get(name);

        if (type != null) {
            @SuppressWarnings("unchecked") SkillType<T> castSkill = ((SkillType<T>) type);
            return castSkill;
        }

        return null;
    }

    /**
     * Find a SkillType by name or throw an error
     */
    public static <T> SkillType<T> find(ResourceLocation name) throws RegistryItemNotFound {
        SkillType<T> type = maybeFind(name);

        if (type == null) {
            throw new RegistryItemNotFound();
        }

        return type;
    }
}
