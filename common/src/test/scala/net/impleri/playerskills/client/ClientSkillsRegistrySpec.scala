package net.impleri.playerskills.client

import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.Skill
import net.impleri.playerskills.facades.minecraft.core.ResourceLocation
import net.impleri.playerskills.utils.PlayerSkillsLogger

class ClientSkillsRegistrySpec extends BaseSpec {
  private val eventHandlerMock = mock[EventHandler]
  private val loggerMock = mock[PlayerSkillsLogger]

  private val testUnit = ClientSkillsRegistry(eventHandlerMock, loggerMock)

  "ClientSkillsRegistry.syncFromServer" should "update stored skills" in {
    val forced = false

    val skill1 = mock[Skill[Boolean]]
    skill1.name returns ResourceLocation("skillstest", "skill").get
    skill1.value returns None

    val givenSkills = List(
      skill1,
    )
    testUnit.get.isEmpty should be(true)

    testUnit.syncFromServer(givenSkills, forced)

    loggerMock.info(*) wasCalled once

    eventHandlerMock.emitSkillsUpdated(givenSkills, List.empty, forced) wasCalled once
  }
}
