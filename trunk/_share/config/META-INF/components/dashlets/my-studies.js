/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Dashboard MyStudies component.
 *
 * @namespace Alfresco.dashlet
 * @class Alfresco.dashlet.MyStudies
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML;

   /**
    * Dashboard MyStudies constructor.
    *
    * @param {String} htmlId The HTML id of the parent element
    * @return {Alfresco.dashlet.MyStudies} The new component instance
    * @constructor
    */
   Alfresco.dashlet.MyStudies = function MyStudies_constructor(htmlId)
   {
      Alfresco.dashlet.MyStudies.superclass.constructor.call(this, "Alfresco.dashlet.MyStudies", htmlId, ["button", "container", "datasource", "datatable", "animation"]);

      // Initialise prototype properties
      this.preferencesService = new Alfresco.service.Preferences();
      return this;
   };

   YAHOO.extend(Alfresco.dashlet.MyStudies, Alfresco.component.Base,
   {
      /**
       * CreateSite module instance.
       *
       * @property createSite
       * @type Alfresco.module.CreateSite
       */
      createSite: null,

      /**
       * Object container for initialization options
       *
       * @property options
       * @type object
       */
      options:
      {
         /**
          * Site data
          *
          * @property sites
          * @type array
          */
         sites: [],

         /**
          * Flag if IMAP server is enabled
          *
          * @property imapEnabled
          * @type boolean
          * @default false
          */
         imapEnabled: false
      },

      /**
       * Date drop-down changed event handler
       * @method onTypeFilterChanged
       * @param p_oMenuItem {object} Selected menu item
       */
      onTypeFilterChanged: function MyStudies_onTypeFilterChanged(p_oMenuItem)
      {
         this.widgets.type.value = p_oMenuItem.value;
         this.onTypeFilterClicked();
      },

      /**
       * Type button clicked event handler
       * @method onTypeFilterClicked
       * @param p_oEvent {object} Dom event
       */
      onTypeFilterClicked: function MyStudies_onTypeFilterClicked(p_oEvent)
      {
    	 /* This query doesn't actually work quite right and the GET is easier and does the job
    	 var query = 'select f.*, a.*, t.* from wc:studyFolder as f join wc:studyFolderData as a on f.cmis:objectid = a.cmis:objectid';
 		 query += ' join cm:titled as t on f.cmis:objectid = t.cmis:objectid';
    	 var xmlquery = '<cmis:query xmlns:cmis="http://docs.oasis-open.org/ns/cmis/core/200908/"><cmis:statement><![CDATA['+query+']]></cmis:statement></cmis:query>';
    	  */
    	  Alfresco.util.Ajax.request(
    		         {
    		        	 /*
    		            url: Alfresco.constants.PROXY_URI + "cmis/queries",
    		            method: "POST",
    		            dataStr: xmlquery,
    		            requestContentType: 'application/cmisquery+xml',
    		            */
    		        	 url: Alfresco.constants.PROXY_URI + "cmis/p/WWARN/Studies/children",
    		            successCallback:
    		            {
    		               fn: this.getPrefs,
    		               scope: this
    		            }
    		         });
      },
      
      getJson: function MyStudies_getJson(p_response)
      {
    	  
          var entries = p_response.getElementsByTagName('entry'),
          entriesLength = entries.length,
          entryEl = null,
          objEl,
          propertiesEl,
          propertiesList,
          propertyEl,
          articles = [],
          article;

      // Convert to object format similar to the json response
      for (var ei = 0; ei < entriesLength; ei++)
      {
         entryEl = entries[ei];
         article = {
            properties: {},
            siteManagers: {},
            modules: [],
            administrators: []
         };
         var objEl = entryEl.getElementsByTagNameNS('http://docs.oasis-open.org/ns/cmis/restatom/200908/','object');
         var i = 0;
         var propsNS = [ 'http://www.alfresco.org', 'http://docs.oasis-open.org/ns/cmis/core/200908/'];
         while ((ns = propsNS[i++])) {
        	 var properties = entryEl.getElementsByTagNameNS(ns,'properties');
        	 var propertyEl = properties[0].firstElementChild;
        	 while(propertyEl != null)
        	 {  
        		 var propertyDefinitionId = propertyEl.getAttribute("propertyDefinitionId");
        		 if (propertyDefinitionId == "cmis:name") {
        			 article.shortName = propertyEl.firstChild.firstChild.nodeValue;
        		 } else if (propertyDefinitionId == "cm:title") {
        			 if (propertyEl.firstChild.firstChild) {
        				 article.title = propertyEl.firstChild.firstChild.nodeValue;
        			 }
        		 } else if (propertyDefinitionId == "cm:description") {
        			 if (propertyEl.firstChild.firstChild) {
        				 article.description = propertyEl.firstChild.firstChild.nodeValue;
        			 }
        		 } else if (propertyDefinitionId == "cmis:lastModifiedBy") {
        			 article.modifiedByUser = propertyEl.firstChild.firstChild.nodeValue;
        			 article.modifiedBy = article.modifiedByUser;
        		 } else if (propertyDefinitionId == "cmis:lastModificationDate") {
        			 article.modifiedOn = propertyEl.firstChild.firstChild.nodeValue;
        		 } else if (propertyDefinitionId == "cmis:objectId") {
        			 article.objectId = propertyEl.firstChild.firstChild.nodeValue;
        		 } else if (propertyDefinitionId == "cmis:path") {
        			 article.path = propertyEl.firstChild.firstChild.nodeValue;
        		 } else if (propertyDefinitionId == "cmis:creationDate") {
        			 article.createdOn = propertyEl.firstChild.firstChild.nodeValue;
        		 } else if (propertyDefinitionId == "cmis:objectId") {
        			 article.sitePreset = propertyEl.firstChild.firstChild.nodeValue;
        		 } else if (propertyDefinitionId == "wc:studyInfoLink") {
        			 if (propertyEl.firstChild && propertyEl.firstChild.firstChild) {
        				 article.chassisLink = propertyEl.firstChild.firstChild.nodeValue;
        			 }
        		 } else if (propertyDefinitionId == "wc:modules") {
        			 var mods = propertyEl.firstElementChild;
        			 while (mods != null) {
        				 if (mods.firstChild != null) {
        					 article.modules.push(mods.firstChild.nodeValue);
        				 }
        				 mods = mods.nextElementSibling;
        			 }
        		 } else if (propertyDefinitionId == "wc:admins") {
        			 var admins = propertyEl.firstElementChild;
        			 while (admins != null) {
        				 article.administrators.push(admins.firstChild.nodeValue);
        				 admins = admins.nextElementSibling;
        			 }
        		 }

        		 propertyEl = propertyEl.nextElementSibling;
        	 }
         }
         articles.push(article);
      }
      return articles;  
      },
      /**
       * Retrieve user preferences
       * @method getPrefs
       * @param p_response {object} Response from "api/people/{userId}/sites" query
       */
      getPrefs: function MyStudies_getPrefs(p_response)
      {
         //Convert CMIS response to json
    	  var items = this.getJson(p_response.serverResponse.responseXML);

         Alfresco.util.Ajax.request(
         {
            url: Alfresco.constants.PROXY_URI + "api/people/"+ encodeURIComponent(Alfresco.constants.USERNAME) + "/preferences?pf=org.wwarn.share.studies",
            successCallback:
            {
               fn: this.onSitesUpdate,
               scope: this,
               obj: items
            }
         });
      },

      /**
       * Process response from sites and preferences queries
       * @method onSitesUpdate
       * @param p_response {object} Response from "api/people/{userId}/preferences" query
       * @param p_items {object} Response from "api/people/{userId}/sites" query
       */
      onSitesUpdate: function MyStudies_onSitesUpdate(p_response, p_items)
      {
         var favSites = {},
            imapfavSites = {},
            siteManagers, i, j, k, l,
            ii = 0;

         if (p_response.json.org)
         {
            favSites = p_response.json.org.wwarn.share.studies.favourites;
            imapfavSites = p_response.json.org.wwarn.share.studies.imapFavourites;
         }

         for (i = 0, j = p_items.length; i < j; i++)
         {
            p_items[i].isSiteManager = false;
            siteManagers = p_items[i].siteManagers;
            for (k = 0, l = siteManagers.length; siteManagers && k < l; k++)
            {
               if (siteManagers[k] == Alfresco.constants.USERNAME)
               {
                  p_items[i].isSiteManager = true;
                  break;
               }
            }

            p_items[i].isFavourite = typeof(favSites[p_items[i].shortName]) == "undefined" ? false : favSites[p_items[i].shortName];
            if (imapfavSites)
            {
               p_items[i].isIMAPFavourite = typeof(imapfavSites[p_items[i].shortName]) == "undefined" ? false : imapfavSites[p_items[i].shortName];
            }
         }

         this.options.sites = [];
         for (i = 0, j = p_items.length; i < j; i++)
         {
            var site =
            {
               shortName: p_items[i].shortName,
               title: p_items[i].title,
               objectId: p_items[i].objectId,
               path: p_items[i].path,
               description: p_items[i].description,
               createdOn: p_items[i].createdOn,
               administrators: p_items[i].administrators,
               modules: p_items[i].modules,
               chassisLink: p_items[i].chassisLink,
               isFavourite: p_items[i].isFavourite,
               isIMAPFavourite: p_items[i].isIMAPFavourite,
               sitePreset: p_items[i].sitePreset,
               isSiteManager: p_items[i].isSiteManager
            };

            if (this.filterAccept(site))
            {
               this.options.sites[ii] = site;
               ii++;
            }
         }

         var successHandler = function MD__oFC_success(sRequest, oResponse, oPayload)
         {
            oResponse.results=this.options.sites;
            this.widgets.dataTable.onDataReturnInitializeTable.call(this.widgets.dataTable, sRequest, oResponse, oPayload);
         };

         if (this.widgets.dataSource) {
        	 this.widgets.dataSource.sendRequest(this.options.sites,
        		 {
        		 	success: successHandler,
        		 	scope: this
        		 });
         }
      },

      /**
       * Determine whether a given site should be displayed or not depending on the current filter selection
       * @method filterAccept
       * @param site {object} Site object literal
       * @return {boolean}
       */
      filterAccept: function MyStudies_filterAccept(site)
      {
         var type = "all";
         if (this.widgets.type) {
        	 type = this.widgets.type.value;
         }
    	 switch (type)
         {
            case "all":
               return true;

            case "sites":
               return (site.sitePreset !== "document-workspace" && site.sitePreset !== "meeting-workspace");

            case "favs":
               return (site.isFavourite || (this.options.imapEnabled && site.isIMAPFavourite));

            case "clinical":

            	  var i = site.modules.length;
            	  while (i--) {
            	    if (site.modules[i] === 'Clinical') {
            	      return true;
            	    }
            	  }
            	  return false;
            case "pk":

          	  var i = site.modules.length;
          	  while (i--) {
          	    if (site.modules[i] === 'Pharmacology') {
          	      return true;
          	    }
          	  }
          	  return false;
            case "molecular":

          	  var i = site.modules.length;
          	  while (i--) {
          	    if (site.modules[i] === 'Molecular') {
          	      return true;
          	    }
          	  }
          	  return false;
            case "iv":

          	  var i = site.modules.length;
          	  while (i--) {
          	    if (site.modules[i] === 'In vitro') {
          	      return true;
          	    }
          	  }
          	  return false;
            
         }
         return false;
      },

      /**
       * Fired by YUI when parent element is available for scripting
       * @method onReady
       */
      onReady: function MyStudies_onReady()
      {
         var me = this;

         // Dropdown filter
         this.widgets.type = new YAHOO.widget.Button(this.id + "-type",
         {
            type: "split",
            menu: this.id + "-type-menu"
         });

         this.widgets.type.on("click", this.onTypeFilterClicked, this, true);
         this.widgets.type.getMenu().subscribe("click", function (p_sType, p_aArgs)
         {
            var menuItem = p_aArgs[1];
            if (menuItem)
            {
               me.widgets.type.set("label", menuItem.cfg.getProperty("text"));
               me.onTypeFilterChanged.call(me, p_aArgs[1]);
            }
         });
         this.widgets.type.value = "all";
       
         // DataSource definition
         this.widgets.dataSource = new YAHOO.util.DataSource(this.options.sites,
         {
            responseType: YAHOO.util.DataSource.TYPE_JSARRAY
         });

         /**
          * Use the getDomId function to get some unique names for global event handling
          */
         var favEventClass = Alfresco.util.generateDomId(null, "fav-site"),
            imapEventClass = Alfresco.util.generateDomId(null, "imap-site");

         /**
          * Favourites custom datacell formatter
          */
         var renderCellFavourite = function MS_oR_renderCellFavourite(elCell, oRecord, oColumn, oData)
         {
            Dom.setStyle(elCell, "width", oColumn.width + "px");
            Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

            var isFavourite = oRecord.getData("isFavourite"),
               isIMAPFavourite = oRecord.getData("isIMAPFavourite");

            var desc = '<div class="study-favourites">';
            desc += '<a class="favourite-study ' + favEventClass + (isFavourite ? ' enabled' : '') + '" title="' + me.msg("link.favouriteSite") + '">&nbsp;</a>';
            if (me.options.imapEnabled)
            {
               desc += '<a class="imap-favourite-study ' + imapEventClass + (isIMAPFavourite ? ' imap-enabled' : '') + '" title="' + me.msg("link.imap_favouriteSite") + '">&nbsp;</a>';
            }
            desc += '</div>';
            elCell.innerHTML = desc;
         };

         /**
          * Name & description custom datacell formatter
          */
         var renderCellID = function MS_oR_renderCellID(elCell, oRecord, oColumn, oData)
         {
            var siteId = oRecord.getData("shortName"),
            path = oRecord.getData("path");
            objectId = oRecord.getData("objectId");

            var desc = '<div class="study-id"><a href="' + Alfresco.constants.URL_PAGECONTEXT + 'repository#filter=path|' + path + '" class="theme-color-1">' + $html(siteId) + '</a></div>';

            elCell.innerHTML = desc;
         };
         /**
          * Name & description custom datacell formatter
          */
         var renderCellName = function MS_oR_renderCellName(elCell, oRecord, oColumn, oData)
         {
            var siteId = oRecord.getData("shortName"),
            	objectId = oRecord.getData("objectId"),
            	siteTitle = oRecord.getData("title"),
            	siteDescription = oRecord.getData("description");

            var desc = '<div class="study-title"><a href="' + Alfresco.constants.URL_PAGECONTEXT + 'folder-details?nodeRef=' + objectId + '" class="theme-color-1">' + $html(siteTitle) + '</a></div>';
            desc += '<div class="study-description">' + $html(siteDescription) + '</div>';

            elCell.innerHTML = desc;
         };

         /**
          * Name & description custom datacell formatter
          */
         var renderCellCreated = function MS_oR_renderCellCreated(elCell, oRecord, oColumn, oData)
         {
            var createdOn = oRecord.getData("createdOn");
            var formattedDate = '';
            var format = { format: "%Y-%m-%d" };
            var date = new Date(createdOn);
                
            // date is a JavaScript Date object
            formattedDate = YAHOO.util.Date.format(date, format);
           
            var desc = '<div class="study-created">' + $html(formattedDate) + '</div>';

            elCell.innerHTML = desc;
         };
         /**
          * Name & description custom datacell formatter
          */
         var renderCellAdmins = function MS_oR_renderCellAdmins(elCell, oRecord, oColumn, oData)
         {
            var admins = oRecord.getData("administrators");

            var desc = '<div class="study-admins"><ul>';
            for(var i = 0,l=admins.length;i<l;i++) {
            	desc += '<li>' + $html(admins[i]) + '</li>';
            }
            desc +=  '</ul></div>';
            

            elCell.innerHTML = desc;
         };
         /**
          * Actions custom datacell formatter
          */
         var renderCellActions = function MS_oR_renderCellActions(elCell, oRecord, oColumn, oData)
         {  
            var chassisLink = oRecord.getData("chassisLink");
        	var desc = '<div class="study-title"><a href="' + chassisLink + '" class="theme-color-1">' + me.msg("link.chassis")  + '</a></div>';
        
            elCell.innerHTML = desc;
         };
       
         // DataTable column defintions
         var columnDefinitions =
         [
            { key: "siteId", label: this.msg("head.favs"), sortable: false, formatter: renderCellFavourite, width: this.options.imapEnabled ? 40 : 20 },
            { key: "id", label: this.msg("head.id"), sortable: true, formatter: renderCellID },
            { key: "title", label: this.msg("head.title"), sortable: true, formatter: renderCellName },
            { key: "created", label: this.msg("head.created"), sortable: true, formatter: renderCellCreated },
            { key: "administrators", label: this.msg("head.admins"), sortable: true, formatter: renderCellAdmins },
            { key: "description", label: this.msg("head.actions"), sortable: false, formatter: renderCellActions }
         ];

         // DataTable definition
         this.widgets.dataTable = new YAHOO.widget.DataTable(this.id + "-sites", columnDefinitions, this.widgets.dataSource,
         {
            MSG_EMPTY: this.msg("label.noSites")
         });
         
         /**
          * Hook favourite site events
          */
         var registerEventHandler = function(cssClass, fnHandler)
         {
            var fnEventHandler = function MS_oR_fnEventHandler(layer, args)
            {
               var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "div");
               if (owner !== null)
               {
                  fnHandler.call(me, args[1].target.offsetParent, owner);
               }

               return true;
            };
            YAHOO.Bubbling.addDefaultAction(cssClass, fnEventHandler);
         };

         registerEventHandler(favEventClass, this.onFavouriteSite);
         registerEventHandler(imapEventClass, this.onImapFavouriteSite);

         // Enable row highlighting
         this.widgets.dataTable.subscribe("rowMouseoverEvent", this.widgets.dataTable.onEventHighlightRow);
         this.widgets.dataTable.subscribe("rowMouseoutEvent", this.widgets.dataTable.onEventUnhighlightRow);
      },
     
      /**
       * Adds an event handler that adds or removes the site as favourite site
       *
       * @method onFavouriteSite
       * @param row {object} DataTable row representing file to be actioned
       */
      onFavouriteSite: function MyStudies_onFavouriteSite(row)
      {
         var record = this.widgets.dataTable.getRecord(row),
            site = record.getData(),
            siteId = site.shortName;

         site.isFavourite = !site.isFavourite;

         this.widgets.dataTable.updateRow(record, site);

         // Assume the call will succeed, but register a failure handler to replace the UI state on failure
         var responseConfig =
         {
            failureCallback:
            {
               fn: function MS_oFS_failure(event, obj)
               {
                  // Reset the flag to it's previous state
                  var record = obj.record,
                     site = record.getData();

                  site.isFavourite = !site.isFavourite;
                  this.widgets.dataTable.updateRow(record, site);
                  Alfresco.util.PopupManager.displayPrompt(
                  {
                     text: this.msg("message.siteFavourite.failure", site.title)
                  });
               },
               scope: this,
               obj:
               {
                  record: record
               }
            },
            successCallback:
            {
               fn: function MS_oFS_success(event, obj)
               {
                  var record = obj.record,
                     site = record.getData();

                  YAHOO.Bubbling.fire(site.isFavourite ? "favouriteSiteAdded" : "favouriteSiteRemoved", site);
               },
               scope: this,
               obj:
               {
                  record: record
               }
            }
         };

         this.preferencesService.set('org.wwarn.share.studies.favourites' + "." + siteId, site.isFavourite, responseConfig);
      },

      /**
       * Adds an event handler that adds or removes the site as favourite site
       *
       * @method _addImapFavouriteHandling
       * @param row {object} DataTable row representing file to be actioned
       */
      onImapFavouriteSite: function MyStudies_onImapFavouriteSite(row)
      {
         var record = this.widgets.dataTable.getRecord(row),
            site = record.getData(),
            siteId = site.shortName;

         site.isIMAPFavourite = !site.isIMAPFavourite;

         this.widgets.dataTable.updateRow(record, site);

         // Assume the call will succeed, but register a failure handler to replace the UI state on failure
         var responseConfig =
         {
            failureCallback:
            {
               fn: function MS_oIFS_failure(event, obj)
               {
                  // Reset the flag to it's previous state
                  var record = obj.record,
                     site = record.getData();

                  site.isIMAPFavourite = !site.isIMAPFavourite;
                  this.widgets.dataTable.updateRow(record, site);
                  Alfresco.util.PopupManager.displayPrompt(
                  {
                     text: this.msg("message.siteFavourite.failure", site.title)
                  });
               },
               scope: this,
               obj:
               {
                  record: record
               }
            }
         };

         this.preferencesService.set(Alfresco.service.Preferences.IMAP_FAVOURITE_SITES + "." + siteId, site.isIMAPFavourite, responseConfig);
      },

      /**
       * Fired by YUI Link when the "Create site" label is clicked
       * @method onCreateSiteLinkClick
       * @param event {domEvent} DOM event
       */
      onCreateSiteLinkClick: function MyStudies_onCreateSiteLinkClick(event)
      {
         Alfresco.module.getCreateSiteInstance().show();
         Event.preventDefault(event);
      },

      /**
       * Searches the current recordSet for a record with the given parameter value
       *
       * @method _findRecordByParameter
       * @param p_value {string} Value to find
       * @param p_parameter {string} Parameter to look for the value in
       */
      _findRecordByParameter: function MyStudies__findRecordByParameter(p_value, p_parameter)
      {
        var recordSet = this.widgets.dataTable.getRecordSet();
        for (var i = 0, j = recordSet.getLength(); i < j; i++)
        {
           if (recordSet.getRecord(i).getData(p_parameter) == p_value)
           {
              return recordSet.getRecord(i);
           }
        }
        return null;
      }
   });
})();