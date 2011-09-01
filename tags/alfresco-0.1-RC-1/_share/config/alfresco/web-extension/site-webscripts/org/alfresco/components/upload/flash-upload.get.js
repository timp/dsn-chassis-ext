function getContentTypes()
{
	var contentTypes = [
		{ id: "cm:content", value: "cm_content" },
        { id: "wc:dataFile", value: "type.wc_dataFile" },
        { id: "wc:dataDictionary", value: "type.wc_dataDictionary" },
        { id: "wc:protocol", value: "type.wc_protocol" },
        { id: "wc:publication", value: "type.wc_publication" },
        { id: "wc:other", value: "type.wc_other" },
        { id: "wc:output", value: "type.wc_output" },
        { id: "wc:studyInfo", value: "type.wc_studyInfo" }
		];
	return contentTypes;
}

//var derivedFileNodes = [
//    { nodeRef: "7272-2929292-2929292-29929", name: "SomeDoc.doc", type: "Data File", fileId : "3736272a-2921-2203-b625-92ab222b222a" },
  //  { nodeRef: "7272-2929292-2929292-29222", name: "SomeCal.xls", type: "Data Dictionary", fileId : "27383d2a-2543-a222-b621-91aa1122b22a" }
    //];

// Not used at the moment as we use a DataSource that is calling the studyfiles web script via AJAX
//var connector = remote.connect("alfresco");
//var studyFiles = connector.get("/wwarn/studyfiles?folderPath=/WWARN/Studies/ABCDE");

// create json object from data
//var studyFilesJson = eval('(' + studyFiles + ')');

model.contentTypes = getContentTypes();
//model.derivedFileList = studyFilesJson["studyFiles"];
//model.derivedFileList = derivedFileNodes;