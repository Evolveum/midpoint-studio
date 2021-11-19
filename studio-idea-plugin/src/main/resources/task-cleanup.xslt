<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3"
                xmlns:mext="http://midpoint.evolveum.com/xml/ns/public/model/extension-3"
                xmlns:scext="http://midpoint.evolveum.com/xml/ns/public/model/scripting/extension-3"
                xmlns:c="http://midpoint.evolveum.com/xml/ns/public/common/common-3"
                exclude-result-prefixes="mext scext">

    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

    <xsl:strip-space elements="*"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/c:task/c:extension">
        <xsl:call-template name="removeIfEmpty"/>
    </xsl:template>

    <xsl:template match="c:distribution">
        <xsl:call-template name="removeIfEmpty"/>
    </xsl:template>

    <xsl:template name="removeIfEmpty">
        <xsl:if test="node()">
            <xsl:copy>
                <xsl:apply-templates select="@*|node()"/>
            </xsl:copy>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
