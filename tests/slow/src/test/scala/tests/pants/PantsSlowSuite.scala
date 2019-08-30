package tests.pants

import scala.meta.io.AbsolutePath

import tests.BaseImportSuite
import scala.meta.internal.builds.PantsBuildTool
import scala.meta.internal.builds.PantsDigest

object PantsSlowSuite extends BaseImportSuite("pants") {
  val buildTool = PantsBuildTool()

  override def currentDigest(
      workspace: AbsolutePath
  ): Option[String] = PantsDigest.current(workspace, userConfig)

  // beforeAll()
  testAsync("basic") {
    //wor
    cleanWorkspace()
    for {
      _ <- server.initialize(
        """|/build.gradle
           |plugins {
           |    id 'scala'
           |}
           |repositories {
           |    mavenCentral()
           |}
           |dependencies {
           |    implementation 'org.scala-lang:scala-library:2.12.9'
           |}
           |""".stripMargin
      )
      _ = assertNoDiff(
        client.workspaceMessageRequests,
        List(
          // Project has no .bloop directory so user is asked to "import via bloop"
          importBuildMessage,
          progressMessage
        ).mkString("\n")
      )
      _ = client.messageRequests.clear() // restart
      _ = assertStatus(_.isInstalled)
      _ <- server.didChange("build.gradle")(_ + "\n// comment")
      _ = assertNoDiff(client.workspaceMessageRequests, "")
      _ <- server.didSave("build.gradle")(identity)
      // Comment changes do not trigger "re-import project" request
      _ = assertNoDiff(client.workspaceMessageRequests, "")
      _ <- server.didChange("build.gradle") { text =>
        text + "\ndef version = \"1.0.0\"\n"
      }
      _ = assertNoDiff(client.workspaceMessageRequests, "")
      _ <- server.didSave("build.gradle")(identity)
    } yield {
      assertNoDiff(
        client.workspaceMessageRequests,
        List(
          // Project has .bloop directory so user is asked to "re-import project"
          importBuildChangesMessage,
          progressMessage
        ).mkString("\n")
      )
    }
  }
}
