# Introduction #

Lines to add to tomcat/shared/classes/alfresco-global.properties


Edit as appropriate for hosts, users and passwords
# Details #

```
authentication.chain=cas:external,drupal:drupal,alfrescoNtlm1:alfrescoNtlm

#To enable WebDAV and user synchronization with the authentication database
drupal.db.driver=org.gjt.mm.mysql.Driver
drupal.db.username=
drupal.db.password=
drupal.db.url=jdbc:mysql://dbhost:3306/wwarn_drupal

#To enable google docs integration
googledocs.googleeditable.enabled=true
googledocs.username=
googledocs.password=
googledocs.application.name=Alfresco ECM system
googledocs.url=https://docs.google.com/feeds/default/private/full
googledocs.spreadsheet.service.name=wise
#log4j.logger.org.alfresco.repo.googledocs=debug

#
# Outbound Email Configuration
#-------------
mail.host=localhost
mail.port=25
#mail.username=anonymous
#mail.password=
mail.encoding=UTF-8
mail.from.default=
mail.smtp.auth=false

```