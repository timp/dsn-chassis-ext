function getContentTypes()
{
	var contentTypes = [
		{ id: "cm:content", value: "cm_content" },
        { id: "wc:data_file", value: "type.wc_data_file" },
        { id: "wc:data_dictionary", value: "type.wc_data_dictionary" },
        { id: "wc:protocol", value: "type.wc_protocol" },
        { id: "wc:publication", value: "type.wc_publication" },
        { id: "wc:other", value: "type.wc_other" },
        { id: "wc:output", value: "type.wc_output" },
        { id: "wc:study_info", value: "type.wc_study_info" }
		];
	return contentTypes;
}

model.contentTypes = getContentTypes();