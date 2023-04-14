package net.impleri.playerskills.restrictions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Use net.impleri.playerskills.integration.kubejs.api.AbstractRestrictionBuilder
 */
@Deprecated()
public abstract class AbstractRestrictionBuilder<T extends AbstractRestriction<?>> extends net.impleri.playerskills.integration.kubejs.api.AbstractRestrictionBuilder<T> {

    public AbstractRestrictionBuilder(ResourceLocation id, @Nullable MinecraftServer server) {
        super(id, server);
    }

    public AbstractRestrictionBuilder(ResourceLocation id) {
        super(id);
    }
}
