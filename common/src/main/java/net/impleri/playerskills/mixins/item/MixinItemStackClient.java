package net.impleri.playerskills.mixins.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MixinItemStackClient {
  @Shadow
  public abstract Item getItem();

  @Inject(method = "getHoverName", at = @At(value = "RETURN"), cancellable = true)
  private void playerSkills$getHoverName(CallbackInfoReturnable<Component> cir) {
    var item = net.impleri.slab.item.Item.fromVanilla(getItem());
    var nameOpt = net.impleri.playerskills.facades.item.ItemStackClient.handleGetHoverName(item);
    if (nameOpt.nonEmpty()) {
      cir.setReturnValue(nameOpt.get().output());
    }
  }

  @Inject(method = "getDescriptionId", at = @At(value = "RETURN"), cancellable = true)
  private void playerSkills$getDescriptionId(CallbackInfoReturnable<String> cir) {
    var item = net.impleri.slab.item.Item.fromVanilla(getItem());
    var nameOpt = net.impleri.playerskills.facades.item.ItemStackClient.handleGetDescriptionId(item);
    if (nameOpt.nonEmpty()) {
      cir.setReturnValue(nameOpt.get());
    }
  }

  @Inject(method = "getDisplayName", at = @At(value = "RETURN"), cancellable = true)
  private void playerSkills$getDisplayName(CallbackInfoReturnable<Component> cir) {
    var item = net.impleri.slab.item.Item.fromVanilla(getItem());
    var nameOpt = net.impleri.playerskills.facades.item.ItemStackClient.handleGetHoverName(item);
    if (nameOpt.nonEmpty()) {
      cir.setReturnValue(nameOpt.get().output());
    }
  }
}
