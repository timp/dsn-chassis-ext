# Introduction #

This page describes how to deploy a workflow and how to trigger it once it has been deployed.


# Details #
http://wiki.alfresco.com/wiki/WorkflowAdministration#Manually_deploying_a_jPDL_XML_file_or_Process_Archive
## Deployment ##

The first time the application is installed these workflows will be automatically installed.

Login as admin then go to http://localhost:8080/alfresco/faces/jsp/admin/workflow-console.jsp

Command to deploy:
```
    deploy alfresco/module/org_wwarn_module_cms/workflows/clinical_study/processdefinition.xml
```
Note. The group "Data Managers" must exist, with at least one member, as the workflow will assign tasks to members of it.

```
    deploy alfresco/module/org_wwarn_module_cms/workflows/molecular_study/processdefinition.xml
```
Note. The group "BioInformatics" must exist, with at least one member, as the workflow will assign tasks to members of it.

```
    deploy alfresco/module/org_wwarn_module_cms/workflows/invitro_study/processdefinition.xml
```

```
    deploy alfresco/module/org_wwarn_module_cms/workflows/pk_study/processdefinition.xml
```

Command to view deployed process definition:
```
    show file alfresco/module/org_wwarn_module_cms/workflows/clinical_study/processdefinition.xml
```

## Templates ##
Ensure that the alfresco box can send mail.
Eg install postfix (as per https://sysadmin.cggh.org/drupal6/node/167) to enable email.
The following email templates are used and should be uploaded through the share interface to Repository> Data Dictionary> Email Templates:
```
Data Dictionary/Email Templates/clinical_wwarn_mapping_info_request_email.ftl
Data Dictionary/Email Templates/clinical_wwarn_output_info_to_contributor_email.ftl
```
```
Data Dictionary/Email Templates/molecular_wwarn_mapping_info_request_email.ftl
Data Dictionary/Email Templates/molecular_wwarn_output_info_to_contributor_email.ftl
```

```
Data Dictionary/Email Templates/iv_wwarn_mapping_info_request_email.ftl
Data Dictionary/Email Templates/iv_wwarn_output_info_to_contributor_email.ftl
```

```
Data Dictionary/Email Templates/pk_wwarn_mapping_info_request_email.ftl
Data Dictionary/Email Templates/pk_wwarn_output_info_to_contributor_email.ftl
```
## Starting from the command line ##
Where study.xml is the file containing the atom entry for the study
```
curl -v -u user:password -d @study.xml -H "Content-Type:application/atom+xml" http://localhost:8080/alfresco/service/wwarn/startworkflow?workflow=ww:clinicalStudyProcess
```