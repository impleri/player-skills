package net.impleri.playerskills.restrictions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class AbstractRestriction<Target> {
    private static final Predicate<Player> DEFAULT_CONDITION = (Player player) -> true;
    public final Target target;
    public final Target replacement;

    public final @NotNull Predicate<Player> condition;

    public final List<ResourceLocation> includeDimensions;
    public final List<ResourceLocation> excludeDimensions;

    public final List<ResourceLocation> includeBiomes;
    public final List<ResourceLocation> excludeBiomes;


    public AbstractRestriction(
            Target target,
            @Nullable Predicate<Player> condition,
            @Nullable List<ResourceLocation> includeDimensions,
            @Nullable List<ResourceLocation> excludeDimensions,
            @Nullable List<ResourceLocation> includeBiomes,
            @Nullable List<ResourceLocation> excludeBiomes,
            @Nullable Target replacement
    ) {
        this.target = target;
        this.replacement = replacement;
        this.condition = (condition != null) ? condition : DEFAULT_CONDITION;
        this.includeDimensions = (includeDimensions != null) ? includeDimensions : new ArrayList<>();
        this.excludeDimensions = (excludeDimensions != null) ? excludeDimensions : new ArrayList<>();
        this.includeBiomes = (includeBiomes != null) ? includeBiomes : new ArrayList<>();
        this.excludeBiomes = (excludeBiomes != null) ? excludeBiomes : new ArrayList<>();
    }

    public AbstractRestriction(
            Target target,
            @Nullable Predicate<Player> condition,
            @Nullable Target replacement
    ) {
        this(target, condition, null, null, null, null, replacement);
    }
}
