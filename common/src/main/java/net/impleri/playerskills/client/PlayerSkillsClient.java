package net.impleri.playerskills.client;

import com.google.common.collect.ImmutableList;
import dev.architectury.registry.ReloadListenerRegistry;
import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.api.Skill;
import net.impleri.playerskills.client.events.ClientSkillsUpdatedEvent;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public final class PlayerSkillsClient implements ResourceManagerReloadListener {
    private static final PlayerSkillsClient INSTANCE = new PlayerSkillsClient();

    public static void init() {
        INSTANCE.registerEvents();
        PlayerSkills.LOGGER.info("PlayerSkills Client started");
    }

    /**
     * Request Server to resend skills
     */
    public static void resyncSkills() {
        NetHandler.resyncPlayer();
    }

    /**
     * Handle updated skills from server
     */
    @ApiStatus.Internal
    public static void syncFromServer(ImmutableList<Skill<?>> skills, boolean force) {
        Registry.syncFromServer(skills, force);
    }

    /**
     * Broadcast client-side event that skills have changed
     */
    @ApiStatus.Internal
    public static void emitSkillsUpdated(ImmutableList<Skill<?>> skills, ImmutableList<Skill<?>> prev, boolean force) {
        ClientSkillsUpdatedEvent.EVENT.invoker().accept(new ClientSkillsUpdatedEvent(skills, prev, force));
    }

    private void registerEvents() {
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES, this);
    }

    @Override
    public void onResourceManagerReload(@NotNull ResourceManager resourceManager) {
        PlayerSkillsClient.resyncSkills();
    }
}
