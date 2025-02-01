package net.impleri.playerskills.mixins.recipe;

import net.impleri.playerskills.extensions.recipe.UpgradeIngredients;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(UpgradeRecipe.class)
public class MixinUpgradeRecipe implements UpgradeIngredients {
  @Shadow
  @Final
  private Ingredient base;

  @Shadow
  @Final
  private Ingredient addition;

  @Override
  public List<Ingredient> getRecipeIngredients() {
    return List.of(base, addition);
  }
}
