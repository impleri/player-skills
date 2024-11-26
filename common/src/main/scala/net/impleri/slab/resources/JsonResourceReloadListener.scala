package net.impleri.slab.resources

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import net.minecraft.resources.{ResourceLocation => McResourceLocation}
import net.minecraft.server.packs.resources.{ResourceManager => McResourceManager}
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
import net.minecraft.util.profiling.ProfilerFiller

import java.util
import scala.jdk.CollectionConverters._
import scala.util.chaining.scalaUtilChainingOps

abstract class JsonResourceReloadListener(
  pathName: String,
  gson: Gson = JsonResourceReloadListener.GsonService,
) extends SimpleJsonResourceReloadListener(gson, pathName)
    with ReloadListener {

  def parse(values: Map[ResourceLocation, JsonElement]): Unit

  override def apply(
    values: util.Map[McResourceLocation, JsonElement],
    resourceManager: McResourceManager,
    profilerFiller: ProfilerFiller,
  ): Unit =
    values
      .asScala
      .flatMap(t => ResourceLocation(t._1).map((_, t._2)))
      .toMap
      .pipe(parse)
}

object JsonResourceReloadListener {
  private[resources] val GsonService: Gson =
    new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
}
