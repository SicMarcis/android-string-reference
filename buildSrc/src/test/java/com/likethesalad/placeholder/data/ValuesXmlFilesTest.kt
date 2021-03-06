package com.likethesalad.placeholder.data

import com.google.common.truth.Truth
import com.likethesalad.placeholder.models.StringResourceModel
import org.junit.Test
import java.io.File

class ValuesXmlFilesTest {

    @Test
    fun `Get string resources from files`() {
        val valuesFilesSet = setOf(
            getResourceFile("strings_1.xml"),
            getResourceFile("strings_2.xml")
        )

        val valuesFiles = ValuesXmlFiles(valuesFilesSet)

        Truth.assertThat(valuesFiles.stringResources).containsExactly(
            StringResourceModel("welcome_1", "The welcome message for TesT1"),
            StringResourceModel("welcome_3", "The welcome message for TesT3"),
            StringResourceModel(
                mapOf(
                    "name" to "message_non_translatable_1",
                    "translatable" to "false"
                ),
                "Non translatable TesT2"
            )
        )
    }

    private fun getResourceFile(fileName: String): File {
        return File(javaClass.getResource("raw/$fileName").file)
    }
}