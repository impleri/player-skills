package net.impleri.playerskills.api.skills

import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component

trait TranslatableSkill[T] extends SkillData[T] {
  def announceChange: Boolean = false
  def notifyKey: Option[String] = None

  protected def getMessageKey: String = "playerskills.notify.skill_change"

  protected def formatSkillName(): Component =
    Component.literal(name.getPath.replace("_", " "))
      .withStyle(ChatFormatting.DARK_AQUA)
      .withStyle(ChatFormatting.BOLD)

  protected def formatSkillValue(value: Option[T]): Component =
    Component.literal(value.map(v => s"$v").getOrElse(""))
      .withStyle(ChatFormatting.GOLD)

  protected def formatSkillValue(): Component = formatSkillValue(value)

  protected def formatNotificationMessage(messageKey: String, oldValue: Option[T] = None): Component =
    Component.translatable(messageKey, formatSkillName(), formatSkillValue(), formatSkillValue(oldValue))

  protected def formatNotification(oldValue: Option[T] = None): Component =
    formatNotificationMessage(notifyKey.getOrElse(getMessageKey), oldValue)

  def getNotification(oldValue: Option[T] = None): Option[Component] =
    if (!announceChange) None else value.map(_ => formatNotification(oldValue))
}
