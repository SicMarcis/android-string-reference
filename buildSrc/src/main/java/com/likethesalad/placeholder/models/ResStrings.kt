package com.likethesalad.placeholder.models

import com.likethesalad.placeholder.data.Constants

class ResStrings(
    strings: List<StringResourceModel>,
    private val parentResStrings: ResStrings? = null
) {
    private val stringsMap = mutableMapOf<String, StringResourceModel>()

    val mergedStrings: List<StringResourceModel> by lazy {
        val mergedMap = mutableMapOf<String, StringResourceModel>()
        if (parentResStrings != null) {
            addStringsToMap(parentResStrings.mergedStrings, mergedMap)
        }

        addStringsToMap(stringsMap.values, mergedMap)
        mergedMap.values.sorted()
    }
    val mergedTemplates: List<StringResourceModel> by lazy {
        val mergedMap = mutableMapOf<String, StringResourceModel>()
        if (parentResStrings != null) {
            val templates = parentResStrings.mergedTemplates
            for (it in templates) {
                mergedMap[it.name] = it
            }
        }

        val localTemplates = getLocalTemplates()
        for (it in localTemplates) {
            mergedMap[it.name] = it
        }

        mergedMap.values.sorted()
    }

    init {
        addStringsToMap(strings, stringsMap)
    }

    fun hasLocalTemplates(): Boolean {
        return stringsMap.keys.any { Constants.TEMPLATE_STRING_REGEX.matches(it) }
    }

    fun hasLocalValuesForTemplates(): Boolean {
        val nonTemplatesPlaceholders = getLocalNonTemplatesNames().map { "\${$it}" }
        val mergedTemplatesContents = mergedTemplates.map { it.content }
        return nonTemplatesPlaceholders.any { placeholder -> mergedTemplatesContents.any { it.contains(placeholder) } }
    }

    private fun getLocalNonTemplatesNames(): List<String> {
        return stringsMap.keys.filter { !Constants.TEMPLATE_STRING_REGEX.matches(it) }
    }

    private fun getLocalTemplates(): List<StringResourceModel> {
        return stringsMap.values.filter { Constants.TEMPLATE_STRING_REGEX.matches(it.name) }
    }

    private fun addStringsToMap(
        strings: Collection<StringResourceModel>,
        map: MutableMap<String, StringResourceModel>
    ) {
        for (it in strings) {
            map[it.name] = it
        }
    }
}
