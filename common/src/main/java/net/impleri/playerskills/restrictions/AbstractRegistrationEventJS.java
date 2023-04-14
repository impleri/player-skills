package net.impleri.playerskills.restrictions;

import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;

/**
 * @deprecated Use net.impleri.playerskills.integration.kubejs.api.AbstractRestrictionBuilder
 */
@Deprecated()
abstract public class AbstractRegistrationEventJS<T, R extends AbstractRestriction<T>, B extends net.impleri.playerskills.integration.kubejs.api.AbstractRestrictionBuilder<R>>
        extends net.impleri.playerskills.integration.kubejs.api.AbstractRegistrationEventJS<T, R, B> {
    public AbstractRegistrationEventJS(MinecraftServer s, String type, Registry<T> registry) {
        super(s, type, registry);
    }
}
