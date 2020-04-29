package org.virtuslab.inkuire.engine.model

import scala.io.Source

class DokkaDb {
  // TODO add actual DokkaDb structure
}

object DokkaDb {
  def read(path: String): DokkaDb = {
    // TODO actually read DokkaDb from given path
//    Source.fromFile(path).to(LazyList)
    new DokkaDb
  }
}