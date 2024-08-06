package net.impleri.slab.client.events

import dev.architectury.event.Event
import dev.architectury.event.events.client.ClientRecipeUpdateEvent
import net.impleri.slab.item.crafting.RecipeManager

case class RecipeEvents(
  private val onRecipeUpdate: Event[ClientRecipeUpdateEvent] =
    ClientRecipeUpdateEvent.EVENT,
) {
  def onUpdate(f: RecipeManager => Unit): Unit = {
    onRecipeUpdate.register { (recipeManager: RecipeManager.Vanilla) =>
      {
        Option(recipeManager).map(RecipeManager(_)).foreach(f)
      }
    }
  }
}
