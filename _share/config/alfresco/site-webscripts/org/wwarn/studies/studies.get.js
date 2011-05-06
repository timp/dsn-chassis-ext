var conn = remote.connect("alfresco");
// search cmis search api path
var id = "/cmis/queries";

  var selectString = "SELECT * FROM wc:study_folder";
  var cmisQuery = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?><query xmlns:cmis=\"http://docs.oasis-open.org/ns/cmis/core/200908/\"><statement>" + selectString + "</statement><pageSize>" + 0 + "</pageSize></query>";
  
  // get the documents for the currently active site
  var conn = remote.connect("alfresco");
  var queryResult = conn.post(stringUtils.urlEncodeComponent(id), cmisQuery, "application/cmisquery+xml");
  // sitesdocuments[0] = "{name: " + sitename + ", documents: " + atom.toFeed(queryResult) + "}";
//  var studies=atom.toFeed(queryResult);

model.results = atom.toFeed(queryResult);
//model.studies=studies;
