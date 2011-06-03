var folder = null;
if (args.folderPath == null) {
    folder = search.findNode(args.folderNodeRef);
} else {
    folder = companyhome.childByNamePath(args.folderPath);
}
var studyFileNodes = new Array();
var allNodesInFolder = folder.children;
for each (node in allNodesInFolder) {
    if (node.isSubType("wc:studyFile")) {
        studyFileNodes.push(node);
    }
}
model.studyFiles = studyFileNodes;