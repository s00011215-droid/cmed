@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
set MAVEN_HOME=C:\Users\willy\.m2\wrapper\dists\apache-maven-3.9.6
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%
call %MAVEN_HOME%\bin\mvn.cmd %*
