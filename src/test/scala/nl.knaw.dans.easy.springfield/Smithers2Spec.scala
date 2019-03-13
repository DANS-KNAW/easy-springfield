/**
 * Copyright (C) 2017 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.springfield

import java.net.URI
import java.nio.file.Paths

import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.scalatest._

import scala.util.{ Failure, Random, Success }
import scala.xml.Elem

class Smithers2Spec extends FlatSpec
  with Matchers
  with CustomMatchers
  with Smithers2
  with DebugEnhancedLogging {
  override val smithers2BaseUri: URI = new URI("http://localhost:8080/smithers2/")
  override val smithers2ConnectionTimeoutMs: Int = 100000
  override val smithers2ReadTimoutMs: Int = 100000
  override val defaultDomain: String = "dans"
  private val elem: Elem =
    <fsxml>
        <presentation id="3">
            <videoplaylist id="1">
            <video id="1" referid="/domain/dans/user/utest/video/5">
            </video>
            <video id="2" referid="/domain/dans/user/utest/video/7">
            </video>
          </videoplaylist>
        </presentation>
      </fsxml>

  "extractVideoRefFromPresentationForVideoId" should "retrieve a relative path to a video starting from domain" in {
    extractVideoRefFromPresentationForVideoId("1")(elem) shouldBe Success("domain/dans/user/utest/video/5")
    extractVideoRefFromPresentationForVideoId("2")(elem) shouldBe Success("domain/dans/user/utest/video/7")
  }

  it should "fail if a non existing index is given" in {
    extractVideoRefFromPresentationForVideoId("3")(elem) should matchPattern {
      case Failure(i: IllegalStateException) if i.getMessage == "No videoReference found for index '3' in the presentation" =>
    }
  }

  it should "fail if a malformed xml-elem is provided" in {
    val malFormedElem = <mal><formed><elem></elem></formed></mal>
    extractVideoRefFromPresentationForVideoId("1")(malFormedElem) should matchPattern {
      case Failure(i: IllegalStateException) if i.getMessage == "No videoReference found for index '1' in the presentation" =>
    }
  }

  it should "return the first video if two videos share the same reference id" in {
    val elemDuplicateReference = <fsxml>
        <presentation id="3">
          <videoplaylist id="1">
            <video id="1" referid="/domain/dans/user/utest/video/5">
            </video>
            <video id="1" referid="/domain/dans/user/utest/video/7">
            </video>
          </videoplaylist>
        </presentation>
      </fsxml>
    extractVideoRefFromPresentationForVideoId("1")(elemDuplicateReference) shouldBe Success("domain/dans/user/utest/video/5")
  }

  "checkPresentation" should "succeed if the name path has more than 3 parts and presentation is the penultimate part" in {
    checkPresentation(Paths.get("domain/dans/user/utest/presentation/1")) shouldBe Success(())
  }

  it should "fail if the path has more than 3 parts and presentation is the last part" in {
    val path = "domain/dans/user/utest/presentation"
    checkPresentation(Paths.get(path)) should matchPattern {
      case Failure(iae: IllegalArgumentException) if iae.getMessage == createExceptionMessage(path) =>
    }
  }

  it should "fail if the  path has less than 4 parts and presentation is the penultimate part" in {
    val path = "utest/presentation/notANumber"
    checkPresentation(Paths.get(path)) should matchPattern {
      case Failure(iae: IllegalArgumentException) if iae.getMessage == createExceptionMessage(path) =>
    }
  }

  it should "succeed if the path has more than 3 parts and presentation is the penultimate part, even though last part is not a number" in {
    checkPresentation(Paths.get("domain/dans/user/utest/presentation/notANumber")) shouldBe Success(())
  }

  "checkVideoReferId" should "succeed if the path has more than 3 parts and video is the penultimate part" in {
    checkVideoReferId(Paths.get("domain/dans/user/utest/video/1")) shouldBe Success(())
  }

  it should "fail if the path has more than 3 parts and video is the last part" in {
    val path = "domain/dans/user/utest/video"
    checkVideoReferId(Paths.get(path)) should matchPattern {
      case Failure(iae: IllegalArgumentException) if iae.getMessage == s"$path does not appear to be a video referid. Expected format: [domain/<d>/]user/<u>/video/<number>" =>
    }
  }

  it should "fail if the path has less than 4 parts and video is the penultimate part" in {
    val path = "test/video/1"
    checkVideoReferId(Paths.get(path)) should matchPattern {
      case Failure(iae: IllegalArgumentException) if iae.getMessage == s"$path does not appear to be a video referid. Expected format: [domain/<d>/]user/<u>/video/<number>" =>
    }
  }

  it should "succeed if the path has more than 3 parts and video is the penultimate part, even though last part is not a number" in {
    checkVideoReferId(Paths.get("domain/dans/user/utest/video/notANumber")) shouldBe Success(())
  }

  "checkCollection" should "succeed if the name path has more than 3 parts and collection is the penultimate part" in {
    checkCollection(Paths.get("domain/dans/user/utest/collection/1")) shouldBe Success(())
  }

  it should "fail if the path has more than 3 parts and collection is the last part" in {
    val path = "domain/dans/user/utest/collection"
    checkCollection(Paths.get(path)) should matchPattern {
      case Failure(iae: IllegalArgumentException) if iae.getMessage == s"$path does not appear to be a collection Springfield path. Expected format: [domain/<d>/]user/<u>/collection/<name>" =>
    }
  }

  it should "fail if the path has less than 4 parts and collection is the penultimate part" in {
    val path = "test/collection/1"
    checkCollection(Paths.get(path)) should matchPattern {
      case Failure(iae: IllegalArgumentException) if iae.getMessage == s"$path does not appear to be a collection Springfield path. Expected format: [domain/<d>/]user/<u>/collection/<name>" =>
    }
  }

  it should "succeed if the path has more than 3 parts and collection is the penultimate part, even though last part is not a number" in {
    checkCollection(Paths.get("domain/dans/user/utest/collection/notANumber")) shouldBe Success(())
  }

  "checkNameLenght" should "succeed if the name length is below 101" in {
    checkNameLength("below 100") shouldBe Success(())
    checkNameLength(Random.nextString(100)) shouldBe Success(())
  }

  it should "fail if the name length is above 100" in {
    val moreThan100Chars = Random.nextString(101)
    checkNameLength(moreThan100Chars) should matchPattern {
      case Failure(i: IllegalArgumentException) if i.getMessage == s"Name is longer than 100 chars: $moreThan100Chars" =>
    }
  }

  "getCompletePath" should "add domain to the path" in {
    val uncompletPath = Paths.get("not/a/complete/path")
    getCompletePath(uncompletPath) shouldBe Paths.get("domain/dans").resolve(uncompletPath)
  }

  it should "not alter an already complete path (relative)" in {
    val completePath = Paths.get("domain/dans/complete/path")
    getCompletePath(completePath) shouldBe completePath
  }

  it should "not alter an already complete path (absolute)" in {
    val completePath = Paths.get("/domain/dans/complete/path")
    getCompletePath(completePath) shouldBe completePath
  }

  it should "not alter an already complete path (relative), also if the second param is not equal to the default domain" in {
    val completePath = Paths.get("domain/notDans/complete/path")
    getCompletePath(completePath) shouldBe completePath
  }

  private def createExceptionMessage(path: String): String = s"$path does not appear to be a presentation referid or Springfield path. Expected format: [domain/<d>/]user/<u>/presentation/<number> OR [domain/<d>/]user/<u>/collection/<c>/presentation/<p>"
}
