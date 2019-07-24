/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.app.service.impl

import java.util.Locale
import java.util.Locale._
import java.util.Optional.ofNullable

import example.web.app.service.api.GreetingService
import AGreetingService.UNDETERMINED
import global.namespace.neuron.di.scala._
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.scalatest.prop.TableDrivenPropertyChecks._

import scala.jdk.CollectionConverters._

class AGreetingServiceSpec extends WordSpec {

  "AGreetingService" should {
    "compute the expected message" in {
      forAll(tests) { (languageRanges, who, message) =>
        greetingService(languageRanges.asJava, ofNullable(who.orNull)) shouldBe message
      }
    }
  }

  private lazy val tests = Table(
    ("languageRanges", "who", "message"),
    (List.empty[Locale], Some("Chris"), "Hello, Chris!"),
    (List.empty[Locale], None, "Hello, world!"),
    (List(AUSTRIA, GERMAN), Some("Christian"), "Servus, Christian!"),
    (List(AUSTRIA, GERMAN), None, "Servus, miteinander!"),
    (List(GERMANY, GERMAN), Some("Christian"), "Hallo, Christian!"),
    (List(GERMANY, GERMAN), None, "Hallo, Welt!"),
    (List(SWITZERLAND, GERMAN), Some("Christian"), "Grüazie, Christian!"),
    (List(SWITZERLAND, GERMAN), None, "Grüazie, miteinander!"),
    (List(UK, ENGLISH), Some("Chris"), "Hello, Chris!"),
    (List(UK, ENGLISH), None, "Hello, world!"),
    (List(UNDETERMINED), Some("Chris"), "Hello, Chris!"),
    (List(UNDETERMINED), None, "Hello, world!"),
    (List(US, ENGLISH), Some("Chris"), "Howdy, Chris!"),
    (List(US, ENGLISH), None, "Howdy, y'all!"),
  )

  private lazy val greetingService: GreetingService = wire[AGreetingService]

  //noinspection ScalaUnusedSymbol
  private lazy val defaultLocale = ENGLISH
  private lazy val AUSTRIA = forLanguageTag("de-AT")
  private lazy val SWITZERLAND = forLanguageTag("de-CH")

  //noinspection ScalaUnusedSymbol
  private lazy val greetingMessages = Map(
    AUSTRIA -> List("Servus, %s!", "miteinander"),
    ENGLISH -> List("Hello, %s!", "world"),
    GERMAN -> List("Hallo, %s!", "Welt"),
    SWITZERLAND -> List("Grüazie, %s!", "miteinander"),
    US -> List("Howdy, %s!", "y'all"),
  ).view.mapValues(_.asJava).toMap.asJava
}
