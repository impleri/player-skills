package net.impleri.playerskills.mixins.item;

import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class MixinItem {
  @Inject(method = "getDescriptionId()Ljava/lang/String;", at = @At(value = "HEAD"), cancellable = true)
  private void playerSkills$getDescriptionId(CallbackInfoReturnable<String> cir) {
    var nameOpt = net.impleri.playerskills.facades.item.ClientItem.getRestrictedName((Item) ((Object) this));
    if (nameOpt.nonEmpty()) {
      cir.setReturnValue(nameOpt.get().output().getString());
    }
  }
}
