package com.example.androidapper.creator

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale

/**
 * Generates the minimal set of files required for a freshly scaffolded Android
 * application.
 */
public class AppTemplateGenerator(
    private val fileWriter: FileWriter = FileWriter(),
) {

    /**
     * Holds metadata about the generated files for convenient assertions in tests.
     */
    public data class GenerationResult(
        val manifestPath: Path,
        val themePath: Path,
        val themeName: String,
    )

    /**
     * Generates the manifest and theme resources for the provided [appName].
     *
     * The method derives the theme name once and reuses it across all
     * generated artifacts.
     */
    public fun generate(appName: String, outputDir: Path): GenerationResult {
        val sanitizedThemeName: String = sanitizeThemeName(appName)
        val manifestContent: String = buildAndroidManifest(appName, sanitizedThemeName)
        val themeContent: String = generatedTheme(sanitizedThemeName)

        val manifestPath: Path = outputDir.resolve("src/main/AndroidManifest.xml")
        val themePath: Path = outputDir.resolve("src/main/res/values/themes.xml")

        fileWriter.write(manifestPath, manifestContent)
        fileWriter.write(themePath, themeContent)

        return GenerationResult(
            manifestPath = manifestPath,
            themePath = themePath,
            themeName = sanitizedThemeName,
        )
    }

    /**
     * Builds the Android manifest XML for the provided [appName] using the
     * supplied [sanitizedThemeName].
     */
    public fun buildAndroidManifest(appName: String, sanitizedThemeName: String): String {
        return """
            |<?xml version="1.0" encoding="utf-8"?>
            |<manifest xmlns:android="http://schemas.android.com/apk/res/android">
            |    <application
            |        android:label="$appName"
            |        android:theme="@style/$sanitizedThemeName">
            |        <activity android:name=".MainActivity">
            |            <intent-filter>
            |                <action android:name="android.intent.action.MAIN" />
            |                <category android:name="android.intent.category.LAUNCHER" />
            |            </intent-filter>
            |        </activity>
            |    </application>
            |</manifest>
        """.trimMargin()
    }

    /**
     * Builds the Android manifest XML and automatically derives the theme name.
     */
    public fun buildAndroidManifest(appName: String): String {
        val sanitizedThemeName: String = sanitizeThemeName(appName)
        return buildAndroidManifest(appName, sanitizedThemeName)
    }

    /**
     * Generates the primary theme XML for the provided [sanitizedThemeName].
     */
    public fun generatedTheme(sanitizedThemeName: String): String {
        return """
            |<resources>
            |    <style name="$sanitizedThemeName" parent="Theme.Material3.DayNight.NoActionBar">
            |        <item name="android:statusBarColor">@android:color/transparent</item>
            |        <item name="android:navigationBarColor">@android:color/transparent</item>
            |    </style>
            |</resources>
        """.trimMargin()
    }

    /**
     * Sanitises the theme name by stripping non-alphanumeric characters and
     * converting the result to upper camel case.
     */
    public fun sanitizeThemeName(appName: String): String {
        val parts: List<String> = appName
            .split("[^A-Za-z0-9]+".toRegex())
            .filter { part -> part.isNotBlank() }
            .map { part ->
                val lowerCased: String = part.lowercase(Locale.US)
                lowerCased.replaceFirstChar { character ->
                    if (character.isLowerCase()) character.titlecase(Locale.US) else character.toString()
                }
            }

        val condensed: String = parts.joinToString(separator = "")
        val baseName: String = if (condensed.isEmpty()) "App" else condensed
        val prefixAdjusted: String = if (baseName.startsWith("Theme")) baseName else "Theme$baseName"
        return prefixAdjusted
    }

    /**
     * Simple file writer helper that always writes UTF-8 encoded content.
     */
    public class FileWriter {
        public fun write(path: Path, content: String): Unit {
            val parent: Path? = path.parent
            if (parent != null) {
                Files.createDirectories(parent)
            }
            Files.writeString(path, content, StandardCharsets.UTF_8)
        }
    }
}
