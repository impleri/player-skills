package net.impleri.playerskills.integrations.ftbquests.tasks

import dev.ftb.mods.ftblibrary.icon.Icon
import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.task.TaskType
import dev.ftb.mods.ftbquests.quest.task.TaskTypes
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.playerskills.server.PlayerSkillsServer
import net.impleri.playerskills.skills.basic.BasicSkillType
import net.impleri.playerskills.utils.SkillResourceLocation
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.integrations.ftbquests.helpers.BooleanValueHandling
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

case class BasicSkillTask(
  q: Quest,
  override val playerOps: PlayerOps,
  override val skillOps: SkillOps,
) extends SkillTask[Boolean](q, playerOps, skillOps) with BooleanValueHandling {
  override val skillType: ResourceLocation = BasicSkillType.NAME

  override def getType: TaskType = BasicSkillTask.TASK_TYPE
}

object BasicSkillTask {
  val TASK_TYPE: TaskType = TaskTypes.register(
    SkillResourceLocation.of("basic_skill_task").get,
    apply,
    () => Icon.getIcon("minecraft:item/wooden_shovel"),
  )

  TASK_TYPE.setDisplayName(Component.translatable("playerskills.quests.basic_skill"))

  def apply(quest: Quest): BasicSkillTask = {
    new BasicSkillTask(
      quest,
      PlayerSkillsServer.STATE.PLAYER_OPS,
      PlayerSkills.STATE.SKILL_OPS,
    )
  }
}
