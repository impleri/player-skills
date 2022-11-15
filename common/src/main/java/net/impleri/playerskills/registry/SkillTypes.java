package net.impleri.playerskills.registry;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.Registries;
import net.impleri.playerskills.PlayerSkillsCore;
import net.impleri.playerskills.SkillResourceLocation;
import net.impleri.playerskills.api.SkillType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public abstract class SkillTypes {
    public static final ResourceLocation REGISTRY_KEY = SkillResourceLocation.of("skill_types_registry");

    private static final Registrar<SkillType<?>> registry = Registries.get(PlayerSkillsCore.MOD_ID)
            .<SkillType<?>>builder(REGISTRY_KEY)
            .build();

    /**
     * Get all SkillTypes registered
     */
    public static List<SkillType<?>> entries() {
        return registry.entrySet().stream().<SkillType<?>>map(Map.Entry::getValue).toList();
    }

    private static <T> @Nullable SkillType<T> maybeFind(ResourceLocation name) {
        @Nullable SkillType<?> type = registry.get(name);

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

    /**
     * Adds a SkillType if it does not already exists
     */
    public static <T> int add(Supplier<SkillType<T>> type) throws RegistryItemAlreadyExists {
        ResourceLocation skillName = type.get().getName();

        SkillType<T> existing = maybeFind(skillName);
        if (existing != null) {
            throw new RegistryItemAlreadyExists();
        }

        registry.register(skillName, type);

        return registry.entrySet().size();
    }
}
