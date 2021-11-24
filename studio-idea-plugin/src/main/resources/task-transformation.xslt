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

    <xsl:template match="/c:task">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
            <activity>
                <xsl:if test="/c:task/c:handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/reconciliation/handler-3'] or /c:task/c:handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/partitioned-reconciliation/handler-3'] or /c:task/c:assignment/c:targetRef[@oid = '00000000-0000-0000-0000-000000000501']">
                    <xsl:call-template name="reconciliation"/>
                </xsl:if>
                <xsl:if test="/c:task/c:handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/live-sync/handler-3'] or /c:task/c:assignment/c:targetRef[@oid = '00000000-0000-0000-0000-000000000504']">
                    <xsl:call-template name="livesync"/>
                </xsl:if>
                <xsl:if test="/c:task/c:handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/import/handler-3'] or /c:task/c:assignment/c:targetRef[@oid = '00000000-0000-0000-0000-000000000503'] or (/c:task/c:handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/task/workers-creation/handler-3'] and /c:task/c:workManagement/c:workers/c:handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/import/handler-3'])">
                    <xsl:call-template name="import"/>
                </xsl:if>
                <xsl:if test="/c:task/c:handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/recompute/handler-3'] or /c:task/c:assignment/c:targetRef[@oid = '00000000-0000-0000-0000-000000000502'] or (/c:task/c:handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/task/workers-creation/handler-3'] and /c:task/c:workManagement/c:workers/c:handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/recompute/handler-3'])">
                    <xsl:call-template name="recomputation"/>
                </xsl:if>
                <xsl:if test="/c:task/c:handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/model/iterative-scripting/handler-3'] or /c:task/c:assignment/c:targetRef[@oid = '00000000-0000-0000-0000-000000000509'] or (/c:task/c:handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/task/workers-creation/handler-3'] and /c:task/c:workManagement/c:workers/c:handlerUri[text() = 'http://midpoint.evolveum.com/xml/ns/public/model/iterative-scripting/handler-3'])">
                    <xsl:call-template name="iterativeScripting"/>
                </xsl:if>
            </activity>
        </xsl:copy>
    </xsl:template>

    <!-- skip these elements -->
    <xsl:template match="/c:task/c:category"/>
    <xsl:template match="/c:task/c:handlerUri"/>
    <xsl:template match="/c:task/c:workManagement"/>
    <xsl:template match="/c:task/c:structuredProgress"/>
    <xsl:template match="/c:task/c:workState"/>
    <xsl:template match="/c:task/c:otherHandlersUriStack"/>

    <xsl:template match="/c:task/c:operationExecution/c:taskPartUri"/>
    <xsl:template match="/c:task/c:operationStats/c:iterationInformation"/>
    <xsl:template match="/c:task/c:operationStats/c:iterativeTaskInformation"/>
    <xsl:template match="/c:task/c:operationStats/c:synchronizationInformation"/>
    <xsl:template match="/c:task/c:operationStats/c:actionsExecutedInformation"/>
    <xsl:template match="/c:task/c:operationStats/c:workBucketManagementPerformanceInformation"/>
    <xsl:template match="/c:task/c:activityState/c:bucket"/>
    <xsl:template match="/c:task/c:activityState/c:numberOfBuckets"/>

    <xsl:template match="//c:buckets/c:allocation/c:allocateFirst"/>
    <xsl:template match="//c:buckets/c:allocation/c:workAllocationMaxRetries"/>
    <xsl:template match="//c:buckets/c:allocation/c:workAllocationRetryIntervalBase"/>
    <xsl:template match="//c:buckets/c:allocation/c:workAllocationRetryExponentialThreshold"/>
    <xsl:template match="//c:buckets/c:allocation/c:workAllocationRetryIntervalLimit"/>

    <xsl:template match="//c:provisioningStatistics/c:entry/c:resource"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:getSuccess"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:getFailure"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:searchSuccess"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:searchFailure"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:createSuccess"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:createFailure"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:updateSuccess"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:updateFailure"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:deleteSuccess"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:deleteFailure"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:syncSuccess"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:syncFailure"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:scriptSuccess"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:scriptFailure"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:otherSuccess"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:otherFailure"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:averageTime"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:minTime"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:maxTime"/>
    <xsl:template match="//c:provisioningStatistics/c:entry/c:totalTime"/>
    <xsl:template match="//c:workers/c:handlerUri"/>

    <xsl:template match="/c:task/c:recurrence"/>
    <xsl:template match="/c:task/c:errorHandlingStrategy"/>
    <xsl:template match="/c:task/c:extension/mext:kind"/>
    <xsl:template match="/c:task/c:extension/mext:intent"/>
    <xsl:template match="/c:task/c:extension/mext:objectclass"/>
    <xsl:template match="/c:task/c:extension/mext:workerThreads"/>
    <xsl:template match="/c:task/c:extension/mext:liveSyncBatchSize"/>
    <xsl:template match="/c:task/c:extension/mext:updateLiveSyncTokenInDryRun"/>
    <xsl:template match="/c:task/c:extension/mext:objectType"/>
    <xsl:template match="/c:task/c:extension/mext:objectQuery"/>
    <xsl:template match="/c:task/c:extension/scext:executeScript"/>

    <xsl:template match="/c:task/c:executionStatus">
        <executionState><xsl:value-of select="node()"/></executionState>
    </xsl:template>

    <xsl:template match="/c:task/c:schedule">
        <schedule>
            <xsl:if test="/c:task/c:recurrence">
                <recurrence><xsl:value-of select="/c:task/c:recurrence"/></recurrence>
            </xsl:if>
            <xsl:copy-of select="*|comment()"/>
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

    <xsl:template name="distributionComplexActivity">
        <distribution>
            <xsl:call-template name="distributionBasic"/>
        </distribution>
    </xsl:template>

    <xsl:template name="distributionSimpleActivity">
        <distribution>
            <xsl:if test="/c:task/c:workManagement">
                <xsl:apply-templates select="/c:task/c:workManagement/c:buckets" />
                <xsl:apply-templates select="/c:task/c:workManagement/c:workers"/>
            </xsl:if>

            <xsl:call-template name="distributionBasic"/>
        </distribution>
    </xsl:template>

    <xsl:template name="controlFlow">
        <xsl:if test="/c:task/c:errorHandlingStrategy">
            <controlFlow>
                <errorHandling>
                    <xsl:copy-of select="/c:task/c:errorHandlingStrategy/*"/>
                </errorHandling>
            </controlFlow>
        </xsl:if>
    </xsl:template>

    <xsl:template name="distributionBasic">
        <xsl:if test="/c:task/c:extension/mext:workerThreads">
            <workerThreads><xsl:value-of select="/c:task/c:extension/mext:workerThreads"/></workerThreads>
        </xsl:if>
        <xsl:if test="/c:task/c:workManagement/c:workers or /c:task/c:workManagement/c:partitions">
            <subtasks/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="reconciliation">
        <work>
            <reconciliation>
                <xsl:call-template name="resourceObjects"/>
            </reconciliation>
        </work>
        <xsl:call-template name="controlFlow"/>
        <xsl:call-template name="distributionComplexActivity"/>
        <xsl:if test="/c:task/c:workManagement/c:partitions/c:partition">
            <tailoring>
                <xsl:if test="/c:task/c:workManagement/c:partitions/c:partition/c:index[text() = 2]">
                    <change>
                        <reference>resourceObjects</reference>
                        <distribution>
                            <xsl:apply-templates select="/c:task/c:workManagement/c:partitions/c:partition/c:index[text() = 2]/parent::node()/c:workManagement/c:buckets"/>
                            <xsl:apply-templates select="/c:task/c:workManagement/c:partitions/c:partition/c:index[text() = 2]/parent::node()/c:workManagement/c:workers"/>
                        </distribution>
                    </change>
                </xsl:if>
                <xsl:if test="/c:task/c:workManagement/c:partitions/c:partition/c:index[text() = 3]">
                    <change>
                        <reference>remainingShadows</reference>
                        <distribution>
                            <xsl:apply-templates select="/c:task/c:workManagement/c:partitions/c:partition/c:index[text() = 3]/parent::node()/c:workManagement/c:buckets"/>
                            <xsl:apply-templates select="/c:task/c:workManagement/c:partitions/c:partition/c:index[text() = 3]/parent::node()/c:workManagement/c:workers"/>
                        </distribution>
                    </change>
                </xsl:if>
            </tailoring>
        </xsl:if>
    </xsl:template>

    <xsl:template name="livesync">
        <work>
            <liveSynchronization>
                <xsl:call-template name="resourceObjects"/>
                <xsl:if test="/c:task/c:extension/mext:liveSyncBatchSize">
                    <batchSize><xsl:value-of select="/c:task/c:extension/mext:liveSyncBatchSize"/></batchSize>
                    <updateLiveSyncTokenInDryRun><xsl:value-of select="/c:task/c:extension/mext:updateLiveSyncTokenInDryRun"/></updateLiveSyncTokenInDryRun>
                </xsl:if>
            </liveSynchronization>
        </work>
        <xsl:call-template name="controlFlow"/>
        <xsl:call-template name="distributionSimpleActivity"/>
    </xsl:template>

    <xsl:template name="import">
        <work>
            <import>
                <xsl:call-template name="resourceObjects"/>
            </import>
        </work>
        <xsl:call-template name="controlFlow"/>
        <xsl:call-template name="distributionSimpleActivity"/>
    </xsl:template>

    <xsl:template name="objectDefinition">
        <objects>
            <type><xsl:value-of select="/c:task/c:extension/mext:objectType"/></type>
            <query><xsl:copy-of select="/c:task/c:extension/mext:objectQuery/*"/></query>
            <!-- todo searchOptions -->
        </objects>
    </xsl:template>

    <xsl:template name="recomputation">
        <work>
            <recomputation>
                <xsl:call-template name="objectDefinition"/>
                <!-- todo executionOptions -->
            </recomputation>
        </work>
        <xsl:call-template name="controlFlow"/>
        <xsl:call-template name="distributionSimpleActivity"/>
    </xsl:template>

    <xsl:template name="iterativeScripting">
        <work>
            <iterativeScripting>
                <xsl:call-template name="objectDefinition"/>
                <scriptExecutionRequest>
                    <xsl:copy-of select="/c:task/c:extension/scext:executeScript/*"/>
                </scriptExecutionRequest>
            </iterativeScripting>
        </work>
        <xsl:call-template name="controlFlow"/>
        <xsl:call-template name="distributionSimpleActivity"/>
    </xsl:template>

</xsl:stylesheet>
