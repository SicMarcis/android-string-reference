package com.likethesalad.placeholder.data.resources

import com.google.gson.Gson
import com.likethesalad.placeholder.data.OutputStringFileResolver
import com.likethesalad.placeholder.models.PathIdentity
import com.likethesalad.placeholder.models.StringResourceModel
import com.likethesalad.placeholder.models.StringsGatheredModel
import com.likethesalad.placeholder.models.StringsTemplatesModel
import com.likethesalad.placeholder.utils.AndroidXmlResDocument
import java.io.File


class AndroidResourcesHandler(
    private val outputStringFileResolver: OutputStringFileResolver
) : ResourcesHandler {

    private val gson = Gson()

    override fun getGatheredStringsFromFile(stringFile: File): StringsGatheredModel {
        return gson.fromJson(stringFile.readText(), StringsGatheredModel::class.java)
    }

    override fun saveGatheredStrings(stringsGathered: StringsGatheredModel) {
        val jsonStrings = gson.toJson(stringsGathered)
        outputStringFileResolver.getRawStringsFile(stringsGathered.pathIdentity.suffix).writeText(jsonStrings)
    }

    override fun saveResolvedStringList(
        resolvedStrings: List<StringResourceModel>,
        pathIdentity: PathIdentity
    ) {
        val resDocument = AndroidXmlResDocument()
        resDocument.appendAllStringResources(resolvedStrings)
        resDocument.saveToFile(outputStringFileResolver.getResolvedStringsFile(pathIdentity.valuesFolderName))
    }

    override fun getTemplatesFromFile(templateFile: File): StringsTemplatesModel {
        return gson.fromJson(templateFile.readText(), StringsTemplatesModel::class.java)
    }

    override fun saveTemplates(templates: StringsTemplatesModel) {
        val jsonTemplates = gson.toJson(templates)
        outputStringFileResolver.getTemplateStringsFile(templates.pathIdentity.suffix).writeText(jsonTemplates)
    }
}