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

import nl.knaw.dans.lib.logging.DebugEnhancedLogging
import org.scalatest._

import scala.util.{ Failure, Success }
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
}
