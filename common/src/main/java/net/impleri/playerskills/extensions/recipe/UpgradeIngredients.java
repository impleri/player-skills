package net.impleri.playerskills.extensions.recipe;

import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public interface UpgradeIngredients {
  default List<Ingredient> getRecipeIngredients() {
    return List.of();
  }
}
