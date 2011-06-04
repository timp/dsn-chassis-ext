var outputFileNodeRef = args.outputFileNodeRef;
var studyFileNodeRef = args.studyFileNodeRef;

if (outputFileNodeRef == null || studyFileNodeRef == null) {
    model.message = "Could not setup derived file association, outputFileNodeRef is null or studyFileNodeRef is null";
} else {
    var outputFileNode = search.findNode(outputFileNodeRef);
    var studyFileNode = search.findNode(studyFileNodeRef);
    studyFileNode.createAssociation(outputFileNode, "wc:derivations");
    model.message = "Successfully setup association between studyFileNodeRef (" + studyFileNodeRef + ") and outputFileNodeRef (" + outputFileNodeRef + ")";
}
