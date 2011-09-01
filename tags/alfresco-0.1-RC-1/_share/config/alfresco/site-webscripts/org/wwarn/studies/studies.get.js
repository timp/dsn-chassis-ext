const PREF_SITES = "org.alfresco.share.sites";
const PREF_FAVOURITE_SITES = PREF_SITES + ".favourites";
const PREF_IMAP_FAVOURITE_SITES = PREF_SITES + ".imapFavourites";

function main()
{
   var sites = [],
      imapServerEnabled = false;

   // Prepare the model for the template
   model.sites = sites;
   model.imapServerEnabled = imapServerEnabled;
}

main();