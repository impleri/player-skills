package net.impleri.playerskills.client;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.restrictions.AbstractRestriction;
import net.impleri.playerskills.restrictions.Registry;
import net.impleri.playerskills.restrictions.RestrictionsApi;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Predicate;

public abstract class RestrictionsClient<T, R extends AbstractRestriction<T>, A extends RestrictionsApi<T, R>> {
    protected static Player getPlayer() {
        try {
            return Minecraft.getInstance().player;
        } catch (Throwable error) {
            PlayerSkills.LOGGER.warn("Unable to get the clientside player: {}", error.getMessage());
        }

        return null;
    }

    private final Registry<R> registry;

    protected final A serverApi;

    public RestrictionsClient(Registry<R> registry, A serverApi) {
        this.registry = registry;
        this.serverApi = serverApi;
    }

    public List<R> getAll() {
        return registry.entries();
    }

    protected List<R> getFiltered(Predicate<R> predicate) {
        return serverApi.getFiltered(getPlayer(), predicate);
    }

    /**
     * @deprecated Use the more expressive methods available on subclasses (e.g. ItemSkills.isConsumable)
     */
    @Deprecated
    public boolean canPlayer(ResourceLocation resource, String fieldName) {
        return serverApi.canPlayer(getPlayer(), resource, fieldName);
    }

    /**
     * @deprecated Use the more expressive methods available on subclasses (e.g. ItemSkills.isConsumable)
     */
    @Deprecated
    public boolean canPlayer(T target, String fieldName) {
        return serverApi.canPlayer(getPlayer(), target, fieldName);
    }
}
