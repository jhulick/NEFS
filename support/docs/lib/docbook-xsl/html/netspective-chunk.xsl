<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version='1.0'
                xmlns="http://www.w3.org/TR/xhtml1/transitional"
                exclude-result-prefixes="#default">

<xsl:import href="../../redist/docbook-xsl-1.62.4/html/chunk.xsl"/>

<xsl:variable name="chunk.quietly">1</xsl:variable>

<xsl:variable name="toc.section.depth">3</xsl:variable>
<xsl:variable name="admon.graphics">1</xsl:variable>
<xsl:variable name="navig.graphics">0</xsl:variable>
<xsl:variable name="chunk.first.sections">1</xsl:variable>

<xsl:variable name="html.stylesheet">resources/netspective-docbook.css</xsl:variable>

<xsl:variable name="menuchoice.menu.separator"> -&gt; </xsl:variable>
<xsl:variable name="menuchoice.separator"> + </xsl:variable>

</xsl:stylesheet>