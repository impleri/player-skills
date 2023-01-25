package net.impleri.playerskills.restrictions;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public abstract class AbstractRestriction<Target> {
    public final Target target;
    public final Predicate<Player> condition;
    public final Target replacement;


    public AbstractRestriction(
            Target target,
            @Nullable Predicate<Player> condition,
            @Nullable Target replacement
    ) {
        this.target = target;
        this.condition = condition;
        this.replacement = replacement;
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
