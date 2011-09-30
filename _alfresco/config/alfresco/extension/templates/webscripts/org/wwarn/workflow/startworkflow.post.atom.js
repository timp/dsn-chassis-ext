var postedObject = entry;
var workflow = args['workflow'];

function startWorkflow(workflowName, studyId, author, chassis) {
var studyFolderPath = '/WWARN/Studies/' + studyId;

var folder = null;
if (studyFolderPath != null) {
    folder = companyhome.childByNamePath(studyFolderPath);
}
model.folder = folder;
if (workflowName == null || workflowName == "") {
	status.code = 500;
    status.message = "Could not start workflow, {workflowName} has not been specified";
} else if (studyId == null || studyId == "") {
	status.code = 500;
    status.message = "Could not start workflow, {studyId} has not been specified";
} else {

    var workflow = actions.create("start-workflow");
    workflow.parameters.workflowName = "jbpm$" + workflowName;
    //workflow.parameters["bpm:workflowDescription"] = upload.name;
    workflow.parameters["ww:studyID"] = studyId;
    workflow.parameters["ww:studyFolderLink"] = '/share/page/folder-details?nodeRef=' + folder.nodeRef;
    workflow.parameters["wc:studyInfoLink"] = '/repository/study/dashboard?tab=study-info&study=' + chassis;
    //Used to send email
    workflow.parameters["ww:contributorEmail"] = author;
    //workflow.parameters["wc:modules"] = modules;
    //workflow.parameters["wc:region"] = region;
    //workflow.parameters["wc:countries"] = countries;
    workflow.execute(folder);

    status.code = 201;
    status.message = "Started workflow " + workflowName + " for study " + studyId;
}
}
var studyId = postedObject.title;
var chassis = postedObject.id;
var author = postedObject.author.email;
startWorkflow(workflow, studyId, author, chassis);
model.message = status.message;
model.workflow = workflow;
model.study = studyId;
model.chassis = chassis;
logger.log("Message: " + status.message);
