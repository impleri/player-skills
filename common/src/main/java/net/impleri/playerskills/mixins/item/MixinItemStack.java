package net.impleri.playerskills.mixins.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
  @Shadow
  public abstract Item getItem();

  @Inject(method = "getTooltipLines", at = @At(value = "RETURN"), cancellable = true)
  private void playerSkills$getTooltipLines(@Nullable Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir) {
    var item = net.impleri.slab.item.Item.fromVanilla(getItem());
    var playerOpt = net.impleri.slab.entity.Player.fromVanilla(player);
    var replacement = net.impleri.playerskills.facades.item.ItemStack.handleGetTooltipLines(playerOpt, item);

    if (replacement.nonEmpty()) {
      cir.setReturnValue(replacement.get());
    }
  }
}
