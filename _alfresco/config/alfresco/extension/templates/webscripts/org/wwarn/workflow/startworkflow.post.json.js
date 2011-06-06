var postedObject = eval('(' + json + ')');

var workflowName = postedObject.workflowName;
var study = postedObject.study;
var studyId = study.id;
var studyFolderPath = study.folderPath;
var modules = study.modules;
var region = study.region;
var countries = study.countries;
var contributorEmail = study.contributorEmail;

var folder = null;
if (studyFolderPath != null) {
    folder = companyhome.childByNamePath(studyFolderPath);
}

if (folder == null) {
    model.message = "Could not start workflow, study folder could not be found, specify correct {studyFolderPath}";
} else if (workflowName == null || workflowName == "") {
    model.message = "Could not start workflow, {workflowName} has not been specified";
} else if (studyId == null || studyId == "") {
    model.message = "Could not start workflow, {studyId} has not been specified";
} else {
    var workflow = actions.create("start-workflow");
    workflow.parameters.workflowName = "jbpm$" + workflowName;
    //workflow.parameters["bpm:workflowDescription"] = upload.name;
    workflow.parameters["ww:studyID"] = studyId;
    workflow.parameters["ww:contributorEmail"] = contributorEmail;
    workflow.parameters["wc:modules"] = modules;
    workflow.parameters["wc:region"] = region;
    workflow.parameters["wc:countries"] = countries;
    workflow.execute(folder);

    model.message = "Started workflow " + workflowName + " for study " + studyId;
}

logger.log("Message: " + model.message);