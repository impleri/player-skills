package net.impleri.playerskills.integrations.ftbquests.rewards

import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.reward.RewardType
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.playerskills.server.PlayerSkillsServer
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.integrations.ftbquests.quests.{DoubleQuest, QuestState, QuestStateOps}
import net.impleri.playerskills.skills.numeric.NumericSkillType

case class NumericSkillReward(
  q: Quest,
  override val playerOps: PlayerOps,
  override val skillOps: SkillOps,
  override val skillTypeOps: SkillTypeOps,
) extends RestrictableReward[Double](q, playerOps, skillOps, skillTypeOps)
    with DoubleQuest {
  data = QuestState(NumericSkillType.NAME)

  override def getType: RewardType = NumericSkillReward.REWARD_TYPE
}

object NumericSkillReward {
  final val REWARD_TYPE: RewardType = QuestStateOps
    .createRewardType(
      QuestStateOps.NUMERIC_SKILL,
      "minecraft:item/iron_hoe",
      apply,
    )

  def apply(quest: Quest): NumericSkillReward =
    new NumericSkillReward(
      quest,
      PlayerSkillsServer.STATE.PLAYER_OPS,
      PlayerSkills.STATE.SKILL_OPS,
      PlayerSkills.STATE.SKILL_TYPE_OPS,
    )
}
