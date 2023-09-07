package net.impleri.fluidskills.integrations.rei;

import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import net.impleri.playerskills.client.events.ClientSkillsUpdatedEvent;

public class FluidSkillsReiPlugin implements REIClientPlugin {
    public FluidSkillsReiPlugin() {
        // Subscribe to client skill updates to refresh basic filtering rules
        ClientSkillsUpdatedEvent.EVENT.register(FluidSkillsFiltering::updateHidden);
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        registry.registerVisibilityPredicate(new FluidDisplayVisibility());
    }

    @Override
    public void registerBasicEntryFiltering(BasicFilteringRule<?> rule) {
        FluidSkillsFiltering.register(rule);
    }
}
