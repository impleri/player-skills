package net.impleri.playerskills.mixins.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(RecipeManager.class)
public class MixinRecipeManager {
  @Inject(method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;)Ljava/util/Optional;", at = @At(value = "RETURN"), cancellable = true)
  public <C extends Container, T extends Recipe<C>> void playerSkills$onGetRecipeFor(RecipeType<T> recipeType, C container, Level level, CallbackInfoReturnable<Optional<T>> cir) {
    var recipeOpt = net.impleri.slab.item.crafting.Recipe.fromVanillaOpt(cir.getReturnValue());

    if (!net.impleri.playerskills.facades.item.RecipeManagerHandler.handleOnGetRecipe(recipeOpt)) {
      cir.setReturnValue(Optional.empty());
    }
  }

  @Inject(method = "getRecipeFor(Lnet/minecraft/world/item/crafting/RecipeType;Lnet/minecraft/world/Container;Lnet/minecraft/world/level/Level;Lnet/minecraft/resources/ResourceLocation;)Ljava/util/Optional;", at = @At(value = "RETURN"), cancellable = true)
  public <C extends Container, T extends Recipe<C>> void playerSkills$onGetSpecificRecipeFor(RecipeType<T> recipeType, C container, Level level, ResourceLocation resourceLocation, CallbackInfoReturnable<Optional<Pair<ResourceLocation, T>>> cir) {
    var recipeOpt = net.impleri.slab.item.crafting.Recipe.fromVanillaPair(cir.getReturnValue());

    if (!net.impleri.playerskills.facades.item.RecipeManagerHandler.handleOnGetRecipe(recipeOpt)) {
      cir.setReturnValue(Optional.empty());
    }
  }

  @Inject(method = "getRecipesFor", at = @At(value = "RETURN"), cancellable = true)
  public <C extends Container, T extends Recipe<C>> void playerSkills$onGetRecipesFor(RecipeType<T> recipeType, C container, Level level, CallbackInfoReturnable<List<T>> cir) {
    var values = net.impleri.slab.item.crafting.Recipe.fromVanillaList(cir.getReturnValue());

    if (values.isEmpty()) {
      return;
    }

    var recipes = net.impleri.playerskills.facades.item.RecipeManagerHandler.handleOnGetRecipes(values);
    cir.setReturnValue((List<T>) recipes);
  }
}
