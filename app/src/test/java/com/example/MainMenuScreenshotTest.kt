package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.Instance
import com.example.ui.screens.MainMenuScreen
import com.example.ui.theme.VMManagerTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class MainMenuScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun main_menu_screenshot() {
        val sampleInstances = listOf(
            Instance(
                id = 1,
                name = "Alpine Linux 3.19",
                iconUri = null,
                osImageUri = null,
                ramMb = 1024,
                storageGb = 4
            ),
            Instance(
                id = 2,
                name = "Ubuntu Core 22",
                iconUri = null,
                osImageUri = null,
                ramMb = 2048,
                storageGb = 16
            )
        )

        composeTestRule.setContent {
            VMManagerTheme {
                MainMenuScreen(
                    instances = sampleInstances,
                    onAddInstanceClick = {},
                    onInstanceClick = {},
                    onDeleteInstance = {},
                    onNavigateToDeveloperOptions = {},
                    onNavigateToSettings = {}
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/main_menu.png")
    }
}
