package net.impleri.playerskills.data.utils

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.impleri.playerskills.BaseSpec
import net.impleri.playerskills.api.skills.SkillOps
import net.impleri.playerskills.data.SkillsDataLoader
import net.impleri.playerskills.utils.PlayerSkillsLogger

class JsonDataParserSpec extends BaseSpec {
  private val skillOpsMock = mock[SkillOps]
  private val loggerMock = mock[PlayerSkillsLogger]

  private val testUnit = SkillsDataLoader(skillOpsMock, loggerMock)

  "JsonDataParser.getElement" should "handles NPEs" in {
    val key = "test"
    val input = mock[JsonObject]

    input.get(key) throws new NullPointerException()

    testUnit.getElement(input, key) should be(None)

    loggerMock.info(*) wasCalled once
    loggerMock.error(*) wasCalled once
  }

  "JsonDataParser.parseValueHelper" should "handles failures" in {
    val input = mock[JsonObject]

    input.getAsBoolean throws new NullPointerException()

    testUnit.parseValueHelper(_.getAsBoolean)(input) should be(None)

    loggerMock.info(*) wasCalled once
    loggerMock.error(*) wasCalled once
  }

  "JsonDataParser.parseBoolean" should "return boolean true values" in {
    val key = "test"
    val value = true
    val input = new JsonObject()
    input.addProperty(key, value)

    testUnit.parseBoolean(input, key).value should be(value)
  }

  it should "return boolean false values" in {
    val key = "test"
    val value = false
    val input = new JsonObject()
    input.addProperty(key, value)

    testUnit.parseBoolean(input, key, None).value should be(value)
  }

  it should "return boolean default values" in {
    val key = "test"
    val input = new JsonObject()

    testUnit.parseBoolean(input, key, Option(true)).value should be(true)
  }

  "JsonDataParser.parseInt" should "return integer values" in {
    val key = "test"
    val value = 42
    val input = new JsonObject()
    input.addProperty(key, value)

    testUnit.parseInt(input, key).value should be(value)
  }

  it should "cast doubles to int values" in {
    val key = "test"
    val input = new JsonObject()
    input.addProperty(key, 15.0)

    testUnit.parseInt(input, key, None).value should be(15)
  }

  it should "return int default values" in {
    val key = "test"
    val input = new JsonObject()

    testUnit.parseInt(input, key, Option(13)).value should be(13)
  }

  "JsonDataParser.parseDouble" should "return double values" in {
    val key = "test"
    val value = 13.65
    val input = new JsonObject()
    input.addProperty(key, value)

    testUnit.parseDouble(input, key).value should be(value)
  }

  it should "cast ints to double values" in {
    val key = "test"
    val input = new JsonObject()
    input.addProperty(key, 15)

    testUnit.parseDouble(input, key, None).value should be(15.0)
  }

  it should "return double default values" in {
    val key = "test"
    val input = new JsonObject()

    testUnit.parseDouble(input, key, Option(7.54)).value should be(7.54)
  }

  "JsonDataParser.parseString" should "return string values" in {
    val key = "test"
    val value = "alpha"
    val input = new JsonObject()
    input.addProperty(key, value)

    testUnit.parseString(input, key).value should be(value)
  }

  it should "fail to cast ints to string values" in {
    val key = "test"
    val input = new JsonObject()
    input.addProperty(key, 15)

    testUnit.parseString(input, key, None) should be(None)
  }

  it should "return string default values" in {
    val key = "test"
    val input = new JsonObject()

    testUnit.parseString(input, key, Option("beta")).value should be("beta")
  }

  "JsonDataParser.parseInclude" should "return a list of string values" in {
    val value1 = "alpha"
    val value2 = "beta"
    val value = new JsonArray()
    value.add(value1)
    value.add(value2)

    val input = new JsonObject()
    input.add("include", value)

    testUnit.parseInclude(input, testUnit.castAsString) should be(List(value1, value2))
  }

  "JsonDataParser.parseExclude" should "return an empty list by default" in {
    val input = new JsonObject()

    testUnit.parseExclude(input, testUnit.castAsString).isEmpty should be(true)
  }

  "JsonDataParser.parseFacet" should "handles include and exclude parameters" in {
    val include1 = "alpha"
    val includes = new JsonArray()
    includes.add(include1)

    val exclude1 = "beta"
    val excludes = new JsonArray()
    excludes.add(exclude1)

    val input = new JsonObject()
    input.add("include", includes)
    input.add("exclude", excludes)

    val wrapper = new JsonObject()
    wrapper.add("facet", input)

    val onInclude = mock[JsonElement => Unit]
    val onExclude = mock[JsonElement => Unit]

    testUnit.parseFacet(wrapper, "facet", onInclude, onExclude)

    val alphaElement = new JsonPrimitive(include1)
    onInclude(alphaElement) wasCalled once

    val betaElement = new JsonPrimitive(exclude1)
    onExclude(betaElement) wasCalled once
  }

  it should "handle arrays as includes" in {
    val include1 = "alpha"
    val includes = new JsonArray()
    includes.add(include1)

    val wrapper = new JsonObject()
    wrapper.add("facet", includes)

    val onInclude = mock[JsonElement => Unit]
    val onExclude = mock[JsonElement => Unit]

    testUnit.parseFacet(wrapper, "facet", onInclude, onExclude)

    val alphaElement = new JsonPrimitive(include1)
    onInclude(alphaElement) wasCalled once

    onExclude(*) wasNever called
  }

  it should "does nothing for everything else" in {
    val include1 = "alpha"
    val alphaElement = new JsonPrimitive(include1)

    val wrapper = new JsonObject()
    wrapper.add("facet", alphaElement)

    val onInclude = mock[JsonElement => Unit]
    val onExclude = mock[JsonElement => Unit]

    testUnit.parseFacet(wrapper, "facet", onInclude, onExclude)

    onInclude(*) wasNever called

    onExclude(*) wasNever called
  }

  "JsonDataParser.parseOptions" should "iterate with a parser" in {
    val include1 = "alpha"
    val options = new JsonArray()
    options.add(include1)

    val wrapper = new JsonObject()
    wrapper.add("options", options)

    val onOption = mock[JsonElement => Option[Unit]]

    testUnit.parseOptions(wrapper, onOption)

    val alphaElement = new JsonPrimitive(include1)
    onOption(alphaElement) wasCalled once
  }
}
