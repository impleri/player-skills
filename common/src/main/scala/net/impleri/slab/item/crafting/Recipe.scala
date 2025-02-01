package net.impleri.slab.item.crafting

import net.impleri.playerskills.extensions.recipe.UpgradeIngredients
import net.impleri.slab.item.Item
import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.resources.ResourceWrapper
import net.minecraft.world.item.crafting.{Recipe => McRecipe}
import net.minecraft.world.Container

import scala.jdk.CollectionConverters._

case class Recipe[T <: Recipe.AnyVanilla](override val underlying: T)
    extends ResourceWrapper[T]
    with IsRecipe {
  override val name: Option[ResourceLocation] =
    Option(underlying.getId).flatMap(ResourceLocation(_))

  def getType: RecipeType.Any = RecipeType(underlying.getType)

  private def getTypeString: String = getType.name.fold("unknown")(_.path).capitalize

  def getResult: Item.VanillaStack = underlying.getResultItem

  def getIngredients: List[Item.VanillaIngredient] =
    underlying match {
      case sr: UpgradeIngredients=> sr.getRecipeIngredients.asScala.toList
      case r: Recipe.AnyVanilla => r.getIngredients.asScala.toList
    }

  override def toString: String = s"${getTypeString}Recipe[$getResultItem]{$getIngredientItems}"
}

object Recipe {
  type BaseContainer = Container
  type Vanilla[T <: BaseContainer] = McRecipe[T]
  type AnyVanilla = Vanilla[_]
  type BaseVanilla = Vanilla[BaseContainer]

  type Any = Recipe[AnyVanilla]

  def fromVanilla(underlying: AnyVanilla): Option[Any] = Option(underlying).map(Recipe(_))
}
