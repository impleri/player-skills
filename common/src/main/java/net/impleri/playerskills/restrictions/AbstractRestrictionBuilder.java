package net.impleri.playerskills.restrictions;

import dev.latvian.mods.kubejs.BuilderBase;
import dev.latvian.mods.rhino.util.HideFromJS;
import dev.latvian.mods.rhino.util.RemapForJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

public abstract class AbstractRestrictionBuilder<T extends AbstractRestriction<?>> extends BuilderBase<T> {
    @HideFromJS
    public Predicate<Player> condition = (Player player) -> true;

    @HideFromJS
    public AbstractRestrictionBuilder(ResourceLocation id) {
        super(id);
    }

    @RemapForJS("if")
    public AbstractRestrictionBuilder<T> condition(Predicate<PlayerDataJS> consumer) {
        this.condition = (Player player) -> player != null && consumer.test(new PlayerDataJS(player));

        return this;
    }

    public AbstractRestrictionBuilder<T> unless(Predicate<PlayerDataJS> consumer) {
        this.condition = (Player player) -> player == null || !consumer.test(new PlayerDataJS(player));

        return this;
    }
}
