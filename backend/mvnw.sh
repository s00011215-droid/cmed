#!/bin/bash
# Maven wrapper for the xiangyun-zhifang project
export JAVA_HOME="/c/Program Files/Eclipse Adoptium/jdk-17.0.19.10-hotspot"
MAVEN_HOME="C:/Users/willy/.m2/wrapper/dists/apache-maven-3.9.6"
PROJECT_DIR="F:/MDFiles/xiangyun-zhifang/backend"
"$JAVA_HOME/bin/java" \
  -classpath "$MAVEN_HOME/boot/plexus-classworlds-2.7.0.jar" \
  "-Dclassworlds.conf=$MAVEN_HOME/bin/m2.conf" \
  "-Dmaven.home=$MAVEN_HOME" \
  "-Dlibrary.jansi.path=$MAVEN_HOME/lib/jansi-native" \
  "-Dmaven.multiModuleProjectDirectory=$PROJECT_DIR" \
  org.codehaus.classworlds.Launcher \
  "$@"
