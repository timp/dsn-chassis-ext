var renditionDefName = "cm:adHocRenditionDef";
    var renderingEngineName = "xsltRenderingEngine";
//Rendering Actions/style.xsl
//Company Home/Rendering Actions Space/study.xsl
    var xsltNode = "workspace://SpacesStore/c8a93433-c094-42e5-9372-80fc994962db";
    var destination = document.displayPath + "/" + document.name + "rendered.html";

    var renditionDef = renditionService.createRenditionDefinition(renditionDefName, renderingEngineName);

    renditionDef.parameters["template_node"] = search.findNode(xsltNode);
    renditionDef.parameters["destination-path-template"] = destination;

    renditionDef.execute(document);