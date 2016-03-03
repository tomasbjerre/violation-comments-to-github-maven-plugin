package se.bjurr.violations.comments.github.maven;

import org.apache.maven.plugins.annotations.Parameter;

public class ViolationConfig {
 @Parameter(property = "reporter", required = true)
 private String reporter;
 @Parameter(property = "folder", required = true)
 private String folder;
 @Parameter(property = "pattern", required = false)
 private String pattern;

 public void setFolder(String folder) {
  this.folder = folder;
 }

 public void setPattern(String pattern) {
  this.pattern = pattern;
 }

 public void setReporter(String reporter) {
  this.reporter = reporter;
 }

 public String getFolder() {
  return folder;
 }

 public String getPattern() {
  return pattern;
 }

 public String getReporter() {
  return reporter;
 }
}
