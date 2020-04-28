package org.virtuslab.inkuire.engine.parser

import com.softwaremill.diffx.scalatest.DiffMatcher
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

abstract class BaseInkuireTest extends AnyFlatSpec with Matchers with DiffMatcher
