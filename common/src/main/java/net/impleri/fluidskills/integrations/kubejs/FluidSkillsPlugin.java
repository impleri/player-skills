package net.impleri.fluidskills.integrations.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import net.impleri.fluidskills.integrations.kubejs.events.EventsBinding;
import net.impleri.fluidskills.integrations.kubejs.events.RestrictionsRegistrationEventJS;
import net.minecraft.server.MinecraftServer;

public class FluidSkillsPlugin extends KubeJSPlugin {
    @Override
    public void registerEvents() {
        EventsBinding.GROUP.register();
    }

    public static void onStartup(MinecraftServer minecraftServer) {
        EventsBinding.RESTRICTIONS.post(new RestrictionsRegistrationEventJS(minecraftServer));
    }
}
