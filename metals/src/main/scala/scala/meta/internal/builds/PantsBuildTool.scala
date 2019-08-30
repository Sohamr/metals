package scala.meta.internal.builds

import scala.meta.io.AbsolutePath
import scala.meta.internal.metals.{MetalsServerConfig, UserConfiguration}

case class PantsBuildTool() extends BuildTool {

  override def toString(): String = "pants"
  def version: String = "1.0.0"
  def minimumVersion: String = "1.0.0"
  def recommendedVersion: String = "1.0.0"
  def executableName: String = "pants"
  def digest(
      workspace: AbsolutePath,
      userConfig: UserConfiguration
  ): Option[String] = {
    PantsDigest.current(workspace, userConfig)
  }
  def args(
      workspace: AbsolutePath,
      userConfig: () => UserConfiguration,
      config: MetalsServerConfig
  ): List[String] = {

    List(
      workspace.resolve("pants").toString(),
      "--pants-config-files=pants.ini.scalameta",
      "compile.rsc",
      "--empty-compilation",
      "--cache-ignore",
      "--no-use-classpath-jars",
      "bloop.bloop-export-config",
      "--sources",
      "bloop.bloop-gen",
      "--execution-strategy=subprocess",
      userConfig().pantsTargets.getOrElse("::/")
    )

  }
}

object PantsBuildTool {
  def isPantsRelatedPath(workspace: AbsolutePath, path: AbsolutePath) = {
    path.toNIO.endsWith("BUILD")
  }
}
