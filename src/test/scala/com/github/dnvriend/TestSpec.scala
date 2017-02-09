package com.github.dnvriend

import org.scalatest.{FlatSpec, Matchers}
import org.typelevel.scalatest.{ValidationMatchers, DisjunctionMatchers}

abstract class TestSpec extends FlatSpec with Matchers with ValidationMatchers with DisjunctionMatchers