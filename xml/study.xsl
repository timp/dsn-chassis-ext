<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" xmlns:atom="http://www.w3.org/2005/Atom"
    version="1.0">
    <xsl:output method="html" />
   
  <xsl:template match="/">
    <html>
        <head>
            <style type="text/css">
                .xforms-label {
    display: inline-block;
    margin-right: 3px;
    width: 150px;
}
            </style>
        </head>
        <body>
            <xsl:apply-templates/>
        </body>
    </html>	
  </xsl:template>
    <xsl:template match="atom:entry">
        <xsl:apply-templates select="atom:title"/>
        <xsl:apply-templates select="atom:content"/>
    </xsl:template>
    <xsl:template match="study">
        <xsl:apply-templates select="publications"/>
        <xsl:apply-templates select="acknowledgements"/>
    </xsl:template>
    <xsl:template match="modules">
        <h4 class="selected-modules-title">Selected WWARN modules:</h4>
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="//atom:title">
        <h3>(1) Title </h3>
        <span class="study-title">
            <label class="xforms-label">Title:</label><span><xsl:apply-templates/></span>
        </span>
    </xsl:template>
    
    <xsl:template match="publications">
        <h3>(2) Publications</h3>
        <xsl:apply-templates select="//study-is-published"/>
         <hr/>
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="publication">
        
        <label>Title:</label><xsl:value-of select="publication-title"/><br/>
        <label>PubMedID:</label><xsl:value-of select="pmid"/><br/>
        <xsl:apply-templates select="publication-references/publication-reference"/>
        <hr/>
    </xsl:template>
    <xsl:template match="//study-is-published">
        <label>Is the study published?</label>
        <xsl:value-of select="."/>
    </xsl:template>
    <xsl:template match="publication-references/publication-reference">
        <label><xsl:value-of select="@type"/><xsl:text>:</xsl:text></label>
        <xsl:value-of select="."/><br/>
    </xsl:template>
    
    <xsl:template match="acknowledgements">
    <h3>(3) Acknowledgements</h3>
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="institution-ack">
        <xsl:value-of select="institution-name"/><br/>
        <xsl:apply-templates select="institution-websites/institution-url"></xsl:apply-templates>
        <hr/>
    </xsl:template>
    <xsl:template match="person">
        <xsl:value-of select="first-name"/><xsl:text> </xsl:text><xsl:value-of select="middle-name"/><xsl:text> </xsl:text><xsl:value-of select="family-name"/><br/>
        <xsl:value-of select="institution"/><br/>
        <xsl:value-of select="email-address"/><xsl:text> </xsl:text>
        <xsl:value-of select="person-is-contactable"/><br/>
    </xsl:template>
    <xsl:template match="institution-websites/institution-url">
        <xsl:apply-templates/>
    </xsl:template>

    
</xsl:stylesheet>