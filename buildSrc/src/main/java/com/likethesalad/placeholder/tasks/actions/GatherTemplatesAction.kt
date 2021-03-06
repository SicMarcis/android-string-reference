package com.likethesalad.placeholder.tasks.actions

import com.likethesalad.placeholder.data.Constants
import com.likethesalad.placeholder.data.resources.ResourcesHandler
import com.likethesalad.placeholder.data.storage.FilesProvider
import com.likethesalad.placeholder.data.storage.IncrementalDataCleaner
import com.likethesalad.placeholder.models.StringResourceModel
import com.likethesalad.placeholder.models.StringsGatheredModel
import com.likethesalad.placeholder.models.StringsTemplatesModel
import java.io.File

class GatherTemplatesAction(
    private val filesProvider: FilesProvider,
    private val resourcesHandler: ResourcesHandler,
    private val incrementalDataCleaner: IncrementalDataCleaner
) {
    fun getStringFiles(): List<File> {
        return filesProvider.getAllGatheredStringsFiles()
    }

    fun getTemplatesFiles(): List<File> {
        return filesProvider.getAllExpectedTemplatesFiles()
    }

    fun gatherTemplateStrings() {
        incrementalDataCleaner.clearTemplateStrings()

        for (stringFile in filesProvider.getAllGatheredStringsFiles()) {
            val gatheredString = resourcesHandler.getGatheredStringsFromFile(stringFile)
            resourcesHandler.saveTemplates(gatheredStringsToTemplateStrings(gatheredString))
        }
    }

    private fun gatheredStringsToTemplateStrings(
        gatheredStrings: StringsGatheredModel
    ): StringsTemplatesModel {
        val mergedStrings = gatheredStrings.mergedStrings
        val stringTemplates = mergedStrings.filter { Constants.TEMPLATE_STRING_REGEX.containsMatchIn(it.name) }
        val placeholdersResolved = getPlaceholdersResolved(mergedStrings, stringTemplates)

        return StringsTemplatesModel(gatheredStrings.pathIdentity, stringTemplates, placeholdersResolved)
    }

    private fun getPlaceholdersResolved(
        strings: Collection<StringResourceModel>,
        templates: Collection<StringResourceModel>
    ): Map<String, String> {
        val stringsMap = stringResourcesToMap(strings)
        val placeholders = templates.map { Constants.PLACEHOLDER_REGEX.findAll(it.content) }
            .flatMap { it.toList().map { m -> m.groupValues[1] } }.toSet()

        val placeholdersResolved = mutableMapOf<String, String>()

        for (it in placeholders) {
            placeholdersResolved[it] = stringsMap.getValue(it)
        }

        return placeholdersResolved
    }

    private fun stringResourcesToMap(list: Collection<StringResourceModel>): Map<String, String> {
        val map = mutableMapOf<String, String>()
        for (it in list) {
            map[it.name] = it.content
        }
        return map
    }
}