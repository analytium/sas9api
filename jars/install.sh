#!/bin/bash

mvn install:install-file -Dfile=./sas.oma.joma.jar -DgroupId=sas -DartifactId=sas.oma.joma -Dversion=9.4 -Dpackaging=jar

mvn install:install-file -Dfile=./sas.oma.omi.jar -DgroupId=sas -DartifactId=sas.oma.omi -Dversion=9.4 -Dpackaging=jar

mvn install:install-file -Dfile=./sas.security.sspi.jar -DgroupId=sas -DartifactId=sas.security.sspi -Dversion=9.4 -Dpackaging=jar

mvn install:install-file -Dfile=./sas.svc.connection.platform.jar -DgroupId=sas -DartifactId=sas.svc.connection.platform -Dversion=9.4 -Dpackaging=jar

mvn install:install-file -Dfile=./sas.core.jar -DgroupId=sas -DartifactId=sas.core -Dversion=9.4 -Dpackaging=jar

mvn install:install-file -Dfile=./sas.oma.joma.rmt.jar -DgroupId=sas -DartifactId=sas.oma.joma.rmt -Dversion=9.4 -Dpackaging=jar

mvn install:install-file -Dfile=./sas.svc.connection.jar -DgroupId=sas -DartifactId=sas.svc.connection -Dversion=9.4 -Dpackaging=jar

mvn install:install-file -Dfile=./sas.oma.util.jar -DgroupId=sas -DartifactId=sas.oma.util -Dversion=9.4 -Dpackaging=jar

echo "===================DONE========================="
