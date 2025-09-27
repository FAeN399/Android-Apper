package com.example.androidapper.creator

import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.io.path.createTempDirectory
import kotlin.io.path.inputStream
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppTemplateGeneratorTest {

    @Test
    fun `generator reuses sanitized theme across manifest and theme resources`() {
        val generator: AppTemplateGenerator = AppTemplateGenerator()
        val tempDir = createTempDirectory(prefix = "android-apper-")

        try {
            val result: AppTemplateGenerator.GenerationResult = generator.generate(
                appName = "My Fancy App!",
                outputDir = tempDir,
            )

            val manifestContent: String = result.manifestPath.readText()
            val themeContent: String = result.themePath.readText()
            val documentBuilderFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance().apply {
                isNamespaceAware = true
            }
            val manifestThemeName: String = result.manifestPath.inputStream().use { input: InputStream ->
                val document = documentBuilderFactory.newDocumentBuilder().parse(input)
                val applicationNode = document.getElementsByTagName("application").item(0)
                val themeAttribute: String = (applicationNode?.attributes?.getNamedItemNS(
                    "http://schemas.android.com/apk/res/android",
                    "theme",
                )?.nodeValue) ?: error("Manifest did not expose a theme: \n$manifestContent")
                themeAttribute.substringAfterLast('/')
            }

            assertEquals(expected = result.themeName, actual = manifestThemeName)
            assertTrue(actual = themeContent.contains("<style name=\"${result.themeName}\""))
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun `sanitize theme name produces predictable camel case`() {
        val generator: AppTemplateGenerator = AppTemplateGenerator()
        val sanitized: String = generator.sanitizeThemeName("hello world!!")
        assertEquals(expected = "ThemeHelloWorld", actual = sanitized)
    }
}
