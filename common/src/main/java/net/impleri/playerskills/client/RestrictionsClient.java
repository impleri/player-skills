package net.impleri.playerskills.client;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.RestrictionsApi;
import net.impleri.playerskills.restrictions.AbstractRestriction;
import net.impleri.playerskills.restrictions.Registry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Predicate;

public abstract class RestrictionsClient<T extends AbstractRestriction<?>> {
    private static Player getPlayer() {
        try {
            return Minecraft.getInstance().player;
        } catch (Throwable error) {
            PlayerSkills.LOGGER.warn("Unable to get the clientside player: {}", error.getMessage());
        }

        return null;
    }

    private final Registry<T> registry;

    private final RestrictionsApi<T> serverApi;

    public RestrictionsClient(Registry<T> registry, RestrictionsApi<T> serverApi) {
        this.registry = registry;
        this.serverApi = serverApi;
    }

    public List<T> getAll() {
        return registry.entries();
    }

    protected List<T> getFiltered(Predicate<T> predicate) {
        var player = getPlayer();

        return getAll().stream()
                .filter(r -> r.condition.test(player) && predicate.test(r))
                .toList();
    }

    public boolean canPlayer(ResourceLocation item, String fieldName) {
        var player = getPlayer();

        return serverApi.canPlayer(player, item, fieldName);
    }

}
