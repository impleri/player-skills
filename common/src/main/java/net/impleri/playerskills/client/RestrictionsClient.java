package net.impleri.playerskills.client;

import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.restrictions.AbstractRestriction;
import net.impleri.playerskills.restrictions.Registry;
import net.impleri.playerskills.restrictions.RestrictionsApi;
import net.impleri.playerskills.utils.PlayerSkillsLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class RestrictionsClient<T, R extends AbstractRestriction<T>, A extends RestrictionsApi<T, R>> {
    private final Registry<R> registry;

    protected final A serverApi;

    protected final PlayerSkillsLogger logger;

    public RestrictionsClient(Registry<R> registry, A serverApi, PlayerSkillsLogger logger) {
        this.registry = registry;
        this.serverApi = serverApi;
        this.logger = logger;
    }

    public RestrictionsClient(Registry<R> registry, A serverApi) {
        this(registry, serverApi, PlayerSkills.LOGGER);
    }

    public List<R> getAll() {
        return registry.entries();
    }
    
    protected Player getPlayer() {
        try {
            return Minecraft.getInstance().player;
        } catch (Throwable error) {
            logger.warn("Unable to get the clientside player: {}", error.getMessage());
        }

        return null;
    }

    protected List<R> getFiltered(Predicate<R> predicate) {
        var player = getPlayer();

        return player == null ? new ArrayList<>() : serverApi.getFiltered(player, predicate);
    }

    /**
     * @deprecated Use the more expressive methods available on subclasses (e.g. ItemSkills.isConsumable)
     */
    @Deprecated
    public boolean canPlayer(ResourceLocation resource, String fieldName) {
        var player = getPlayer();

        return player != null && serverApi.canPlayer(getPlayer(), resource, fieldName);
    }

    /**
     * @deprecated Use the more expressive methods available on subclasses (e.g. ItemSkills.isConsumable)
     */
    @Deprecated
    public boolean canPlayer(T target, String fieldName) {
        var player = getPlayer();

        return player != null && serverApi.canPlayer(player, target, fieldName);
    }
}
