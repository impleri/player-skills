package net.impleri.playerskills.mixins.recipe;

import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeBook.class)
public class MixinRecipeBook {
  @Inject(method = "contains(Lnet/minecraft/world/item/crafting/Recipe;)Z", at = @At("HEAD"), cancellable = true)
  private void playerSkills$onContains(Recipe<?> recipe, CallbackInfoReturnable<Boolean> cir) {
    if (!net.impleri.playerskills.facades.recipe.ClientRecipeHandler.isRecipeProducible(recipe)) {
      cir.setReturnValue(false);
    }
  }
}
