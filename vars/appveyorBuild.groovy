def call(Map config) {
  /**
  * Config must be:
  * accountToken    - Appveyor API Token
  * accountName     - Appveyor username
  * projectSlug     - Appveyor project slug
  * commitId        - git commit hash
  * branch          - which branch the commit came from
  */
  script {
    echo '[APPVEYOR] Starting appveyor job';

    def request = [:]
    request['accountName'] = config.accountName;
    request['projectSlug'] = config.projectSlug;
    request['environmentVariables'] = [:];
    request['environmentVariables']['JENKINS_BUILD_NUMBER'] = config.buildNumber;

    if (config.branch.startsWith('PR')) {
      echo '[APPVEYOR] Building a pull request';
      def pr = config.branch.split('-')[1];
      request['pullRequestId'] = pr;
      echo "[APPVEYOR] Building pull request #${pr}"
    } else {
      echo "[APPVEYOR] Building ${config.branch} : ${config.commitId}";
      request['branch'] = config.branch;
      request['commitId'] = config.commitId;
    }

    def requestBody = new groovy.json.JsonBuilder(request).toPrettyString();

    // echo "[APPVEYOR] Request body: ${request_body}";
    def build_response = httpRequest(
      url: "https://ci.appveyor.com/api/account/${config.accountName}/builds",
      httpMode: 'POST',
      customHeaders: [
        [name: 'Authorization', value: "Bearer ${config.accountToken}"],
        [name: 'Content-type', value: 'application/json']
      ],
      requestBody: requestBody
    )

    def content = build_response.getContent();

    def buildObj = new groovy.json.JsonSlurperClassic().parseText(content)

    echo "[APPVEYOR] Appveyor build number: ${buildObj.buildNumber}";
    echo "[APPVEYOR] Appveyor build version: ${buildObj.version}";
    echo "[APPVEYOR] Build URL: https://ci.appveyor.com/project/${config.accountName}/${config.projectSlug}/builds/${buildObj.buildId}"

    env.APPVEYOR_BUILD_VERSION = buildObj.version;
    env.APPVEYOR_BUILD_ID = buildObj.buildId;

    def appveyorFinished = false;
    def buildStatus = "";

    while (appveyorFinished == false) {

      def status_response = httpRequest(
          url: "https://ci.appveyor.com/api/projects/${config.accountName}/${config.projectSlug}/build/${env.APPVEYOR_BUILD_VERSION}",
          httpMode: 'GET',
          customHeaders: [
              [name: 'Authorization', value: "Bearer ${config.accountToken}"],
              [name: 'Accept', value: 'application/json']
          ]
      )

      def status_content = status_response.getContent()
      def build_data = new groovy.json.JsonSlurperClassic().parseText(status_content)
      buildStatus = build_data.build.status

      if (buildStatus == "success" || buildStatus == "error" || buildStatus == "failed" || buildStatus == 'cancelled') {
        echo "[APPVEYOR] Finished. Result is ${buildStatus} ";
        appveyorFinished = true;
      } else {
        echo "[APPVEYOR] Build status is ${buildStatus}";
        sleep(30);
      }
    }

    if (buildStatus != "success") {
      echo "[APPVEYOR] Failed to build!";
      error("Appveyor failed to build! Version: ${buildVersion} - Status: ${buildStatus}")
    }
  }
}