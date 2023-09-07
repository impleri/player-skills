package net.impleri.fluidskills.integrations.rei;

import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.impleri.playerskills.client.api.FluidRestrictionClient;
import net.impleri.fluidskills.utils.ListDiff;
import net.impleri.playerskills.client.events.ClientSkillsUpdatedEvent;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FluidSkillsFiltering {
    private static BasicFilteringRule.MarkDirty filteringRule;

    private static final List<Fluid> currentlyFiltered = new ArrayList<>();

    public static void updateHidden(ClientSkillsUpdatedEvent event) {
        var nextHidden = FluidRestrictionClient.INSTANCE.getHidden();

        // Nothing on either list, so don't bother
        if (currentlyFiltered.size() == 0 && nextHidden.size() == 0) {
            FluidSkills.LOGGER.debug("No changes in restrictions");
            return;
        }

        var toHide = !ListDiff.contains(currentlyFiltered, nextHidden);
        var toReveal = !ListDiff.contains(nextHidden, currentlyFiltered);


        if ((toHide || toReveal) && filteringRule != null) {
            FluidSkills.LOGGER.debug("Updating REI filters");
            // Update what we're supposed
            currentlyFiltered.clear();
            currentlyFiltered.addAll(nextHidden);

            // Trigger re-filtering REI entry stacks
            filteringRule.markDirty();
        }

    }

    public static void register(BasicFilteringRule<?> rule) {
        filteringRule = rule.hide(() -> FluidRestrictionClient.INSTANCE.getHidden().stream()
                .map(fluid -> EntryStacks.of(fluid).cast())
                .collect(Collectors.toList()));
    }
}
