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

model.contentTypes = getContentTypes();