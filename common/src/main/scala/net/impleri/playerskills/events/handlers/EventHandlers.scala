package net.impleri.playerskills.events.handlers

import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.events.SkillChangedEvent
import net.impleri.playerskills.facades.MinecraftPlayer
import net.impleri.playerskills.facades.MinecraftServer
import net.impleri.playerskills.server.NetHandler
import net.minecraft.server.packs.resources.ResourceManager

import java.util.UUID

case class EventHandlers(
  netHandler: NetHandler,
  onSetup: () => Unit,
  onServerChange: Option[MinecraftServer] => Unit,
  onReloadResources: ResourceManager => Unit,
) {
  private val INTERNAL = InternalEvents(onReloadResources)
  private val COMMAND = CommandEvents()
  private val LIFECYCLE = LifecycleEvents(onSetup, onServerChange)

  def init(): Unit = {
    LIFECYCLE.registerEvents()
    COMMAND.registerEvents()
    INTERNAL.registerEvents()
  }

  def emitSkillChanged[T](player: MinecraftPlayer[_], newSkill: Skill[T], oldSkill: Option[Skill[T]]): Unit = {
    SkillChangedEvent.EVENT.invoker().accept(SkillChangedEvent[T](player, Option(newSkill), oldSkill))

    newSkill.getNotification(oldSkill.flatMap(_.value))
      .foreach(player.sendMessage(_))
  }

  def resync(playerId: UUID, server: MinecraftServer): Unit = {
    server.getPlayer(playerId)
      .foreach(netHandler.syncPlayer(_))
  }
}


object EventHandlers {
  private def noOp = () => ()

  private def noOp1[T] = (_: T) => ()

  def apply(
    netHandler: NetHandler = NetHandler(),
    onSetup: () => Unit = noOp,
    onServerChange: Option[MinecraftServer] => Unit = noOp1,
    onReloadResources: ResourceManager => Unit = noOp1,
  ): EventHandlers = {
    new EventHandlers(netHandler, onSetup, onServerChange, onReloadResources)
  }
}
