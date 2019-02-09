package nl.knaw.dans.easy.springfield

class GetProgressOfCurrentJobsSpec extends TestSupportFixture with GetProgressOfCurrentJobs {

  "getProgressOfCurrentJobs" should "return a map from jobref to a progress (a percentage completed)" in {
    val queueXml = <fsxml>
      <queue id="high">
        <properties>
          <info>queue with high priority</info>
          <priority>high</priority>
        </properties>
        <job>
          <rawaudio id="1" referid="/domain/dans/user/emarkus/audio/23/rawaudio/2">
            <properties>
              <reencode>false</reencode>
              <mount>dans</mount>
              <format>aac</format>
              <extension>m4a</extension>
              <wantedbitrate>800000</wantedbitrate>
              <batchfile>mp4_audio</batchfile>
              <filename>IM14HeeschH1v1.m4a</filename>
              <job>/domain/dans/service/willie/queue/high/job/42</job>
            </properties>
          </rawaudio>
          <status>

          </status>
        </job>
      </queue>
    </ fsxml>

    getProgressOfCurrentJobs(queueXml) should be(Map("/domain/dans/service/willie/queue/high/job/42" -> ))

  }
}
