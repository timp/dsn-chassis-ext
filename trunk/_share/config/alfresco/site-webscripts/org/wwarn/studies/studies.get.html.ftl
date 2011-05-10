<script type="text/javascript">//<![CDATA[
   var studiesDashlet = new Alfresco.dashlet.MyStudies("${args.htmlid}").setOptions(
   {
      imapEnabled: ${imapServerEnabled?string},
      sites: [
<#if sites??>
   <#list sites as site>
      {
         sitePreset: '${site.sitePreset?js_string}',
         shortName: '${site.shortName?js_string}',
         title: '${site.title?js_string}',
         description: '${site.description?js_string}',
         isFavourite: ${site.isFavourite?string},
         <#if imapServerEnabled>isIMAPFavourite: ${site.isIMAPFavourite?string},</#if>
         isSiteManager: ${site.isSiteManager?string}
      }<#if (site_has_next)>,</#if>
   </#list>
</#if>
      ]
   }).setMessages(${messages});
   new Alfresco.widget.DashletResizer("${args.htmlid}", "${instance.object.id}");
   //Initialize the data
   studiesDashlet.onTypeFilterClicked();
//]]></script>

<div class="dashlet my-studies">
   <div class="title">${msg("header.mySites")}</div>
   <div class="toolbar flat-button">
      
      <input id="${args.htmlid}-type" type="button" name="type" value="${msg("filter.all")}" />
      <select id="${args.htmlid}-type-menu">
         <option value="all">${msg("filter.all")}</option>
         <option value="favs">${msg("filter.favs")}</option>                
         <option value="clinical">${msg("filter.clinical")}</option>
         <option value="pk">${msg("filter.pk")}</option>
         <option value="molecular">${msg("filter.molecular")}</option>
         <option value="iv">${msg("filter.iv")}</option>        
      </select>
   </div>
<#if sites??>
   <div id="${args.htmlid}-sites" class="body scrollableList" <#if args.height??>style="height: ${args.height}px;"</#if>>
<#else>
   <div class="body scrollableList" <#if args.height??>style="height: ${args.height}px;"</#if>>
      <div class="detail-list-item first-item last-item">
         <span>${msg("label.noSites")} some text</span>
      </div>
</#if>

   </div>
</div>