#!/bin/bash
mvn versions:update-properties
mvn clean generate-resources eclipse:eclipse install || exit 1
cd violation-comments-to-github-maven-plugin-example
mvn versions:update-properties -DallowSnapshots=true
mvn violation-comments-to-github-maven-plugin:violation-comments -DGITHUB_PULLREQUESTID=false -DGITHUB_OAUTH2TOKEN=$GITHUB_OAUTH2TOKEN -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -e
mvn violation-comments-to-github-maven-plugin:violation-comments -DGITHUB_PULLREQUESTID=$TRAVIS_PULL_REQUEST -DGITHUB_OAUTH2TOKEN=$GITHUB_OAUTH2TOKEN -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -e
