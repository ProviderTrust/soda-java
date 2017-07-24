

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


    //noinspection LongLine
    stash excludes: '**/runtime-aspects/**,**/compiletime-aspects/**,**/build/gradle/libs/*,**/build/gradle/tmp/**/*,.idea/**/*,.git/**/*', name: 'Soda API buildFiles'
}
stage 'Tests In Parallel'
parallel(["Unit Tests": {
    node {
        //        stage 'Unit Tests'
        def jdkHome = tool 'JDK 8'
        unstash 'Soda API buildFiles'
        withEnv(["JAVA_HOME=$jdkHome"]) {
            // Run the build
            wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'css']) {
                sh "./gradlew test $gradleOptions"
            }
        }
        step([$class: 'JUnitResultArchiver', allowEmptyResults: true, healthScaleFactor: 2.0,
              keepLongStdio: true, testResults: '**/build/gradle/test-results/TEST*.xml'])

    }
}, "Integration Tests": {

    node {
        //        stage 'Integration Tests'
        def jdkHome = tool 'JDK 8'
        unstash 'Soda API buildFiles'

        withEnv(["JAVA_HOME=$jdkHome"]) {
            // Run the build
            wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'css']) {
                sh "./gradlew test -PtestGroups=integration $gradleOptions"
            }
        }
        step([$class: 'JUnitResultArchiver', allowEmptyResults: true, healthScaleFactor: 4.0,
              keepLongStdio: true, testResults: '**/build/gradle/test-results/TEST*.xml'])

    }

}, failFast: true])

node {
    stage 'Coding Convention Review'
    def jdkHome = tool 'JDK 8'
    unstash 'Soda API buildFiles'

    withEnv(["JAVA_HOME=$jdkHome"]) {
        // Run the build
        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'css']) {
            sh "./gradlew checkstyleMain $gradleOptions"
        }
    }
    step([$class: 'CheckStylePublisher', canComputeNew: true, defaultEncoding: 'UTF-8', failedTotalAll: '30',
          failedTotalHigh: '5', failedTotalNormal: '20', healthy: '20', pattern: '**/build/gradle/reports/checkstyle/*.xml',
          unHealthy: '50', unstableTotalAll: '40', unstableTotalHigh: '10', unstableTotalNormal: '30'])


}

node {
    stage 'Code Quality'
    def jdkHome = tool 'JDK 8'
    unstash 'Soda API buildFiles'

    withEnv(["JAVA_HOME=$jdkHome"]) {
        // Run the build
        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'css']) {
            sh "./gradlew findbugsMain $gradleOptions"
        }
    }
    step([$class: 'FindBugsPublisher', canComputeNew: true, defaultEncoding: 'UTF-8', excludePattern: '', failedTotalAll: '15',
          failedTotalHigh: '5', failedTotalNormal: '10', healthy: '5', includePattern: '',
          pattern: '**/build/gradle/reports/findbugs/*.xml', thresholdLimit: 'normal', unHealthy: '50', unstableTotalAll: '5',
          unstableTotalHigh: '2', unstableTotalNormal: '5'])

    stage 'JavaDoc'

    withEnv(["JAVA_HOME=$jdkHome"]) {
        // Run the build
        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'css']) {
            sh "./gradlew javadoc $gradleOptions"
        }
    }
    step([$class: 'WarningsPublisher', canComputeNew: true, canResolveRelativePaths: false,
          consoleParsers: [[parserName: 'JavaDoc Tool']], defaultEncoding: '', excludePattern: '', healthy: '',
          includePattern: '', messagesPattern: '', unHealthy: ''])

    step([$class: 'JavadocArchiver', javadocDir: 'build/gradle/docs/javadoc', keepAll: false])

    step([$class: 'AnalysisPublisher', canComputeNew: true, defaultEncoding: '',
          healthy: '', pmdActivated: false, unHealthy: ''])

    step([$class: 'GitHubCommitStatusSetter', errorHandlers: [[$class: 'ShallowAnyErrorHandler']]])

    stage 'Libraries'
    withEnv(["JAVA_HOME=$jdkHome"]) {
        // Run the build
        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'css']) {
            sh "./gradlew classes $gradleOptions -Pcompile_libraries=true"
        }
    }

    if(false && currentBuild.result == 'SUCCESS') {
        try
        {
            if(isSnapshot || isRelease)
            {
                stage 'Publish'
                timeout(time: 5, unit: 'MINUTES') {
                    //noinspection LongLine
                    slackSend channel: '#ci-jenkins-build', teamDomain: 'providertrust.slack.com', token: 'xYngIvl9Nv0XnKko8iu5qH5I', color: 'good', message: "${atUser}${env.JOB_NAME} ${currentBuild.displayName} can be published to the artifactory repo.\n(<${env.JOB_URL}|Open>)"

                    //noinspection LongLine
                    def params = input id: 'Publish', message: 'Publish Artifacts To Repo Server', ok: 'Publish Artifacts', parameters: [[$class: 'StringParameterDefinition', defaultValue: 'rtennant', description: 'Username to publish artifacts', name: 'publish_venturetech_username'], [$class: 'PasswordParameterDefinition', defaultValue: '', description: 'Password publish artifacts', name: 'publish_venturetech_password']]

                    withEnv(["JAVA_HOME=$jdkHome", "GRADLE_OPTS=-Xmx2024m -Xms512m"]) {
                        // Run the build
                        wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'css']) {
                            //noinspection LongLine
                            echo "Publishing artifacts"
                            //noinspection LongLine
                            def additionalArgs = $/artifactoryPublish -Ppublish_venturetech_username=${params.publish_venturetech_username} -Ppublish_venturetech_password=${params.publish_venturetech_password}/$
                            sh "./gradlew $gradleOptions $additionalArgs"
                        }
                    }

                }
            }
            if(isSnapshot)
            {
                stage 'Deploy'
                timeout(time: 15, unit: 'MINUTES') {
                    //noinspection LongLine
                    slackSend channel: '#ci-jenkins-build', teamDomain: 'providertrust.slack.com', token: 'xYngIvl9Nv0XnKko8iu5qH5I', color: 'good', message: "${atUser}${env.JOB_NAME} ${currentBuild.displayName} can be deployed to QA for testing.\n(<${env.JOB_URL}|Open>)"
                    input id: 'Deploy', message: 'Deploy build to QA Server?', ok: 'Deploy'
                    withEnv(["JAVA_HOME=$jdkHome"]) {
                        //noinspection LongLine
                        withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'aws_id', credentialsId: 'proteus-sys-jenkins-integration', secretKeyVariable: 'aws_secret']]) {
                            // Run the build
                            wrap([$class: 'AnsiColorBuildWrapper', 'colorMapName': 'css']) {
                                //noinspection LongLine
                                echo "Deploying artifacts"
                                def additionalArgs = "autoDeploy -Paws_id=${env.aws_id} -Paws_secret=${env.aws_secret}"
                                sh "./gradlew $gradleOptions $additionalArgs"
                            }
                        }
                    }
                }
            }
        }
        catch(error){
            echo "$error"
            //Skipping publish task
        }
    }
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

