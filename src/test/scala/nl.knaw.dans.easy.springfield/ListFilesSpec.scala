/*
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

import scala.xml.XML

class ListFilesSpec extends TestSupportFixture with ListFiles{
  "listFiles" should "return audio as well as video" in {
    val parent = XML.loadFile("src/test/resources/barbapappa.xml")
    listFiles(parent) shouldBe
      Seq(
        ("easy-dataset:218799","/domain/dans/user/Barbapappa/presentation/4","Lala_20200117_audio.mkv", "/domain/dans/user/Barbapappa/video/1/rawvideo/2/Lala_20200117_audio.mp4", true),
        ("easy-dataset:218800","/domain/dans/user/Barbapappa/presentation/5","Beau_20200305_part2_section1.mkv","/domain/dans/user/Barbapappa/video/2/rawvideo/2/Beau_20200305_part2_section1.mp4",true),
        ("easy-dataset:218800","/domain/dans/user/Barbapappa/presentation/5","Beau_20200305_part2_section3.mkv","/domain/dans/user/Barbapappa/video/3/rawvideo/2/Beau_20200305_part2_section3.mp4",true),
        ("easy-dataset:218801","/domain/dans/user/Barbapappa/presentation/6","Barbamamma_20200127_audio.mka","/domain/dans/user/Barbapappa/audio/5/rawaudio/2/Barbamamma_20200127_audio.m4a",true),
        ("easy-dataset:218801","/domain/dans/user/Barbapappa/presentation/6","Barbamamma_20200127_micaudio.mka","/domain/dans/user/Barbapappa/audio/4/rawaudio/2/Barbamamma_20200127_micaudio.m4a",true),
        ("easy-dataset:218802","/domain/dans/user/Barbapappa/presentation/7","Pons_20200226_Video_part1.mkv","/domain/dans/user/Barbapappa/video/4/rawvideo/2/Pons_20200226_Video_part1.mp4",true),
        ("easy-dataset:218802","/domain/dans/user/Barbapappa/presentation/7","Pons_20200226_Video_part2.mkv","/domain/dans/user/Barbapappa/video/5/rawvideo/2/Pons_20200226_Video_part2.mp4",true),
        ("easy-dataset:218803","/domain/dans/user/Barbapappa/presentation/8","Boegel_20200226_video.mkv","/domain/dans/user/Barbapappa/video/6/rawvideo/2/Boegel_20200226_video.mp4",true),
        ("easy-dataset:218804","/domain/dans/user/Barbapappa/presentation/9","Barbamamma_20200127_video.mkv","/domain/dans/user/Barbapappa/video/7/rawvideo/2/Barbamamma_20200127_video.mp4",true),
        ("easy-dataset:231229","/domain/dans/user/Barbapappa/presentation/10","Clickety_20200210_Video_1.mkv","/domain/dans/user/Barbapappa/video/9/rawvideo/2/Clickety_20200210_Video_1.mp4",false),
        ("easy-dataset:231229","/domain/dans/user/Barbapappa/presentation/10","Clickety_20200210_Video_2.mkv","/domain/dans/user/Barbapappa/video/8/rawvideo/2/Clickety_20200210_Video_2.mp4",true),
        ("easy-dataset:231231","/domain/dans/user/Barbapappa/presentation/11","Lib_20100312_video.mkv","/domain/dans/user/Barbapappa/video/10/rawvideo/2/Lib_20100312_video.mp4",true),
        ("easy-dataset:231232","/domain/dans/user/Barbapappa/presentation/12","Trick_20200131_audio.mka","/domain/dans/user/Barbapappa/audio/6/rawaudio/2/Trick_20200131_audio.m4a",true),
        ("easy-dataset:231233","/domain/dans/user/Barbapappa/presentation/13","Trick_20200131_videointerview_1.mkv","/domain/dans/user/Barbapappa/video/12/rawvideo/2/Trick_20200131_videointerview_1.mp4",false),
        ("easy-dataset:231233","/domain/dans/user/Barbapappa/presentation/13","Trick_20200131_videointerview_2.mkv","/domain/dans/user/Barbapappa/video/11/rawvideo/2/Trick_20200131_videointerview_2.mp4",true),
        ("easy-dataset:231234","/domain/dans/user/Barbapappa/presentation/14","Clickety_20200210_audio_1.mka","/domain/dans/user/Barbapappa/audio/8/rawaudio/2/Clickety_20200210_audio_1.m4a",true),
        ("easy-dataset:231234","/domain/dans/user/Barbapappa/presentation/14","Clickety_20200210_audio_2.mka","/domain/dans/user/Barbapappa/audio/7/rawaudio/2/Clickety_20200210_audio_2.m4a",true),
        ("easy-dataset:155215","/domain/dans/user/Barbapappa/presentation/3","Zoo_20190817_audio_iPhone7.m4a","/domain/dans/user/Barbapappa/audio/3/rawaudio/2/Zoo_20190817_audio_iPhone7.m4a",true),
        ("easy-dataset:155216","/domain/dans/user/Barbapappa/presentation/1","Bravo_20160325_interview.mp3","/domain/dans/user/Barbapappa/audio/1/rawaudio/2/Bravo_20160325_interview.m4a",true),
        ("easy-dataset:155217","/domain/dans/user/Barbapappa/presentation/2","Bravo_20160406_interview.mp3","/domain/dans/user/Barbapappa/audio/2/rawaudio/2/Bravo_20160406_interview.m4a",true),
      )
  }

  it should "just return empty Seq if no Springfield xml is passed to it" in {
    val parent = <html>
      <body>This is not springfield</body>
    </html>

    listFiles(parent) shouldBe empty
  }
}
