package com.likethesalad.placeholder

import com.google.common.truth.Truth
import com.likethesalad.placeholder.testutils.TestAssetsProvider
import com.likethesalad.placeholder.testutils.app.content.ValuesResFoldersPlacer
import com.likethesalad.placeholder.testutils.app.layout.AndroidAppProjectDescriptor
import com.likethesalad.placeholder.testutils.base.BaseAndroidProjectTest
import com.likethesalad.placeholder.testutils.base.layout.ProjectDescriptor
import org.gradle.testkit.runner.BuildResult
import org.junit.Test
import java.io.File

class CheckOutputsTest : BaseAndroidProjectTest() {

    private val inputAssetsProvider = TestAssetsProvider("inputs")
    private val outputAssetsProvider = TestAssetsProvider("outputs")

    @Test
    fun `verify basic app outputs`() {
        runInputOutputComparisonTest("basic", listOf("debug"))
    }

    @Test
    fun `verify multi-languages app outputs`() {
        runInputOutputComparisonTest(
            "multi-languages", listOf("debug")
        )
    }

    private fun runInputOutputComparisonTest(
        inOutDirName: String, variantNames: List<String>
    ) {
        val descriptor = AndroidAppProjectDescriptor(inOutDirName)
        val inputDir = getInputTestAsset(inOutDirName)
        descriptor.projectDirectoryBuilder.register(ValuesResFoldersPlacer(inputDir))

        createProjectAndRunStringResolver(descriptor, variantNames)

        variantNames.forEach {
            verifyExpectedOutput(inOutDirName, it)
        }
    }

    private fun createProjectAndRunStringResolver(
        projectDescriptor: ProjectDescriptor,
        variantNames: List<String>
    ): BuildResult {
        val commandList = variantNames.map { "resolve${it.capitalize()}Placeholders" }
        return createProjectAndRun(projectDescriptor, commandList)
    }

    private fun getInputTestAsset(inputDirName: String): File {
        return inputAssetsProvider.getAssetFile(inputDirName)
    }

    private fun getOutputTestAsset(outputDirName: String): File {
        return outputAssetsProvider.getAssetFile(outputDirName)
    }

    private fun verifyExpectedOutput(
        inOutDirName: String,
        variantName: String
    ) {
        val projectDir = getProjectDir(inOutDirName)
        val resultDir = File(projectDir, "build/generated/resolved/$variantName")
        Truth.assertThat(resultDir.exists()).isTrue()
        verifyDirsContentsAreEqual(getExpectedOutputDir(inOutDirName, variantName), resultDir)
    }

    private fun getExpectedOutputDir(inOutDirName: String, variantName: String): File {
        // Return specific variant's outputs dir if any, else fallback to "main".
        val expectedOutputRootDir = getOutputTestAsset(inOutDirName)
        val variantOutputDir = File(expectedOutputRootDir, variantName)
        if (variantOutputDir.exists()) {
            return variantOutputDir
        }

        return File(expectedOutputRootDir, "main")
    }

    private fun verifyDirsContentsAreEqual(dir1: File, dir2: File) {
        val dir1Files = dir1.listFiles()?.asList() ?: emptyList()
        val dir2Files = dir2.listFiles()?.asList() ?: emptyList()
        if (dir1Files.isEmpty() && dir2Files.isEmpty()) {
            return
        }
        checkRootContentFileNames(dir1Files, dir2Files)
        dir1Files.forEach { dir1File ->
            if (dir1File.isFile) {
                checkIfFileIsInList(dir1File, dir2Files)
            } else {
                verifyDirsContentsAreEqual(dir1File, dir2Files.first { it.name == dir1File.name })
            }
        }
    }

    private fun checkRootContentFileNames(dirFiles1: List<File>, dirFiles2: List<File>) {
        val dirFileNames1 = dirFiles1.map { it.name }
        val dirFileNames2 = dirFiles2.map { it.name }
        Truth.assertThat(dirFileNames1).containsExactlyElementsIn(dirFileNames2)
    }

    private fun checkIfFileIsInList(file: File, list: List<File>) {
        val fileWithSameName = list.first { it.name == file.name }
        Truth.assertThat(file.readText()).isEqualTo(fileWithSameName.readText())
    }

    override fun getAndroidBuildPluginVersion(): String = "3.3.3"
}