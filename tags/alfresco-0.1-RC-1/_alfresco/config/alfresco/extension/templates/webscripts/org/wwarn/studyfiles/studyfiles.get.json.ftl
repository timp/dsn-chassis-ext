{
"studyFiles" : [
<#list studyFiles as studyFile>
{
   "nodeRef": "${studyFile.nodeRef}",
   "name": "${jsonUtils.encodeJSONString(studyFile.name)}",
   "type": "${studyFile.type}",
   "fileId": "${studyFile.properties["wc:fileId"]!''}"
}<#if studyFile_has_next>,</#if>
</#list>
]
}
