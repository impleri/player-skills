package net.impleri.fluidskills.integrations.rei;

import dev.architectury.event.EventResult;
import dev.architectury.fluid.FluidStack;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.visibility.DisplayVisibilityPredicate;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.impleri.playerskills.client.api.FluidRestrictionClient;
import net.impleri.playerskills.client.events.ClientSkillsUpdatedEvent;
import net.minecraft.world.level.material.Fluid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class FluidDisplayVisibility implements DisplayVisibilityPredicate {
    private final Map<Fluid, Boolean> producibility = new HashMap<>();
    private final Map<Fluid, Boolean> consumability = new HashMap<>();

    public FluidDisplayVisibility() {
        ClientSkillsUpdatedEvent.EVENT.register(this::clearCache);
    }

    private void clearCache(ClientSkillsUpdatedEvent clientSkillsUpdatedEvent) {
        producibility.clear();
        consumability.clear();
    }

    @Override
    public double getPriority() {
        return 100.0;
    }

    @Override
    public EventResult handleDisplay(DisplayCategory<?> category, Display display) {
        if (matchAnyIngredientInList(display.getOutputEntries(), this.hasHidden(this::isntProducible))) {
            FluidSkills.LOGGER.debug("Recipe contains an unproducible fluid, so hiding");
            return EventResult.interruptFalse();
        }

        if (matchAnyIngredientInList(display.getInputEntries(), this.hasHidden(this::isntConsumable))) {
            FluidSkills.LOGGER.debug("Recipe contains an unconsumable fluid, so hiding");
            return EventResult.interruptFalse();
        }

        return EventResult.pass();
    }

    private boolean matchAnyIngredientInList(List<EntryIngredient> entries, Predicate<EntryStack<?>> predicate) {
        return entries.stream()
                .anyMatch(entry -> entry.stream()
                        .anyMatch(predicate)
                );
    }

    /**
     * Checks every ingredient to see if any are uncraftable
     */
    private boolean isntProducible(Fluid fluid) {
        return !producibility.computeIfAbsent(fluid, FluidRestrictionClient.INSTANCE::isProducible);
    }

    private boolean isntConsumable(Fluid fluid) {
        return !consumability.computeIfAbsent(fluid, FluidRestrictionClient.INSTANCE::isConsumable);
    }

    private Predicate<EntryStack<?>> hasHidden(Predicate<Fluid> predicate) {
        return (entry) -> {
            if (entry.isEmpty()) {
                return false;
            }

            var value = entry.getValue();

            if (value instanceof Fluid fluid) {
                return predicate.test(fluid);
            }

            if (value instanceof FluidStack stack) {
                return !stack.isEmpty() && predicate.test(stack.getFluid());
            }

            return false;
        };
    }
}
