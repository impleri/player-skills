package net.impleri.playerskills.integrations.ftbquests.rewards

import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.reward.RewardType
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.playerskills.server.PlayerSkillsServer
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.integrations.ftbquests.helpers.BooleanQuest
import net.impleri.playerskills.integrations.ftbquests.helpers.QuestStateOps

case class BasicSkillReward(
  q: Quest,
  override val playerOps: PlayerOps,
  override val skillOps: SkillOps,
  override val skillTypeOps: SkillTypeOps,
) extends SkillReward[Boolean](q, playerOps, skillOps, skillTypeOps) with BooleanQuest {
  override def getType: RewardType = BasicSkillReward.REWARD_TYPE
}

object BasicSkillReward {
  val REWARD_TYPE: RewardType = QuestStateOps
    .createRewardType(QuestStateOps.BASIC_SKILL, "minecraft:item/wooden_hoe", apply)

  def apply(quest: Quest): BasicSkillReward = {
    new BasicSkillReward(
      quest,
      PlayerSkillsServer.STATE.PLAYER_OPS,
      PlayerSkills.STATE.SKILL_OPS,
      PlayerSkills.STATE.SKILL_TYPE_OPS,
    )
  }
}
