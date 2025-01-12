package net.impleri.slab.events

import dev.architectury.event.Event
import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.event.CompoundEventResult
import dev.architectury.event.EventResult
import net.impleri.slab.advancements.Award
import net.impleri.slab.entity.Entity
import net.impleri.slab.entity.Hand
import net.impleri.slab.entity.Player
import net.impleri.slab.item.Item
import net.impleri.slab.resources.ResourceLocation
import net.impleri.slab.world.Level
import net.minecraft.advancements.Advancement
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.entity.{Entity => McEntity}
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.{Player => McPlayer}
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.{Level => McLevel}
import net.minecraft.world.phys.HitResult
import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.EntityHitResult

case class PlayerEvents(
  private val onJoinEvent: Event[PlayerEvent.PlayerJoin] =
    PlayerEvent.PLAYER_JOIN,
  private val onQuitEvent: Event[PlayerEvent.PlayerQuit] =
    PlayerEvent.PLAYER_QUIT,
  private val onRespawnEvent: Event[PlayerEvent.PlayerRespawn] =
    PlayerEvent.PLAYER_RESPAWN,
  private val onCloneEvent: Event[PlayerEvent.PlayerClone] =
    PlayerEvent.PLAYER_CLONE,
  private val onAwardEvent: Event[PlayerEvent.PlayerAdvancement] =
    PlayerEvent.PLAYER_ADVANCEMENT,
  private val afterCraftEvent: Event[PlayerEvent.CraftItem] =
    PlayerEvent.CRAFT_ITEM,
  private val afterSmeltEvent: Event[PlayerEvent.SmeltItem] =
    PlayerEvent.SMELT_ITEM,
  private val canPickupEvent: Event[PlayerEvent.PickupItemPredicate] =
    PlayerEvent.PICKUP_ITEM_PRE,
  private val onPickupEvent: Event[PlayerEvent.PickupItem] =
    PlayerEvent.PICKUP_ITEM_POST,
  private val onChangeDimensionEvent: Event[PlayerEvent.ChangeDimension] =
    PlayerEvent.CHANGE_DIMENSION,
  private val onDropEvent: Event[PlayerEvent.DropItem] = PlayerEvent.DROP_ITEM,
  //  onOpenMenuEvent: Event[PlayerEvent.OpenMenu] = PlayerEvent.OPEN_MENU,
  //  onCloseMenuEvent: Event[PlayerEvent.CloseMenu] = PlayerEvent.CLOSE_MENU,
  private val onFillBucketEvent: Event[PlayerEvent.FillBucket] =
    PlayerEvent.FILL_BUCKET,
  private val onAttackEvent: Event[PlayerEvent.AttackEntity] =
    PlayerEvent.ATTACK_ENTITY,
) extends ResultHandler {
  def onJoin(handler: PlayerEvents.OnJoinOrQuit): Unit =
    onJoinEvent.register((rawPlayer: ServerPlayer) =>
      for {
        player <- Option(rawPlayer).map(Player(_))
      } yield handler(player)
    )

  def onQuit(handler: PlayerEvents.OnJoinOrQuit): Unit =
    onQuitEvent.register((rawPlayer: ServerPlayer) =>
      for {
        player <- Option(rawPlayer).map(Player(_))
      } yield handler(player)
    )

  def onRespawn(handler: PlayerEvents.OnRespawn): Unit =
    onRespawnEvent
      .register((rawPlayer: ServerPlayer, wonGame: Boolean) =>
        for {
        player <- Option(rawPlayer).map(Player(_))
      } yield handler(player, wonGame)
      )

  def onClone(handler: PlayerEvents.OnClone): Unit =
    onCloneEvent.register {
      (original: ServerPlayer, player: ServerPlayer, wonGame: Boolean) =>
        for {
        oldPlayer <- Option(original).map(Player(_))
        newPlayer = Option(player).map(Player(_))
      } yield handler(oldPlayer, newPlayer, wonGame)
    }

  def onAward(handler: PlayerEvents.OnAward): Unit =
    onAwardEvent.register { (rawPlayer: ServerPlayer, advancement: Advancement) =>
      for {
        player <- Option(rawPlayer).map(Player(_))
        award = Option(advancement).map(Award(_))
      } yield handler(player, award)
    }

  def afterCraft(handler: PlayerEvents.AfterCraft): Unit =
    // TODO: Wrap Container and pass it into handler
    afterCraftEvent.register {
      (rawPlayer: McPlayer, rawItem: ItemStack, _: Container) =>
        for {
        player <- Option(rawPlayer).map(Player(_))
        item = Option(rawItem).map(Item(_))
      } yield handler(player, item)
    }

  def afterSmelt(handler: PlayerEvents.AfterSmelt): Unit =
    afterSmeltEvent.register { (rawPlayer: McPlayer, rawItem: ItemStack) =>
      for {
        player <- Option(rawPlayer).map(Player(_))
        item = Option(rawItem).map(Item(_))
      } yield handler(player, item)
    }

  def canPickup(handler: PlayerEvents.CanPickup): Unit =
    canPickupEvent.register {
      (rawPlayer: McPlayer, entity: ItemEntity, rawItem: ItemStack) =>
        ensureResult {
          for {
            player <- Option(rawPlayer).map(Player(_))
            item = Option(rawItem).map(Item(_))
            itemEntity = Option(entity).map(Entity(_))
          } yield handler(player, item, itemEntity)
        }
    }

  def onPickup(handler: PlayerEvents.OnPickup): Unit =
    onPickupEvent.register {
      (rawPlayer: McPlayer, entity: ItemEntity, rawItem: ItemStack) =>
        for {
            player <- Option(rawPlayer).map(Player(_))
            item = Option(rawItem).map(Item(_))
            itemEntity = Option(entity).map(Entity(_))
          } yield handler(player, item, itemEntity)
    }

  def onChangeDimension(handler: PlayerEvents.OnChangeDimension): Unit =
    onChangeDimensionEvent
      .register {
        (
          rawPlayer: ServerPlayer,
          oldLevel: ResourceKey[McLevel],
          newLevel: ResourceKey[McLevel],
        ) =>
          for {
            player <- Option(rawPlayer).map(Player(_))
            origin = Option(oldLevel).map(_.location()).flatMap(ResourceLocation(_))
            destination = Option(newLevel).map(_.location()).flatMap(ResourceLocation(_))
          } yield handler(player, destination, origin)
      }

  def onDrop(handler: PlayerEvents.OnDrop): Unit =
    onDropEvent.register { (rawPlayer: McPlayer, item: ItemEntity) =>
      ensureResult {
        for {
          player <- Option(rawPlayer).map(Player(_))
          itemEntity = Option(item).map(Entity(_))
        } yield handler(player, itemEntity)
      }
    }

  //  def onOpenMenu(): Unit = ???

  //  def onCloseMenu(): Unit = ???

  def onFillBucket(handler: PlayerEvents.OnFillBucket): Unit =
    // TODO: Incorporate HitResult into callback
    onFillBucketEvent.register {
      (rawPlayer: McPlayer, rawLevel: McLevel, rawItem: ItemStack, _: HitResult) =>
        ensureCompoundResult[ItemStack] {
          for {
            player <- Option(rawPlayer).map(Player(_))
            level = Option(rawLevel).map(Level(_))
            item = Option(rawItem).map(Item(_))
          } yield handler(player, level, item)
        }
    }

  def onAttack(handler: PlayerEvents.OnAttack): Unit =
    // TODO: Incorporate EntityHitResult into callback
    onAttackEvent
      .register {
        (
          rawPlayer: McPlayer,
          rawLevel: McLevel,
          rawEntity: McEntity,
          rawHand: InteractionHand,
          _: EntityHitResult,
        ) =>
          ensureResult {
            for {
              player <- Option(rawPlayer).map(Player(_))
              level = Option(rawLevel).map(Level(_))
              entity = Option(rawEntity).map(Entity(_))
              hand = Hand.fromVanilla(rawHand)
            } yield handler(player, entity, level, hand)
          }
      }
}

object PlayerEvents {
  type OnJoinOrQuit = Player => Unit
  type OnRespawn = (Player, Boolean) => Unit
  type OnClone = (Player, Option[Player], Boolean) => Unit
  type OnAward = (Player, Option[Award]) => Unit
  type AfterCraft = (Player, Option[Item]) => Unit
  type AfterSmelt = (Player, Option[Item]) => Unit
  type CanPickup = (Player, Option[Item], Option[Entity.Any]) => EventResult
  type OnPickup = (Player, Option[Item], Option[Entity.Any]) => Unit
  type OnChangeDimension =
    (Player, Option[ResourceLocation], Option[ResourceLocation]) => Unit
  type OnDrop = (Player, Option[Entity.Any]) => EventResult
  type OnFillBucket = (
    Player,
    Option[Level.Any],
    Option[Item],
  ) => CompoundEventResult[ItemStack]
  type OnAttack = (
    Player,
    Option[Entity.Any],
    Option[Level.Any],
    Hand.Hand,
  ) => EventResult
}
