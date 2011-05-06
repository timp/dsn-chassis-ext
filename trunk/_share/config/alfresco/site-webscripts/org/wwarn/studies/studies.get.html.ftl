<div class="dashlet">
  <div class="title">${msg("title")}</div>
  <div class="body scrollableList">
    <#list results.entries as study>
    <div class="detail-list-item">
      <#assign cmisra_object=study.getExtension(atom.createQName("http://docs.oasis-open.org/ns/cmis/restatom/200908/","object"))>      
      <div class="details">
       <h4>${study.title}</h4>
        <h4>${cmisra_object}</h4>
      </div>
      
    </div>
    </#list>
  </div>
</div>