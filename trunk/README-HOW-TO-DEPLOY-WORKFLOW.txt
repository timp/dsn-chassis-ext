Login as admin then go to http://localhost:8080/alfresco/faces/jsp/admin/workflow-console.jsp

Command to deploy:
    deploy alfresco/module/org_wwarn_module_cms/workflows/clinical_study/processdefinition.xml

    Note. The group "Data Managers" must exist, with at least one member, as the workflow will assign tasks to members of it.

Command to view deployed process definition:
    show file alfresco/module/org_wwarn_module_cms/workflows/clinical_study/processdefinition.xml

