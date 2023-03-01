package net.impleri.playerskills.utils;

import net.minecraft.resources.ResourceLocation;

/**
 * Helper class for generating ResourceLocation, defaulting the namespace to playerSkills
 */
public class SkillResourceLocation {
    public static ResourceLocation of(ResourceLocation resourceLocation) {
        return resourceLocation;
    }

    public static ResourceLocation of(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    public static ResourceLocation of(String path) {
        String[] elements = decompose(path);

        return of(elements[0], elements[1]);
    }

    /**
     * ofMinecraft
     * <p>
     * Attempt to parse the string into a minecraft-default ResourceLocation and fallback to skills namespace if some error
     */
    public static ResourceLocation ofMinecraft(String value) {
        var mcResource = ResourceLocation.tryParse(value);

        return mcResource == null ? of(value) : mcResource;
    }

    private static final String DEFAULT_NAMESPACE = "skills";

    private static String[] decompose(String string) {
        String namespace = DEFAULT_NAMESPACE;
        String element = string;

        int i = string.indexOf(":");
        if (i >= 0) {
            element = string.substring(i + 1);

            if (i >= 1) {
                namespace = string.substring(0, i);
            }
        }

        return new String[]{namespace, element};
    }
}
