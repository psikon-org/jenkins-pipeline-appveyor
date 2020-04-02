# psikon-jenkins-pipeline

Provides build steps that let you use appveyor to build your project from Jenkins.

## Installation

First, you need to [add this shared library](https://jenkins.io/doc/book/pipeline/shared-libraries/#global-shared-libraries) to your Jenkins global library configuration.

Secondly, you should setup a Jenkins [secret text credential](https://jenkins.io/doc/book/using/using-credentials/) that contains your [Appveyor API bearer token](https://ci.appveyor.com/api-keys).

Finally, you can add the following to the top of your Jenkinsfile:

```
@Library('psikon-jenkins-appveyor@master') _
```

And use the necessary steps to start building with Appveyor.

## Availabe Steps

- [appveyorBuild](#appveyorBuild)
- [appveyorDownloadAll](#appveyorDownloadAll)

### appveyorBuild

Triggers a build on appveyor and waits until it completes. If the status is an error, then the Jenkins build will fail as well.

It sets the following environment variables:

* `APPVEYOR_BUILD_VERSION:` The Appveyor build version for the build that was triggered
* `APPVEYOR_BUILD_ID:` The Appveyor build id

Example

```
withCredentials([string(credentialsId: 'appveyor-token', variable: 'APPVEYOR_TOKEN')]) {
  appveyorBuild(
    accountToken: APPVEYOR_TOKEN,
    accountName: 'my-appveyor-username',
    projectSlug: 'my-project-slug',
    branch: env.GIT_BRANCH,
    commitId: env.GIT_COMMIT,
    buildNumber: env.BUILD_NUMBER
  )
}
```

#### Arguments

* `accountToken:` The Appveyor API bearer token. See https://ci.appveyor.com/api-keys for more information
* `accountName:` Your Appveyor username or organisation name
* `projectSlug:` The project slug of your appveyor project. See your Appveyor project settings for more information.
* `branch`: The git branch Appveyor should checkout. Usually you can use `env.GIT_BRANCH`
* `commitId`: The git commit hash to build. Usually you can use `env.GIT_COMMIT`
* `buildNumber`: This value will be set as an Appveyor environment variable named `JENKINS_BUILD_NUMBER`

### appveyorDownloadAll

Downloads all artifacts from a specific Appveyor build.

Example:

```
appveyorDownloadAll(
  accountName: 'my-appveyor-username',
  projectSlug: 'my-project-slub',
  buildVersion: env.APPVEYOR_BUILD_VERSION,
  targetDir: 'target/artifacts'
)
```

#### Arguments

* `accountName:` Your Appveyor username or organisation name
* `projectSlug:` The project slug of your Appveyor project. See your Appveyor project settings for more information.
* `buildVersion:` The Appveyor build version to download
* `targetDir:` The target directory relative to your Jenkins job's workspace root.
