package net.impleri.playerskills.restrictions;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public abstract class AbstractRestriction<Target> {
    private static final Predicate<Player> DEFAULT_CONDITION = (Player player) -> true;
    public final Target target;
    public final @NotNull Predicate<Player> condition;
    public final Target replacement;


    public AbstractRestriction(
            Target target,
            @Nullable Predicate<Player> condition,
            @Nullable Target replacement
    ) {
        this.target = target;
        this.replacement = replacement;
        this.condition = (condition != null) ? condition : DEFAULT_CONDITION;
    }

    public AbstractRestriction(
            Target target,
            @NotNull Predicate<Player> condition
    ) {
        this(target, condition, null);
    }

    public AbstractRestriction(
            Target target,
            @NotNull Target replacement
    ) {
        this(target, null, replacement);
    }
}
