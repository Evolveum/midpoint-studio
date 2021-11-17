<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3"
                xmlns:mext="http://midpoint.evolveum.com/xml/ns/public/model/extension-3"
                xmlns:c="http://midpoint.evolveum.com/xml/ns/public/common/common-3">

    <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"/>

    <xsl:strip-space elements="*"></xsl:strip-space>

    <xsl:template match="/"
                  xpath-default-namespace="http://midpoint.evolveum.com/xml/ns/public/common/common-3">

        <task xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3"
              xmlns:c="http://midpoint.evolveum.com/xml/ns/public/common/common-3"
              xmlns:mext="http://midpoint.evolveum.com/xml/ns/public/model/extension-3"
              xmlns:q="http://prism.evolveum.com/xml/ns/public/query-3"
              xmlns:xsd="http://www.w3.org/2001/XMLSchema"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xmlns:apti="http://midpoint.evolveum.com/xml/ns/public/common/api-types-3"
              xmlns:icfs="http://midpoint.evolveum.com/xml/ns/public/connector/icf-1/resource-schema-3"
              xmlns:org="http://midpoint.evolveum.com/xml/ns/public/common/org-3"
              xmlns:ri="http://midpoint.evolveum.com/xml/ns/public/resource/instance-3"
              xmlns:t="http://prism.evolveum.com/xml/ns/public/types-3"
              xmlns:s="http://midpoint.evolveum.com/xml/ns/public/model/scripting-3">

            <xsl:call-template name="basic"/>

            <activity>
                <xsl:if test="/task/handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/partitioned-reconciliation/handler-3'] or /task/assignment/targetRef[@oid = '00000000-0000-0000-0000-000000000501']">
                    <xsl:call-template name="reconciliation"/>
                </xsl:if>
                <xsl:if test="/task/handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/live-sync/handler-3'] or /task/assignment/targetRef[@oid = '00000000-0000-0000-0000-000000000504']">
                    <xsl:call-template name="livesync"/>
                </xsl:if>
                <xsl:if test="/task/handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/import/handler-3'] or /task/assignment/targetRef[@oid = '00000000-0000-0000-0000-000000000503']">
                    <xsl:call-template name="import"/>
                </xsl:if>
                <xsl:if test="/task/errorHandlingStrategy">
                    <controlFlow>
                        <errorHandling>
                            <xsl:copy-of select="/task/errorHandlingStrategy/*"/>
                        </errorHandling>
                    </controlFlow>
                </xsl:if>
            </activity>
        </task>
    </xsl:template>

    <xsl:template name="basic">

        <xsl:if test="/task/@oid">
            <xsl:attribute name="oid">
                <xsl:value-of  select="/task/@oid"/>
            </xsl:attribute>
        </xsl:if>

        <xsl:if test="/task/@version">
            <xsl:attribute name="version">
                <xsl:value-of  select="/task/@version"/>
            </xsl:attribute>
        </xsl:if>

        <name>
            <xsl:value-of select="/task/name"/>
        </name>

        <xsl:if test="/task/handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/live-sync/handler-3'] or /task/assignment/targetRef[@oid = '00000000-0000-0000-0000-000000000504']">
            <xsl:call-template name="livesyncExtension"/>
        </xsl:if>

        <assignment>
            <xsl:copy-of select="/task/assignment/targetRef"/>
        </assignment>

        <xsl:copy-of select="/task/ownerRef"/>

        <xsl:element name="executionState">
            <xsl:value-of select="/task/executionStatus"/>
        </xsl:element>

        <xsl:copy-of select="/task/binding"/>

        <xsl:copy-of select="/task/executionEnvironment"/>
        <xsl:copy-of select="/task/schedule"/>
        <xsl:copy-of select="/task/threadStopAction"/>
    </xsl:template>

    <xsl:template name="resourceObjects">
        <resourceObjects>
            <xsl:element name="resourceRef">
                <xsl:attribute name="oid">
                    <xsl:value-of  select="/task/objectRef/@oid"/>
                </xsl:attribute>
            </xsl:element>
            <kind><xsl:value-of select="/task/extension/kind"/></kind>
            <intent><xsl:value-of select="/task/extension/intent"/></intent>
            <objectclass><xsl:value-of select="/task/extension/objectclass"/></objectclass>
        </resourceObjects>
    </xsl:template>

    <xsl:template name="distribution">
        <distribution>
            <xsl:if test="/task/extension/workerThreads">
                <workerThreads><xsl:value-of select="/task/extension/workerThreads"/></workerThreads>
            </xsl:if>
            <xsl:if test="/task/handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/partitioned-reconciliation/handler-3'] or /task/handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/task/workers-creation/handler-3']">
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
                    <xsl:copy-of select="/task/workManagement/partitions/partition/index[text() = 2]/parent::node()/workManagement/buckets"/>
                    <xsl:copy-of select="/task/workManagement/partitions/partition/index[text() = 2]/parent::node()/workManagement/workers"/>
                </distribution>
            </change>
            <change>
                <reference>remainingShadows</reference>
                <distribution>
                    <xsl:copy-of select="/task/workManagement/partitions/partition/index[text() = 3]/parent::node()/workManagement/buckets"/>
                    <xsl:copy-of select="/task/workManagement/partitions/partition/index[text() = 3]/parent::node()/workManagement/workers"/>
                </distribution>
            </change>
        </tailoring>
    </xsl:template>

    <xsl:template name="livesyncExtension">
        <xsl:if test="/task/extension/token">
            <extension>
                <xsl:copy-of select="/task/extension/token"/>
            </extension>
        </xsl:if>
    </xsl:template>

    <xsl:template name="livesync">
        <work>
            <liveSynchronization>
                <xsl:call-template name="resourceObjects"/>
                <xsl:if test="/task/extension/liveSyncBatchSize">
                    <batchSize><xsl:value-of select="/task/extension/liveSyncBatchSize"/></batchSize>
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
                    <xsl:copy-of select="/task/workManagement/buckets"/>
                    <xsl:apply-templates select="/task/workManagement/workers"/>
                </distribution>
            </change>
        </tailoring>
    </xsl:template>

    <xsl:template match="workers/handlerUri" />

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
