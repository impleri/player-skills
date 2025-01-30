package net.impleri.playerskills.mixins.recipe;

import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RecipeCollection.class)
public class MixinRecipeCollection {
  @Inject(method = "isCraftable", at = @At("HEAD"), cancellable = true)
  private void playerSkills$isCraftable(Recipe<?> recipe, CallbackInfoReturnable<Boolean> cir) {
    if (!net.impleri.playerskills.facades.recipe.ClientRecipeHandler.isRecipeProducible(recipe)) {
      cir.setReturnValue(false);
    }
  }

  @Inject(method = "getRecipes()Ljava/util/List;", at = @At("RETURN"), cancellable = true)
  private void playerSkills$onGetRecipes(CallbackInfoReturnable<List<Recipe<?>>> cir) {
    var recipes = net.impleri.playerskills.facades.recipe.ClientRecipeHandler.handleOnGetRecipes(cir.getReturnValue());
    cir.setReturnValue(recipes);
  }

  @Inject(method = "getRecipes(Z)Ljava/util/List;", at = @At("RETURN"), cancellable = true)
  private void playerSkills$onGetDimensionRecipes(CallbackInfoReturnable<List<Recipe<?>>> cir) {
    var recipes = net.impleri.playerskills.facades.recipe.ClientRecipeHandler.handleOnGetRecipes(cir.getReturnValue());
    cir.setReturnValue(recipes);
  }

  @Inject(method = "getDisplayRecipes", at = @At("RETURN"), cancellable = true)
  private void playerSkills$onGetDisplayRecipes(CallbackInfoReturnable<List<Recipe<?>>> cir) {
    var recipes = net.impleri.playerskills.facades.recipe.ClientRecipeHandler.handleOnGetRecipes(cir.getReturnValue());
    cir.setReturnValue(recipes);
  }
}
