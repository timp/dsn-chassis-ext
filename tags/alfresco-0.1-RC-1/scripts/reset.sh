ALF_VERSION=alfresco-3.4.2
ALF_HOME=/opt/${ALF_VERSION}
	cd ${ALF_HOME}/tomcat/webapps/share/WEB-INF
	cp web.xml.orig web.xml
	cd ${ALF_HOME}/tomcat/webapps/alfresco/WEB-INF
	cp web.xml.orig web.xml
	cd ${ALF_HOME}/tomcat/shared/classes
	grep external alfresco-global.properties
	if [ $? -ne 0 ]
	then
		echo "### Enable CAS SSO Authentication via EXTERNAL subsystem" >> alfresco-global.properties
		echo "authentication.chain=cas:external" >> alfresco-global.properties
	fi
