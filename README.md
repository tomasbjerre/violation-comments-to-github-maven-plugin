# Violation Comments to GitHub Maven Plugin [![Build Status](https://travis-ci.org/tomasbjerre/violation-comments-to-github-maven-plugin.svg?branch=master)](https://travis-ci.org/tomasbjerre/violation-comments-to-github-maven-plugin) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/se.bjurr.violations/violation-comments-to-github-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/se.bjurr.violations/violation-comments-to-github-maven-plugin)

This is a Maven plugin for [Violation Comments to GitHub Lib](https://github.com/tomasbjerre/violation-comments-to-github-lib).

It can be used in Travis, or any other build server, to read results from static code analysis like Findbugs, PMD, Checkstyle, JSHint and/or CSSLint and comment pull requests in GitHub with them.

You can have a look at [violations-test](https://github.com/tomasbjerre/violations-test/pull/2) to see what the result may look like.

## Usage ##
There is a running example [here](https://github.com/tomasbjerre/violation-comments-to-github-maven-plugin/tree/master/violation-comments-to-github-maven-plugin-example).

Here is and example: 

```
	<plugin>
		<groupId>se.bjurr.violations</groupId>
		<artifactId>violation-comments-to-github-maven-plugin</artifactId>
		<version>1.0</version>
		<executions>
			<execution>
				<id>ViolationCommentsToGitHub</id>
				<goals>
					<goal>violation-comments</goal>
				</goals>
				<configuration>
					<username>${GITHUB_USERNAME}</username>
					<password>${GITHUB_PASSWORD}</password>
					<oAuth2Token>${GITHUB_OAUTH2TOKEN}</oAuth2Token>
					<pullRequestId>${GITHUB_PULLREQUESTID}</pullRequestId>
					<repositoryOwner>tomasbjerre</repositoryOwner>
					<repositoryName>violations-test</repositoryName>
					<gitHubUrl>https://api.github.com/</gitHubUrl>
					<createCommentWithAllSingleFileComments>false</createCommentWithAllSingleFileComments>
					<createSingleFileComments>true</createSingleFileComments>
					<violations>
						<violation>
							<violation>
								<reporter>FINDBUGS</reporter>
								<folder>.</folder>
								<pattern>.*/findbugs/.*\.xml$</pattern>
							</violation>
							<violation>
								<reporter>PMD</reporter>
								<folder>.</folder>
								<pattern>.*/pmd/.*\.xml$</pattern>
							</violation>
							<violation>
								<reporter>CHECKSTYLE</reporter>
								<folder>.</folder>
								<pattern>.*/checkstyle/.*\.xml$</pattern>
							</violation>
							<violation>
								<reporter>JSHINT</reporter>
								<folder>.</folder>
								<pattern>.*/jshint/.*\.xml$</pattern>
							</violation>
							<violation>
								<reporter>CSSLINT</reporter>
								<folder>.</folder>
								<pattern>.*/csslint/.*\.xml$</pattern>
							</violation>
						</violation>
					</violations>
				</configuration>
			</execution>
		</executions>
	</plugin>
```

To send violations, just run:
```
mvn violation-comments-to-github-maven-plugin:violation-comments -DGITHUB_PULLREQUESTID=$GITHUB_PULL_REQUEST -DGITHUB_USERNAME=... -DGITHUB_PASSWORD=...
```

Or if you want to use OAuth2:
```
mvn violation-comments-to-github-maven-plugin:violation-comments -DGITHUB_PULLREQUESTID=$GITHUB_PULL_REQUEST -DGITHUB_OAUTH2TOKEN=$GITHUB_OAUTH2TOKEN
```

You may also have a look at [Violations Lib](https://github.com/tomasbjerre/violations-lib).

## Developer instructions

To make a release, first run:
```
mvn release:prepare -DperformRelease=true
mvn release:perform
```
Then release the artifact from [staging](https://oss.sonatype.org/#stagingRepositories). More information [here](http://central.sonatype.org/pages/releasing-the-deployment.html).
