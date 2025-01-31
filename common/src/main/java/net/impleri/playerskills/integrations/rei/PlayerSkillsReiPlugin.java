package net.impleri.playerskills.integrations.rei;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import net.impleri.playerskills.PlayerSkills;
import net.impleri.playerskills.client.PlayerSkillsClient;
import net.impleri.playerskills.client.restrictions.RecipeRestrictionOpsClient;
import net.impleri.playerskills.restrictions.RestrictionRegistry;
import net.impleri.playerskills.utils.PlayerSkillsLogger;
import org.jetbrains.annotations.NotNull;

/**
 * This is necessary because the REIPlugin::compareTo function uses a raw type and scala does not like it.
 */
public class PlayerSkillsReiPlugin implements REIClientPlugin {
  private final @NotNull RestrictionRegistry restrictions;
  private final @NotNull RecipeRestrictionOpsClient recipeOps;

  public PlayerSkillsReiPlugin() {
    restrictions = PlayerSkills.STATE().RESTRICTIONS();
    recipeOps = PlayerSkillsClient.STATE().RECIPE_RESTRICTIONS();
  }

  @Override
  public void registerDisplays(DisplayRegistry registry) {
    registry.registerVisibilityPredicate(RecipeRestrictionsPredicate.apply(registry, restrictions, recipeOps, PlayerSkillsLogger.RECIPES()));
  }
}
