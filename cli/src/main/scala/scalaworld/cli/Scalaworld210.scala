package scalaworld.cli

import scalaworld.Fixed
import scalaworld.Scalaworld
import scalaworld.rewrite.Rewrite
import scalaworld.util.logger

class Scalaworld210 {
  def fix(originalContents: String, filename: String): String = {
    Scalaworld.fix(originalContents, Rewrite.default) match {
      case Fixed.Success(fixedCode) => fixedCode
      case Fixed.Failure(e) =>
        logger.warn(s"Failed to fix $filename. Cause ${e.getMessage}")
        originalContents
    }
  }
}
