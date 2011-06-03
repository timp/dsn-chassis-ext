<#assign el=args.htmlid?html>
<div id="${el}-dialog" class="flash-upload hidden">
    <div class="hd">
        <span id="${el}-title-span"></span>
    </div>
    <div class="bd">
        <div class="browse-wrapper">
            <div class="center">
                <div id="${el}-flashuploader-div" class="browse">${msg("label.noFlash")}</div>
                <div class="label">${msg("label.browse")}</div>
            </div>
        </div>
        <div class="tip-wrapper">
            <span id="${el}-multiUploadTip-span">${msg("label.multiUploadTip")}</span>
            <span id="${el}-singleUpdateTip-span">${msg("label.singleUpdateTip")}</span>
        </div>

        <div id="${el}-filelist-table" class="fileUpload-filelist-table"></div>

        <!-- @author IXXUS Start -->
        <div class="derivedFilesCaptionCenter">
            <div class="label">Select the file or files from which your uploads are derived.</div>
        </div>
        <div id="${el}-derivedfilelist-table" class="derivedFilelistTable"></div>
        <!-- @author IXXUS End -->

        <div class="status-wrapper">
            <span id="${el}-status-span" class="status"></span>
        </div>

        <div id="${el}-versionSection-div">
            <div class="yui-g">
                <h2>${msg("section.version")}</h2>
            </div>
            <div class="yui-gd">
                <div class="yui-u first">
                    <span>${msg("label.version")}</span>
                </div>
                <div class="yui-u">
                    <input id="${el}-minorVersion-radioButton" type="radio" name="majorVersion" checked="checked"
                           tabindex="0"/>
                    <label for="${el}-minorVersion-radioButton"
                           id="${el}-minorVersion">${msg("label.minorVersion")}</label>
                </div>
            </div>
            <div class="yui-gd">
                <div class="yui-u first">&nbsp;
                </div>
                <div class="yui-u">
                    <input id="${el}-majorVersion-radioButton" type="radio" name="majorVersion" tabindex="0"/>
                    <label for="${el}-majorVersion-radioButton"
                           id="${el}-majorVersion">${msg("label.majorVersion")}</label>
                </div>
            </div>
            <div class="yui-gd">
                <div class="yui-u first">
                    <label for="${el}-description-textarea">${msg("label.comments")}</label>
                </div>
                <div class="yui-u">
                    <textarea id="${el}-description-textarea" name="description" cols="80" rows="4"
                              tabindex="0"></textarea>
                </div>
            </div>
        </div>

        <!-- Templates for a file row -->
        <div style="display:none">
            <div id="${el}-left-div" class="fileupload-left-div">
                <span class="fileupload-percentage-span hidden">&nbsp;</span>
            <#if (contentTypes?size == 1)>
                <input class="fileupload-contentType-input" type="hidden" value="${contentTypes[0].id}"/>
                <#elseif (contentTypes?size > 1)>
                    <select class="fileupload-contentType-select" tabindex="0">
                        <#if (contentTypes?size > 0)>
                            <#list contentTypes as contentType>
                                <option value="${contentType.id}">${msg(contentType.value)}</option>
                            </#list>
                        </#if>
                    </select>
            </#if>
            </div>
            <div id="${el}-center-div" class="fileupload-center-div">
                <span class="fileupload-progressSuccess-span">&nbsp;</span>
                <img src="${url.context}/res/components/images/generic-file-32.png" class="fileupload-docImage-img"
                     alt="file"/>
                <span class="fileupload-progressInfo-span"></span>
            </div>
            <div id="${el}-right-div" class="fileupload-right-div">
            <span class="fileupload-fileButton-span">
               <button class="fileupload-file-button" value="Remove" disabled="true"
                       tabindex="0">${msg("button.remove")}</button>
            </span>
            </div>
        </div>

        <!-- @author IXXUS Start -->
        <!-- Table as a data source to YUI DataTable with derived files
        Not used at the moment as we use a DataSource that is calling the studyfiles web script via AJAX
        <div style="display:none">
            <table id="-derived-data-table">
                <thead>
                    <tr><th>Node Ref</th><th>Name</th><th>Type</th><th>File Id</th></tr>
    	        </thead>
                <tbody>
                #list derivedFileList as derivedFile>
                    <tr><td>derivedFile.nodeRef</td><td>derivedFile.name</td><td>derivedFile.type</td><td>derivedFile.fileId</td></tr>
                /#list
                </tbody>
            </table>
        </div>-->
        <!-- @author IXXUS End -->

        <div class="bdft">
            <input id="${el}-upload-button" type="button" value="${msg("button.upload")}" tabindex="0"/>
            <input id="${el}-cancelOk-button" type="button" value="${msg("button.cancel")}" tabindex="0"/>
        </div>
    </div>
</div>
<script type="text/javascript">//<![CDATA[
new Alfresco.FlashUpload("${el}").setMessages(
${messages}
        );
//]]></script>
