

def repoUser, repoPassword, gradleOptions, version, isSnapshot, isRelease;
def atUser=''

try
{
    //noinspection GroovyAssignabilityCheck
    properties([
        [$class: 'BuildDiscarderProperty', strategy:
            [$class: 'LogRotator', artifactDaysToKeepStr: '45', artifactNumToKeepStr: '5', daysToKeepStr: '45',
             numToKeepStr: '10']],
        [$class: 'ScannerJobProperty', doNotScan: false],
        [$class: 'GithubProjectProperty', displayName: 'Soda API',
         projectUrlStr: 'https://github.com/ProviderTrust/soda-java'],
    ])

}
catch(error) {
    // Properties not supported for some job types
    echo "Unable to set properties $error"
}

node {
    stage "Build"
    try
    {
        echo("${env.CHANGE_AUTHOR_EMAIL}")
        if (env.CHANGE_AUTHOR_EMAIL && env.CHANGE_AUTHOR_EMAIL.indexOf('@') != -1)
        {
            atUser = '@' + env.CHANGE_AUTHOR_EMAIL.substring(0, env.CHANGE_AUTHOR_EMAIL.indexOf('@')) + ': '
        }
    }
    catch(error)
    {
        echo "Unable to get atUser: $error"
    }
    try
    {
        repoUser = repo_venturetech_username
        repoPassword = repo_venturetech_password
    }
    catch(error)
    {
        echo("Unable to get build parameter. Will try env. $error")
    }
    if(!repoUser) {
        // try environment
        echo("Checking env: ${env.repo_venturetech_username}")
        repoUser = env.repo_venturetech_username
        repoPassword = env.repo_venturetech_password
    }

    def jdkHome = tool 'JDK 8'

    // Get some code from a GitHub repository
    checkout scm

    step([$class: 'GitHubSetCommitStatusBuilder', statusMessage: [content: 'Jenkins is building changes']])

    def gradleProps = readFile('gradle.properties')
    isSnapshot = env.BRANCH_NAME?.startsWith('sprint/') || env.BRANCH_NAME?.startsWith('epic/')
    isRelease = (env.BRANCH_NAME?.equals('master') || env.BRANCH_NAME?.startsWith('release/'))
    def appVersion = getAppVersion(gradleProps).replace('-SNAPSHOT', '')
    def appName = getAppName(gradleProps)
    echo "Building $appName $appVersion"
    version = "${appVersion}.${currentBuild.number}${isSnapshot ? '-SNAPSHOT' : ''}"
    //noinspection LongLine
    gradleOptions= $/-Prepo_venturetech_username=$repoUser -Prepo_venturetech_password=$repoPassword --no-daemon -Papp_version=$version/$
    currentBuild.displayName = "v${version}"
    gradleProps = null;
    withEnv(["JAVA_HOME=$jdkHome"]){
        // Run the build
        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'css']) {
            sh "./gradlew clean assemble $gradleOptions"
        }
    }
    step([$class: 'ArtifactArchiver', artifacts: '**/build/gradle/libs/*', excludes: null,
          fingerprint: true, onlyIfSuccessful: true])
    step([$class: 'WarningsPublisher', canComputeNew: true, canResolveRelativePaths: false,
          consoleParsers: [[parserName: 'Java Compiler (javac)']], defaultEncoding: '',
          excludePattern: '', healthy: '', includePattern: '', messagesPattern: '', unHealthy: ''])
    step([$class: 'TasksPublisher', canComputeNew: true, defaultEncoding: '', excludePattern: '', healthy: '', high: 'FIXME',
          low: 'FUTURE', normal: 'TODO', pattern: 'src/**/*.java', unHealthy: ''])

    stage 'Tests In Parallel'
    withEnv(["JAVA_HOME=$jdkHome"]) {
        // Run the build
        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'css']) {
            sh "./gradlew test $gradleOptions"
        }
    }
    step([$class: 'JUnitResultArchiver', allowEmptyResults: true, healthScaleFactor: 2.0,
          keepLongStdio: true, testResults: '**/build/gradle/test-results/TEST*.xml'])

}



if(currentBuild.result == 'FAILURE') {
    //noinspection LongLine
    try
    {
        slackSend channel: '#ci-jenkins-build', teamDomain: 'providertrust.slack.com', token: 'xYngIvl9Nv0XnKko8iu5qH5I',
            color: 'danger', message: "${atUser}${env.JOB_NAME} ${currentBuild.displayName} failed.\n(<${env.JOB_URL}|Open>)"
    }
    catch(err)
    {
        println "$err"
    }
    mail to: "${env.CHANGE_AUTHOR_EMAIL}",
        subject: "Job '${env.JOB_NAME}' (${currentBuild.displayName}) failed",
        body: "Please go to ${env.BUILD_URL} and review the failure."
} else if(currentBuild.result == 'SUCCESS' && currentBuild.previousBuild && currentBuild.previousBuild?.result != 'SUCCESS') {
    //noinspection LongLine
    try
    {
        slackSend channel: '#ci-jenkins-build', teamDomain: 'providertrust.slack.com', token: 'xYngIvl9Nv0XnKko8iu5qH5I',
            color: 'good', message: "${atUser} ${env.JOB_NAME} ${currentBuild.displayName} is back to normal."
    }
    catch(err)
    {
        println("$err")
    }
}


@NonCPS
def getAppVersion(String text)
{
    def matcher = text =~ /app_version=(.+)/
    return matcher ? matcher[0][1] : '0.0'
}

@NonCPS
def getAppName(String text)
{
    def matcher = text =~ /app_name=(.+)/
    return matcher ? matcher[0][1] : 'App'
}

