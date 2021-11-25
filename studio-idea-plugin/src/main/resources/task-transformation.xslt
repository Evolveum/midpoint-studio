<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://midpoint.evolveum.com/xml/ns/public/common/common-3"
                xmlns:mext="http://midpoint.evolveum.com/xml/ns/public/model/extension-3"
                xmlns:scext="http://midpoint.evolveum.com/xml/ns/public/model/scripting/extension-3"
                xmlns:c="http://midpoint.evolveum.com/xml/ns/public/common/common-3"
                exclude-result-prefixes="mext scext c">

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
                <xsl:variable name="URI_WORKERS_CREATION" select="'http://midpoint.evolveum.com/xml/ns/public/task/workers-creation/handler-3'"/>
                
                <xsl:variable name="URI_ASYNC_UPDATE" select="'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/async-update/handler-3'"/>
                <xsl:variable name="URI_DELETE" select="'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/delete/handler-3'"/>
                <xsl:variable name="URI_IMPORT" select="'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/import/handler-3'"/>
                <xsl:variable name="URI_ITERATIVE_SCRIPTING" select="'http://midpoint.evolveum.com/xml/ns/public/model/iterative-scripting/handler-3'"/>
                <xsl:variable name="URI_LIVE_SYNC" select="'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/live-sync/handler-3'"/>
                <xsl:variable name="URI_PARTITIONED_RECONCILIATION" select="'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/partitioned-reconciliation/handler-3'"/>
                <xsl:variable name="URI_RECOMPUTE" select="'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/recompute/handler-3'"/>
                <xsl:variable name="URI_RECONCILIATION" select="'http://midpoint.evolveum.com/xml/ns/public/model/synchronization/task/reconciliation/handler-3'"/>
                <xsl:variable name="URI_REINDEX" select="'http://midpoint.evolveum.com/xml/ns/public/model/reindex/handler-3'"/>
                <xsl:variable name="URI_SCRIPTING" select="'http://midpoint.evolveum.com/xml/ns/public/model/scripting/handler-3'"/>
                <xsl:variable name="URI_SHADOW_INTEGRITY" select="'http://midpoint.evolveum.com/xml/ns/public/model/shadow-integrity-check/handler-3'"/>
                <xsl:variable name="URI_SHADOW_REFRESH" select="'http://midpoint.evolveum.com/xml/ns/public/model/shadowRefresh/handler-3'"/>

                <xsl:variable name="taskHandlerUri" select="/c:task/c:handlerUri/text()"/>
                <xsl:variable name="assignmentTargetOid" select="/c:task/c:assignment/c:targetRef/@oid"/>
                <xsl:variable name="workersHandlerUri" select="/c:task/c:workManagement/c:workers/c:handlerUri/text()"/>

                <xsl:choose>
                    <xsl:when test="$taskHandlerUri = $URI_RECONCILIATION or $taskHandlerUri = $URI_PARTITIONED_RECONCILIATION or $assignmentTargetOid = '00000000-0000-0000-0000-000000000501'">
                        <xsl:call-template name="reconciliation"/>
                    </xsl:when>
                    <xsl:when test="$taskHandlerUri = $URI_LIVE_SYNC or $assignmentTargetOid = '00000000-0000-0000-0000-000000000504'">
                        <xsl:call-template name="livesync"/>
                    </xsl:when>
                    <xsl:when test="$taskHandlerUri = $URI_IMPORT or $assignmentTargetOid = '00000000-0000-0000-0000-000000000503' or ($taskHandlerUri = $URI_WORKERS_CREATION and $workersHandlerUri = $URI_IMPORT)">
                        <xsl:call-template name="import"/>
                    </xsl:when>
                    <xsl:when test="$taskHandlerUri = $URI_RECOMPUTE or $assignmentTargetOid = '00000000-0000-0000-0000-000000000502' or ($taskHandlerUri = $URI_WORKERS_CREATION and $workersHandlerUri = $URI_RECOMPUTE)">
                        <xsl:call-template name="recomputation"/>
                    </xsl:when>
                    <xsl:when test="$taskHandlerUri = $URI_ITERATIVE_SCRIPTING or $assignmentTargetOid = '00000000-0000-0000-0000-000000000509' or ($taskHandlerUri = $URI_WORKERS_CREATION and $workersHandlerUri = $URI_ITERATIVE_SCRIPTING)">
                        <xsl:call-template name="iterativeScripting"/>
                    </xsl:when>
                    <xsl:when test="$taskHandlerUri = $URI_ASYNC_UPDATE or $assignmentTargetOid = '00000000-0000-0000-0000-000000000505' or ($taskHandlerUri = $URI_WORKERS_CREATION and $workersHandlerUri = $URI_ASYNC_UPDATE)">
                        <xsl:call-template name="asyncUpdate"/>
                    </xsl:when>
                    <xsl:when test="$taskHandlerUri = $URI_DELETE or $assignmentTargetOid = '00000000-0000-0000-0000-000000000528' or ($taskHandlerUri = $URI_WORKERS_CREATION and $workersHandlerUri = $URI_DELETE)">
                        <xsl:call-template name="deletion"/>
                    </xsl:when>
                    <xsl:when test="$taskHandlerUri = $URI_SHADOW_INTEGRITY or ($taskHandlerUri = $URI_WORKERS_CREATION and $workersHandlerUri = $URI_SHADOW_INTEGRITY)">
                        <xsl:call-template name="shadowIntegrityCheck"/>
                    </xsl:when>
                    <xsl:when test="$taskHandlerUri = $URI_SHADOW_REFRESH or ($taskHandlerUri = $URI_WORKERS_CREATION and $workersHandlerUri = $URI_SHADOW_REFRESH)">
                        <xsl:call-template name="shadowRefresh"/>
                    </xsl:when>
                    <xsl:when test="$taskHandlerUri = $URI_REINDEX or ($taskHandlerUri = $URI_WORKERS_CREATION and $workersHandlerUri = $URI_REINDEX)">
                        <xsl:call-template name="reindexing"/>
                    </xsl:when>
                    <xsl:when test="$taskHandlerUri = $URI_SCRIPTING or $assignmentTargetOid = '00000000-0000-0000-0000-000000000508' or ($taskHandlerUri = $URI_WORKERS_CREATION and $workersHandlerUri = $URI_SCRIPTING)">
                        <xsl:call-template name="nonIterativeScripting"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:message terminate="yes">Unknown action</xsl:message>
                    </xsl:otherwise>
                </xsl:choose>

                <xsl:if test="/c:task/c:extension/mext:dryRun[text() = 'true']">
                    <executionMode>dryRun</executionMode>
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

    <xsl:template match="/c:task/c:errorHandlingStrategy"/>
    <xsl:template match="/c:task/c:extension/mext:kind"/>
    <xsl:template match="/c:task/c:extension/mext:intent"/>
    <xsl:template match="/c:task/c:extension/mext:objectclass"/>
    <xsl:template match="/c:task/c:extension/mext:workerThreads"/>
    <xsl:template match="/c:task/c:extension/mext:liveSyncBatchSize"/>
    <xsl:template match="/c:task/c:extension/mext:updateLiveSyncTokenInDryRun"/>
    <xsl:template match="/c:task/c:extension/mext:objectType"/>
    <xsl:template match="/c:task/c:extension/mext:objectQuery"/>
    <xsl:template match="/c:task/c:extension/mext:searchOptions"/>
    <xsl:template match="/c:task/c:extension/mext:modelExecuteOptions"/>
    <xsl:template match="/c:task/c:extension/mext:dryRun"/>
    <xsl:template match="/c:task/c:extension/mext:diagnose"/>
    <xsl:template match="/c:task/c:extension/mext:fix"/>
    <xsl:template match="/c:task/c:extension/mext:checkDuplicatesOnPrimaryIdentifiersOnly"/>
    <xsl:template match="/c:task/c:extension/mext:duplicateShadowsResolver"/>
    <xsl:template match="/c:task/c:extension/scext:executeScript"/>

    <!-- todo optionRaw tracing* -->

    <xsl:template match="/c:task/c:executionStatus">
        <executionState><xsl:value-of select="node()"/></executionState>
    </xsl:template>

    <xsl:template match="/c:task/c:recurrence">
        <xsl:if test="not(/c:task/c:schedule)">
            <schedule>
                <recurrence><xsl:value-of select="/c:task/c:recurrence"/></recurrence>
            </schedule>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/c:task/c:schedule">
        <schedule>
            <xsl:if test="/c:task/c:recurrence">
                <recurrence><xsl:value-of select="/c:task/c:recurrence"/></recurrence>
            </xsl:if>
            <xsl:copy-of select="*|comment()"/>
        </schedule>
    </xsl:template>

    <xsl:template name="objectSet">
        <xsl:param name="customElementName" select="'objects'" required="no"/>

        <xsl:if test="/c:task/c:extension/mext:objectType or /c:task/c:extension/mext:objectQuery or /c:task/c:extension/mext:searchOptions">
            <xsl:element name="{$customElementName}">
                <xsl:if test="/c:task/c:extension/mext:objectType">
                    <type><xsl:value-of select="/c:task/c:extension/mext:objectType"/></type>
                </xsl:if>
                <xsl:if test="/c:task/c:extension/mext:objectQuery">
                    <query><xsl:copy-of select="/c:task/c:extension/mext:objectQuery/node()"/></query>
                </xsl:if>
                <xsl:if test="/c:task/c:extension/mext:searchOptions">
                    <searchOptions><xsl:copy-of select="/c:task/c:extension/mext:searchOptions/node()"/></searchOptions>
                </xsl:if>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template name="resourceObjectSet">
        <xsl:param name="customElementName" select="'resourceObjects'" required="no"/>

        <xsl:if test="/c:task/c:objectRef/@oid or /c:task/c:extension/mext:kind or /c:task/c:extension/mext:intent or /c:task/c:extension/mext:objectclass or /c:task/c:extension/mext:searchOptions">
            <xsl:element name="{$customElementName}">
                <xsl:if test="/c:task/c:objectRef/@oid">
                    <xsl:element name="resourceRef">
                        <xsl:attribute name="oid">
                            <xsl:value-of select="/c:task/c:objectRef/@oid"/>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:if>
                <xsl:if test="/c:task/c:extension/mext:kind">
                    <kind><xsl:value-of select="/c:task/c:extension/mext:kind"/></kind>
                </xsl:if>
                <xsl:if test="/c:task/c:extension/mext:intent">
                    <intent><xsl:value-of select="/c:task/c:extension/mext:intent"/></intent>
                </xsl:if>
                <xsl:if test="/c:task/c:extension/mext:objectclass">
                    <objectclass><xsl:value-of select="/c:task/c:extension/mext:objectclass"/></objectclass>
                </xsl:if>
                <xsl:if test="/c:task/c:extension/mext:searchOptions">
                    <searchOptions><xsl:copy-of select="/c:task/c:extension/mext:searchOptions/node()"/></searchOptions>
                </xsl:if>
            </xsl:element>
        </xsl:if>
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
                <xsl:call-template name="resourceObjectSet"/>
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
                <xsl:call-template name="resourceObjectSet"/>
                <xsl:if test="/c:task/c:extension/mext:liveSyncBatchSize">
                    <batchSize><xsl:value-of select="/c:task/c:extension/mext:liveSyncBatchSize"/></batchSize>
                </xsl:if>
                <xsl:if test="/c:task/c:extension/mext:updateLiveSyncTokenInDryRun">
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
                <xsl:call-template name="resourceObjectSet"/>
            </import>
        </work>
        <xsl:call-template name="controlFlow"/>
        <xsl:call-template name="distributionSimpleActivity"/>
    </xsl:template>

    <xsl:template name="recomputation">
        <work>
            <recomputation>
                <xsl:call-template name="objectSet"/>
                <xsl:if test="/c:task/c:extension/mext:modelExecuteOptions">
                    <executionOptions><xsl:copy-of select="/c:task/c:extension/mext:modelExecuteOptions/node()"/></executionOptions>
                </xsl:if>
            </recomputation>
        </work>
        <xsl:call-template name="controlFlow"/>
        <xsl:call-template name="distributionSimpleActivity"/>
    </xsl:template>

    <xsl:template name="iterativeScripting">
        <work>
            <iterativeScripting>
                <xsl:call-template name="objectSet"/>
                <scriptExecutionRequest>
                    <xsl:copy-of select="/c:task/c:extension/scext:executeScript/*"/>
                </scriptExecutionRequest>
            </iterativeScripting>
        </work>
        <xsl:call-template name="controlFlow"/>
        <xsl:call-template name="distributionSimpleActivity"/>
    </xsl:template>

    <xsl:template name="asyncUpdate">
        <work>
            <asynchronousUpdate>
                    <xsl:call-template name="resourceObjectSet">
                        <xsl:with-param name="customElementName" select="'updatedResourceObjects'"/>
                    </xsl:call-template>
            </asynchronousUpdate>
        </work>
        <xsl:call-template name="controlFlow"/>
        <xsl:call-template name="distributionSimpleActivity"/>
    </xsl:template>

    <xsl:template name="deletion">
        <work>
            <deletion>
                <xsl:call-template name="objectSet"/>
                <xsl:if test="/c:task/c:extension/mext:modelExecuteOptions">
                    <executionOptions><xsl:copy-of select="/c:task/c:extension/mext:modelExecuteOptions/node()"/></executionOptions>
                </xsl:if>
            </deletion>
        </work>
        <xsl:call-template name="controlFlow"/>
        <xsl:call-template name="distributionSimpleActivity"/>
    </xsl:template>

    <xsl:template name="shadowIntegrityCheck">
        <work>
            <shadowIntegrityCheck>
                <xsl:call-template name="objectSet">
                    <xsl:with-param name="customElementName" select="'shadows'"/>
                </xsl:call-template>
                <xsl:if test="/c:task/c:extension/mext:modelExecuteOptions">
                    <executionOptions><xsl:copy-of select="/c:task/c:extension/mext:modelExecuteOptions/node()"/></executionOptions>
                </xsl:if>
                <xsl:if test="/c:task/c:extension/mext:diagnose">
                    <diagnose><xsl:value-of select="/c:task/c:extension/mext:diagnose/text()"/></diagnose>
                </xsl:if>
                <xsl:if test="/c:task/c:extension/mext:fix">
                    <fix><xsl:value-of select="/c:task/c:extension/mext:fix/text()"/></fix>
                </xsl:if>
                <xsl:if test="/c:task/c:extension/mext:checkDuplicatesOnPrimaryIdentifiersOnly">
                    <checkDuplicatesOnPrimaryIdentifiersOnly><xsl:value-of select="/c:task/c:extension/mext:checkDuplicatesOnPrimaryIdentifiersOnly/text()"/></checkDuplicatesOnPrimaryIdentifiersOnly>
                </xsl:if>
                <xsl:if test="/c:task/c:extension/mext:duplicateShadowsResolver">
                    <duplicateShadowsResolver><xsl:value-of select="/c:task/c:extension/mext:duplicateShadowsResolver/text()"/></duplicateShadowsResolver>
                </xsl:if>
            </shadowIntegrityCheck>
        </work>
        <xsl:call-template name="controlFlow"/>
        <xsl:call-template name="distributionSimpleActivity"/>
    </xsl:template>

    <xsl:template name="shadowRefresh">
        <work>
            <shadowRefresh>
                <xsl:call-template name="objectSet">
                    <xsl:with-param name="customElementName" select="'shadows'"/>
                </xsl:call-template>
            </shadowRefresh>
        </work>
        <xsl:call-template name="controlFlow"/>
        <xsl:call-template name="distributionSimpleActivity"/>
    </xsl:template>

    <xsl:template name="reindexing">
        <work>
            <reindexing>
                <xsl:call-template name="objectSet"/>
            </reindexing>
        </work>
        <xsl:call-template name="controlFlow"/>
        <xsl:call-template name="distributionSimpleActivity"/>
    </xsl:template>

    <xsl:template name="nonIterativeScripting">
        <work>
            <nonIterativeScripting>
                <scriptExecutionRequest>
                    <xsl:copy-of select="/c:task/c:extension/scext:executeScript/*"/>
                </scriptExecutionRequest>
            </nonIterativeScripting>
        </work>
        <xsl:call-template name="controlFlow"/>
        <xsl:call-template name="distributionSimpleActivity"/>
    </xsl:template>

</xsl:stylesheet>
