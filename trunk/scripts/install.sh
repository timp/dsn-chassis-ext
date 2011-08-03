#!/bin/sh
#Script to install from a repository
#apt-get install ant
#apt-get install subversion
#apt-get install tofrodos
ALF_VERSION=alfresco-3.4.2
ALF_HOME=/opt/${ALF_VERSION}
SOURCE_HOME=dsn-chassis-ext-read-only

rm -rf ${SOURCE_HOME}
svn checkout http://dsn-chassis-ext.googlecode.com/svn/trunk/ ${SOURCE_HOME}

SCRIPT_HOME=`pwd`
if [ "$1" != "" ]
then
	echo "Configuring for SSO with CAS"
	if [ "$2" = "" ] 
	then
		echo "Must pass in alfresco host name and CAS hostname"
		echo "e.g www.wwarn.org www.wwarn.org"
		exit 1
	fi
	NEW_HOST=https://$1
	CAS_HOST=$2
	sed -i.bak -e "s#http://localhost:8080#${NEW_HOST}#" -e "s#https://localhost:8443/cas-server-webapp-3.4.6#https://${CAS_HOST}/sso#" ${SCRIPT_HOME}/${SOURCE_HOME}/_alfresco/source/java/org/wwarn/cms/authentication/LoginCas.java
	sed -i.bak -e "s#http://localhost:8080#${NEW_HOST}#" ${SCRIPT_HOME}/${SOURCE_HOME}/_share/source/java/org/wwarn/cms/authentication/CasAuthenticationFilter.java
	sed -i.bak -e "s#https://localhost:8443/cas-server-webapp-3.4.6#https://${CAS_HOST}/sso#" ${SCRIPT_HOME}/${SOURCE_HOME}/_alfresco/web/jsp/relogin.jsp

	sed -i.bak -e 's#EOC.cas$#EOC.cas-->#' -e 's# EOC.default-->#<!--EOC.default-->#' -e "s#http://localhost:8080/alfresco#${NEW_HOST}/alfresco#" ${SCRIPT_HOME}/${SOURCE_HOME}/_share/config/alfresco/web-extension/share-config-custom.xml


fi
cd ${SOURCE_HOME}
fromdos build.properties
sed -i -e "s#X:/#/opt/#" -e "s#Alfresco3.4EWWARN#${ALF_VERSION}#" -e "s#C:/Program Files/Java/jdk1.6.0_11#${ALF_HOME}/java#" -e "s#tools/##" build.properties
export JAVA_HOME=${ALF_HOME}/java
service alfresco stop
ant deploy-alfresco-amp
ant deploy-share-jar
#Need to restart the service so that the new war is deployed
service alfresco start
sleep 110
cd ${SCRIPT_HOME}
#Assume that if arguments are used then we're installing for CAS SSO
#http://ecmstuff.blogspot.com/2011/06/configuring-alfresco-for-sso-with-cas.html
if [ "$1" != "" ]
then
	echo "Configuring for SSO with CAS"
	if [ "$2" = "" ] 
	then
		echo "Must pass in alfresco host name and CAS hostname"
		echo "e.g www.wwarn.org www.wwarn.org"
		exit 1
	fi
	NEW_HOST=https://$1
	CAS_HOST=$2
	sed -i.bak -e "s#http://localhost:8080#${NEW_HOST}#" -e "s#https://localhost:8443/cas-server-webapp-3.4.6#https://${CAS_HOST}/sso#" -e "s#https://localhost:8443/share#${NEW_HOST}/share#" ${SCRIPT_HOME}/${SOURCE_HOME}/manual_config/share.war/web.xml
	sed -i.bak -e "s#http://localhost:8080#${NEW_HOST}#" -e "s#https://localhost:8443/cas-server-webapp-3.4.6#https://${CAS_HOST}/sso#" ${SCRIPT_HOME}/${SOURCE_HOME}/manual_config/alfresco.war/web.xml

	sed -i.bak -e 's#EOC.cas$#EOC.cas-->#' -e 's# EOC.default-->#<!--EOC.default-->#' ${SCRIPT_HOME}/${SOURCE_HOME}/_share/config/alfresco/web-extension/share-config-custom.xml

	cd ${ALF_HOME}/tomcat/webapps/share/WEB-INF
	cp ${SCRIPT_HOME}/${SOURCE_HOME}/_alfresco/lib/cas-client-core-3.1.12.jar lib
	test -f web.xml.orig || cp web.xml web.xml.orig
	cp ${SCRIPT_HOME}/${SOURCE_HOME}/manual_config/share.war/web.xml web.xml.sso
	cp ${SCRIPT_HOME}/${SOURCE_HOME}/manual_config/share.war/web.xml web.xml

	cd ${ALF_HOME}/tomcat/webapps/alfresco/WEB-INF
	test -f web.xml.orig || cp web.xml web.xml.orig
	cp ${SCRIPT_HOME}/${SOURCE_HOME}/manual_config/alfresco.war/web.xml web.xml.sso
	cp ${SCRIPT_HOME}/${SOURCE_HOME}/manual_config/alfresco.war/web.xml web.xml

	cd ${ALF_HOME}/tomcat/shared/classes
	grep external alfresco-global.properties
	if [ $? -ne 0 ]
	then
		echo "### Enable CAS SSO Authentication via EXTERNAL subsystem" >> alfresco-global.properties
		echo "authentication.chain=cas:external" >> alfresco-global.properties
	fi
fi
#End sso section
service alfresco restart
