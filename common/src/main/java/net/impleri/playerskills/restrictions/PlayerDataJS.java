package net.impleri.playerskills.restrictions;

import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Use net.impleri.playerskills.integration.kubejs.api.PlayerDataJS
 */
@Deprecated()
public class PlayerDataJS extends net.impleri.playerskills.integration.kubejs.api.PlayerDataJS {
    public PlayerDataJS(@NotNull Player player) {
        super(player);
    }
}
