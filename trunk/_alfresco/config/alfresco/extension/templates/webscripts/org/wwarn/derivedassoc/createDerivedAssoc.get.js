var outputFileNodeRef = args.outputFileNodeRef;
var studyFileNodeRef = args.studyFileNodeRef;

if (outputFileNodeRef == null || studyFileNodeRef == null) {
    model.message = "Could not setup association, outputFileNodeRef is null or studyFileNodeRef is null";
} else {
    var outputFileNode = search.findNode(outputFileNodeRef);
    var studyFileNode = search.findNode(studyFileNodeRef);
    outputFileNode.createAssociation(studyFileNode, "wc:derivations");
    model.message = "Successfully setup association between outputFileNodeRef (" + outputFileNodeRef + ") and studyFileNodeRef (" + studyFileNodeRef + ")";
}
