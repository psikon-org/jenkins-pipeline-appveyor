def call(Map config) {
  /**
  * Config must be:
  * accountName     - Appveyor username
  * projectSlug     - Appveyor project slug
  * buildVersion    - Appveyor build version to download
  *                   artifacts from
  * targetDir       - Target directory to put downloaded artifacts
  */
  echo '[APPVEYOR] Downloading artifacts';

  def content = httpRequest(
    url: "https://ci.appveyor.com/api/projects/${config.accountName}/${config.projectSlug}/build/${config.buildVersion}",
    customHeaders: [
      [name: 'Accept', value: 'application/json']
    ]
  );
  // echo groovy.json.JsonOutput.prettyPrint(content.getContent());
  def build_obj = new groovy.json.JsonSlurperClassic().parseText(content.getContent());

  def job_id = build_obj.build.jobs[0].jobId;

  def artifact_response = httpRequest(
    url: "https://ci.appveyor.com/api/buildjobs/${job_id}/artifacts",
    customHeaders: [
      [name: 'Accept', value: 'application/json']
    ]
  );

  def artifact_response_content = artifact_response.getContent();
  echo artifact_response_content;

  build_obj = new groovy.json.JsonSlurperClassic().parseText(artifact_response_content);

  build_obj.each {
    echo "[APPVEYOR] Artifact found: ${it.fileName}";
    def f = new File(it.fileName);
    def fn = f.getName();
    def encodedFn = java.net.URLEncoder.encode(it.fileName, 'UTF-8');
    def url = "https://ci.appveyor.com/api/buildjobs/${job_id}/artifacts/${encodedFn}";
    echo "[APPVEYOR] Downloading ${url}"
    sh(script: """mkdir -p ${config.targetDir} && wget -O ${config.targetDir}/${fn} ${url}""");
    echo "[APPVEYOR] Artifact downloaded to ${config.targetDir}/${fn}"
  };
}
