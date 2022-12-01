package com.evolveum.midpoint.studio.ui

import com.evolveum.midpoint.schema.constants.ObjectTypes
import com.evolveum.midpoint.studio.impl.MidPointSettings
import com.intellij.openapi.module.Module

/**
 * Created by Viliam Repan (lazyman).
 */
data class GeneralConfiguration(

    var midpointModule: Module?,

    var downloadFilePattern: String,

    var generatedFilePattern: String,

    var restClientTimeout: Int,

    var restLogCommunication: Boolean,

    var downloadTypesInclude: List<ObjectTypes>,

    var downloadTypesExclude: List<ObjectTypes>,

    var downloadLimit: Int
) {
    constructor() : this(
        null, "", "", 30,
        false, emptyList(), emptyList(), 100
    )

    fun asMidpointSettings(): MidPointSettings {
        val result = MidPointSettings()
        result.dowloadFilePattern = downloadFilePattern
        result.generatedFilePattern = generatedFilePattern
        result.restResponseTimeout = restClientTimeout
        result.isPrintRestCommunicationToConsole = restLogCommunication
        result.downloadTypesInclude = downloadTypesInclude
        result.downloadTypesExclude = downloadTypesExclude

        return result
    }
}

fun asGeneralConfiguration(settings: MidPointSettings): GeneralConfiguration {
    return GeneralConfiguration(
        null,
        settings.dowloadFilePattern,
        settings.generatedFilePattern,
        settings.restResponseTimeout,
        settings.isPrintRestCommunicationToConsole,
        settings.downloadTypesInclude,
        settings.downloadTypesExclude,
        settings.typesToDownloadLimit
    )
}
