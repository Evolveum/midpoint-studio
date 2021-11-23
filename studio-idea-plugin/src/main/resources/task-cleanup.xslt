<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3"
                xmlns:c="http://midpoint.evolveum.com/xml/ns/public/common/common-3">

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

    <xsl:template match="//c:distribution">
        <xsl:call-template name="removeIfEmpty"/>
    </xsl:template>

    <xsl:template match="//c:provisioningStatistics/c:entry">
        <xsl:call-template name="removeIfEmpty"/>
    </xsl:template>

    <xsl:template match="//c:buckets/c:allocation">
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
