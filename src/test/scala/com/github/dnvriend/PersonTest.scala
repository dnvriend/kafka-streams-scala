package com.github.dnvriend

import scalaz._
import Scalaz._

class PersonTest extends TestSpec {
	it should "create a person" in {
		Person("foo", 42) shouldBe Person("foo", 42)
		Person.validate("foo", 42) should beSuccess(Person("foo", 42))
	}

	it should "validate name" in {
		Person.validateName("foo") should beSuccess("foo")
		Person.validateName("") should haveFailure("Field 'name' must not be empty")
	}

	it should "validate age" in {
		Person.validateAge(42) should beSuccess(42)
		Person.validateAge(0) should beSuccess(0)
		Person.validateAge(200) should haveFailure("Field 'age' with value '200' must be less than '140'")
		Person.validateAge(-1) should haveFailure("Field 'age' with value '-1' must be greater than '-1'")
	}

	it should "compose" in {
		val result = for {
			p1 <- Person.validate("foo", 42).disjunction
			p2 <- Person.validate("bar", 150).disjunction
		} yield p2

		result should beLeft("Field 'age' with value '150' must be less than '140'".wrapNel)
	}
}