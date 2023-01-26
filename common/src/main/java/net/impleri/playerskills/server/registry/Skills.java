package net.impleri.playerskills.server.registry;

import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.Registries;
import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.registry.RegistryItemAlreadyExists;
import net.impleri.playerskills.registry.RegistryItemNotFound;
import net.impleri.playerskills.utils.SkillResourceLocation;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Skills {
    public static final ResourceLocation REGISTRY_KEY = SkillResourceLocation.of("skills_registry");

    /**
     * Initial Registry
     */
    private static final Registrar<Skill<?>> REGISTRY = Registries.get(PlayerSkills.MOD_ID)
            .<Skill<?>>builder(REGISTRY_KEY)
            .build();

    /**
     * GAME Registry
     */
    private static final Map<ResourceLocation, Skill<?>> registry = new HashMap<>();

    // Dummy method to ensure static elements are created
    public static void buildRegistry() {
        if (!REGISTRY.key().location().equals(REGISTRY_KEY)) {
            PlayerSkills.LOGGER.warn("Skills registry is invalid.");
        }
    }

    /**
     * Resets the GAME registry to match the Initial registry
     */
    public static void resync() {
        registry.clear();
        REGISTRY.entrySet().forEach(entry -> registry.put(entry.getKey().location(), entry.getValue()));
    }

    /**
     * Get an IMMUTABLE List of the Skills in cache
     */
    public static List<Skill<?>> entries() {
        return registry.values().stream().toList();
    }

    private static <T> @Nullable Skill<T> maybeFind(ResourceLocation name) {
        @Nullable Skill<?> type = registry.get(name);

        if (type != null) {
            @SuppressWarnings("unchecked") Skill<T> castSkill = ((Skill<T>) type);
            return castSkill;
        }

        return null;
    }

    /**
     * Find a Skill by name or throw an error
     */
    public static <T> Skill<T> find(ResourceLocation name) throws RegistryItemNotFound {
        Skill<T> skill = maybeFind(name);

        if (skill == null) {
            throw new RegistryItemNotFound();
        }

        return skill;
    }

    /**
     * Adds a Skill if it does not already exists
     */
    public static <T> int add(Skill<T> skill) throws RegistryItemAlreadyExists {
        Skill<T> existing = maybeFind(skill.getName());
        if (existing != null) {
            throw new RegistryItemAlreadyExists();
        }

        return upsert(skill);
    }

    /**
     * Upserts a skill in the registry even if it already exists
     */
    public static <T> int upsert(Skill<T> skill) {
        registry.put(skill.getName(), skill);

        return registry.size();
    }

    /**
     * Removes a Skill if it exists
     */
    public static <T> int remove(Skill<T> skill) throws RegistryItemNotFound {
        ResourceLocation skillName = skill.getName();
        find(skillName);

        registry.remove(skillName);

        return registry.size();
    }
}
