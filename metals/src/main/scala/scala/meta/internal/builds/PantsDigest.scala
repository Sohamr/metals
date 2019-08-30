package scala.meta.internal.builds

import scala.meta.io.AbsolutePath
import java.security.MessageDigest
import scala.sys.process._

import scala.meta.internal.metals.UserConfiguration

object PantsDigest extends Digestable {
  override protected def digestWorkspace(
      workspace: AbsolutePath,
      digest: MessageDigest,
      userConfig: UserConfiguration
  ): Boolean = {
    userConfig.pantsTargets match {
      case None => true
      case Some(pantsTargets) =>
        val args = List(
          workspace.resolve("pants").toString(),
          "filedeps",
          pantsTargets
        )
        val pantsFileDeps = args.!!.trim
        pprint.log(pantsFileDeps)
        pantsFileDeps.linesIterator
          .map { file =>
            java.nio.file.Paths.get(file).toAbsolutePath.normalize
          }
          .filter(_.endsWith("BUILD"))
          .forall(file => Digest.digestFile(AbsolutePath(file), digest))
    }
  }
}
