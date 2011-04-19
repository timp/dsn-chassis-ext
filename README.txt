To deploy the document management code 
---------------------------------------
1) Copy tomcat/webapps/alfresco.war to alfresco.war.bak
2) Update the paths in build.properties
3) Stop Alfresco
4) Run the deploy-alfresco-amp ant target - produces build\dist\WWARN_Alfresco_Code-1.0.amp
5) Run the deploy-share-jar ant target - produces build\lib\WWARN_Share_Code-1.0.jar
6) Start Alfresco

These ant targets deploy the AMP and the JAR directly into the Alfresco installation.

If you just want to generate the AMP and the JAR do:

package-alfresco-amp - can then be installed with Module Management tool
package-share-jar - can be installed by just copying to webapps/share/WEB-INF/lib


