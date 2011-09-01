var studyFileNodeRef = args.studyFileNodeRef;
var comment = args.comment;

if (studyFileNodeRef == null || comment == null) {
    model.message = "Could not setup derivations aspect with comment, studyFileNodeRef is null or comment is null";
} else {
    var studyFileNode = search.findNode(studyFileNodeRef);
    var properties = new Array(1);
    properties["wc:derivationsComments"] = comment;
    studyFileNode.addAspect("wc:derivations", properties);
    model.message = "Successfully setup derivations aspect with comment for studyFileNodeRef (" + studyFileNodeRef + ")";
}
