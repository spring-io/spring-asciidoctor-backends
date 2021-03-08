#!/bin/bash
set -e

version=$( cat built-artifact/version )

java -jar /github-changelog-generator.jar \
  --changelog.repository=spring-io/asciidoctor-spring-backend \
  ${version} generated-changelog/changelog.md

echo ${version} > generated-changelog/version
echo v${version} > generated-changelog/tag
