package net.impleri.fluidskills.integrations.kubejs.events;

import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.impleri.fluidskills.FluidHelper;
import net.impleri.playerskills.restrictions.fluids.FluidRestriction;
import net.impleri.playerskills.restrictions.AbstractRegistrationEventJS;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class RestrictionsRegistrationEventJS extends AbstractRegistrationEventJS<Fluid, FluidRestriction, RestrictionJS.Builder> {
    @HideFromJS
    public RestrictionsRegistrationEventJS(MinecraftServer s) {
        super(s, "fluid", Registry.FLUID);
    }

    @Override
    @HideFromJS
    public void restrictOne(ResourceLocation name, @NotNull Consumer<RestrictionJS.Builder> consumer) {
        var builder = new RestrictionJS.Builder(name, server);
        consumer.accept(builder);

        var fluid = FluidHelper.getFluid(name);
        if (FluidHelper.isEmptyFluid(fluid)) {
            ConsoleJS.SERVER.warn("Could not find any fluid named " + name);
            return;
        }

        var restriction = builder.createObject(fluid);
        FluidSkills.RESTRICTIONS.add(name, restriction);

        logRestrictionCreation(restriction, name);
    }

    @Override
    @HideFromJS
    public Predicate<Fluid> isTagged(TagKey<Fluid> tag) {
        return fluid -> fluid.is(tag);
    }

    @Override
    @HideFromJS
    public ResourceLocation getName(Fluid resource) {
        return FluidHelper.getFluidName(resource);
    }
}
