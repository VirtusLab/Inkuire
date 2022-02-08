package org.virtuslab.inkuire.engine.model

case class ITID(uuid: String, isParsed: Boolean)

object ITID {
  def parsed(uuid: String): ITID = ITID(uuid, isParsed = true)

  def external(uuid: String): ITID = ITID(uuid, isParsed = false)
}
