package net.impleri.playerskills.integrations.ftbquests.rewards

import dev.ftb.mods.ftbquests.quest.Quest
import dev.ftb.mods.ftbquests.quest.reward.RewardType
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.server.api.{Player => PlayerOps}
import net.impleri.playerskills.server.PlayerSkillsServer
import net.impleri.playerskills.PlayerSkills
import net.impleri.playerskills.api.skills.SkillTypeOps
import net.impleri.playerskills.integrations.ftbquests.helpers.QuestState
import net.impleri.playerskills.integrations.ftbquests.helpers.QuestStateOps
import net.impleri.playerskills.integrations.ftbquests.helpers.StringQuest
import net.impleri.playerskills.skills.tiered.TieredSkillType

case class TieredSkillReward(
  q: Quest,
  override val playerOps: PlayerOps,
  override val skillOps: SkillOps,
  override val skillTypeOps: SkillTypeOps,
) extends RestrictableReward[String](q, playerOps, skillOps, skillTypeOps)
    with StringQuest {
  data = QuestState(TieredSkillType.NAME)

  override def getType: RewardType = TieredSkillReward.REWARD_TYPE
}

object TieredSkillReward {
  val REWARD_TYPE: RewardType = QuestStateOps
    .createRewardType(
      QuestStateOps.TIERED_SKILL,
      "minecraft:item/golden_hoe",
      apply,
    )

  def apply(quest: Quest): TieredSkillReward = {
    new TieredSkillReward(
      quest,
      PlayerSkillsServer.STATE.PLAYER_OPS,
      PlayerSkills.STATE.SKILL_OPS,
      PlayerSkills.STATE.SKILL_TYPE_OPS,
    )
  }
}
