<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3"
                xmlns:mext="http://midpoint.evolveum.com/xml/ns/public/model/extension-3"
                xmlns:c="http://midpoint.evolveum.com/xml/ns/public/common/common-3"
                exclude-result-prefixes="mext">

    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

    <xsl:strip-space elements="*"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/c:task">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
            <activity>
                <xsl:if test="/c:task/c:handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/partitioned-reconciliation/handler-3'] or /c:task/c:assignment/c:targetRef[@oid = '00000000-0000-0000-0000-000000000501']">
                    <xsl:call-template name="reconciliation"/>
                </xsl:if>
                <xsl:if test="/c:task/c:handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/live-sync/handler-3'] or /c:task/c:assignment/c:targetRef[@oid = '00000000-0000-0000-0000-000000000504']">
                    <xsl:call-template name="livesync"/>
                </xsl:if>
                <xsl:if test="/c:task/c:handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/import/handler-3'] or /c:task/c:assignment/c:targetRef[@oid = '00000000-0000-0000-0000-000000000503']">
                    <xsl:call-template name="import"/>
                </xsl:if>
                <xsl:if test="/c:task/c:errorHandlingStrategy">
                    <controlFlow>
                        <errorHandling>
                            <xsl:copy-of select="/c:task/c:errorHandlingStrategy/*"/>
                        </errorHandling>
                    </controlFlow>
                </xsl:if>
            </activity>
        </xsl:copy>
    </xsl:template>

    <!-- skip these elements -->
    <xsl:template match="/c:task/c:channel"/>
    <xsl:template match="/c:task/c:handlerUri"/>
    <xsl:template match="/c:task/c:workManagement"/>
    <xsl:template match="/c:task/c:objectRef"/>
    <xsl:template match="/c:task/c:recurrence"/>
    <xsl:template match="/c:task/c:errorHandlingStrategy"/>
    <xsl:template match="/c:task/c:workManagement/c:workers/c:handlerUri"/>
    <xsl:template match="/c:task/c:extension/mext:kind"/>
    <xsl:template match="/c:task/c:extension/mext:intent"/>
    <xsl:template match="/c:task/c:extension/mext:objectclass"/>
    <xsl:template match="/c:task/c:extension/mext:workerThreads"/>
    <xsl:template match="/c:task/c:extension/mext:liveSyncBatchSize"/>

    <!-- todo how to handle extension when we removed all mext items and extension ended up being empty? -->

    <xsl:template match="/c:task/c:executionStatus">
        <executionState><xsl:value-of select="node()"/></executionState>
    </xsl:template>

    <xsl:template match="/c:task/c:schedule">
        <schedule>
            <xsl:if test="/c:task/c:recurrence">
                <recurrence><xsl:value-of select="/c:task/c:recurrence"/></recurrence>
            </xsl:if>
            <xsl:copy-of select="*"/>
        </schedule>
    </xsl:template>

    <xsl:template name="resourceObjects">
        <resourceObjects>
            <xsl:element name="resourceRef">
                <xsl:attribute name="oid">
                    <xsl:value-of  select="/c:task/c:objectRef/@oid"/>
                </xsl:attribute>
            </xsl:element>
            <kind><xsl:value-of select="/c:task/c:extension/mext:kind"/></kind>
            <intent><xsl:value-of select="/c:task/c:extension/mext:intent"/></intent>
            <objectclass><xsl:value-of select="/c:task/c:extension/mext:objectclass"/></objectclass>
        </resourceObjects>
    </xsl:template>

    <xsl:template name="distribution">
        <distribution>
            <xsl:if test="/c:task/c:extension/mext:workerThreads">
                <workerThreads><xsl:value-of select="/c:task/c:extension/mext:workerThreads"/></workerThreads>
            </xsl:if>
            <xsl:if test="/c:task/c:handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/partitioned-reconciliation/handler-3'] or /c:task/c:handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/task/workers-creation/handler-3']">
                <subtasks/>
            </xsl:if>
        </distribution>
    </xsl:template>

    <xsl:template name="reconciliation">
        <work>
            <reconciliation>
                <xsl:call-template name="resourceObjects"/>
            </reconciliation>
        </work>
        <xsl:call-template name="distribution"/>
        <tailoring>
            <change>
                <reference>resourceObjects</reference>
                <distribution>
                    <xsl:copy-of select="/c:task/c:workManagement/c:partitions/c:partition/c:index[text() = 2]/parent::node()/c:workManagement/c:buckets"/>
                    <xsl:copy-of select="/c:task/c:workManagement/c:partitions/c:partition/c:index[text() = 2]/parent::node()/c:workManagement/c:workers"/>
                </distribution>
            </change>
            <change>
                <reference>remainingShadows</reference>
                <distribution>
                    <xsl:copy-of select="/c:task/c:workManagement/c:partitions/c:partition/c:index[text() = 3]/parent::node()/c:workManagement/c:buckets"/>
                    <xsl:copy-of select="/c:task/c:workManagement/c:partitions/c:partition/c:index[text() = 3]/parent::node()/c:workManagement/c:workers"/>
                </distribution>
            </change>
        </tailoring>
    </xsl:template>

    <xsl:template name="livesync">
        <work>
            <liveSynchronization>
                <xsl:call-template name="resourceObjects"/>
                <xsl:if test="/c:task/c:extension/mext:liveSyncBatchSize">
                    <batchSize><xsl:value-of select="/c:task/c:extension/mext:liveSyncBatchSize"/></batchSize>
                </xsl:if>
            </liveSynchronization>
        </work>
    </xsl:template>

    <xsl:template name="import">
        <work>
            <import>
                <xsl:call-template name="resourceObjects"/>
            </import>
        </work>
        <xsl:call-template name="distribution"/>
        <tailoring>
            <change>
                <reference>resourceObjects</reference>
                <distribution>
                    <xsl:copy-of select="/c:task/c:workManagement/c:buckets"/>
                    <xsl:apply-templates select="/c:task/c:workManagement/c:workers"/>
                </distribution>
            </change>
        </tailoring>
    </xsl:template>

</xsl:stylesheet>
