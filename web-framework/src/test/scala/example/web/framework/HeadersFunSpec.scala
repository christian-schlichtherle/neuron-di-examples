/*
 * Copyright (C) 2019 Schlichtherle IT Services.
 * All rights reserved. Use is subject to license terms.
 */
package example.web.framework

import java.util.Collections
import java.util.Locale._

import com.sun.net.httpserver.Headers
import HeadersFun.acceptLanguages
import org.scalatest.Matchers._
import org.scalatest.WordSpec
import org.scalatest.prop.TableDrivenPropertyChecks._

import scala.jdk.CollectionConverters._

class HeadersFunSpec extends WordSpec {

  "HeadersFun.acceptLanguages" should {
    "compute the expected locales" in {
      forAll(tests) { (acceptLanguage, locales) =>
        val h = new Headers
        h.add("Accept-Language", acceptLanguage)
        h.add("Accept-Language", "<ignored>")
        acceptLanguages(h) shouldBe locales.asJava
      }
    }

    "return no locale" in {
      acceptLanguages(new Headers) shouldBe Collections.emptyList
    }
  }

  private lazy val tests = Table(
    ("acceptLanguage", "locales"),
    ("*", List(forLanguageTag("*"))),
    ("en, *;q=0.5", List(ENGLISH, forLanguageTag("*"))),
    (" * ; q=0.5 , en ", List(ENGLISH, forLanguageTag("*"))),
    ("da, en-gb;q=0.8, en;q=0.7", List(forLanguageTag("da"), UK, ENGLISH)),
    (" en ; q=0.7 , en-gb ; q=0.8 , da ", List(forLanguageTag("da"), UK, ENGLISH)),
  )
}
