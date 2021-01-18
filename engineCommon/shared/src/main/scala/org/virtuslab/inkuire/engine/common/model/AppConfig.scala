package org.virtuslab.inkuire.engine.common.model

trait AppConfig

trait AppParam extends Any
case class DbPath(path: String) extends AnyVal with AppParam
case class AncestryGraphPath(path: String) extends AnyVal with AppParam
