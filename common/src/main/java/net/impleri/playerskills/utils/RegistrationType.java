package net.impleri.playerskills.utils;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class RegistrationType<T> {
    @Nullable
    private final String namespace;
    @Nullable
    private final ResourceLocation name;
    @Nullable
    private final TagKey<T> tag;

    public RegistrationType(String value, ResourceKey<Registry<T>> registryKey) {
        if (value.trim().startsWith("@")) {
            namespace = value.substring(1);
            name = null;
            tag = null;
        } else if (value.trim().startsWith("#")) {
            var tagKey = value.substring(1);

            tag = TagKey.create(registryKey, SkillResourceLocation.ofMinecraft(tagKey));
            namespace = null;
            name = null;
        } else if (value.trim().endsWith(":*")) {
            namespace = value.substring(0, value.indexOf(":"));
            name = null;
            tag = null;
        } else {
            name = SkillResourceLocation.ofMinecraft(value);
            namespace = null;
            tag = null;
        }
    }

    public void ifTag(@NotNull Consumer<TagKey<T>> consumer) {
        if (tag != null) {
            consumer.accept(tag);
        }
    }

    public void ifName(@NotNull Consumer<ResourceLocation> consumer) {
        if (name != null) {
            consumer.accept(name);
        }
    }

    public void ifNamespace(@NotNull Consumer<String> consumer) {
        if (namespace != null) {
            consumer.accept(namespace);
        }
    }
}
