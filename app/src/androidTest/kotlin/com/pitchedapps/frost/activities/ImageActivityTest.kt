/*
 * Copyright 2018 Allan Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.pitchedapps.frost.activities

import android.content.Intent
import androidx.core.view.isVisible
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.pitchedapps.frost.helper.TEST_FORMATTED_URL
import com.pitchedapps.frost.helper.activityRule
import com.pitchedapps.frost.helper.getResource
import com.pitchedapps.frost.utils.ARG_COOKIE
import com.pitchedapps.frost.utils.ARG_IMAGE_URL
import com.pitchedapps.frost.utils.ARG_TEXT
import com.pitchedapps.frost.utils.isIndirectImageUrl
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import okhttp3.internal.closeQuietly
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer
import okio.source
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

@HiltAndroidTest
class ImageActivityTest {

  @get:Rule(order = 0) val hildAndroidRule = HiltAndroidRule(this)

  @get:Rule(order = 1)
  val activityRule =
    activityRule<ImageActivity>(intentAction = { putExtra(ARG_IMAGE_URL, TEST_FORMATTED_URL) })

  @get:Rule(order = 2) val globalTimeout: Timeout = Timeout.seconds(15)

  lateinit var mockServer: MockWebServer

  @Before
  fun before() {
    mockServer = mockServer()
  }

  @After
  fun after() {
    mockServer.closeQuietly()
  }

  @Test
  fun initializesSuccessfully() =
    launchScenario(mockServer.url("image").toString()) {
      // Verify no crash
    }

  @Test
  fun validImageTest() =
    launchScenario(mockServer.url("image").toString()) {
      mockServer.takeRequest()
      assertEquals(1, mockServer.requestCount, "One http request expected")
      //            assertEquals(
      //                FabStates.DOWNLOAD,
      //                fabAction,
      //                "Image should be successful, image should be downloaded"
      //            )
      assertFalse(binding.error.isVisible, "Error should not be shown")
      val tempFile = assertNotNull(tempFile, "Temp file not created")
      assertTrue(tempFile.exists(), "Image should be located at temp file")
      assertTrue(
        System.currentTimeMillis() - tempFile.lastModified() < 2000L,
        "Image should have been modified within the last few seconds"
      )
      assertNull(errorRef, "No error should exist")
      tempFile.delete()
    }

  @Test
  @Ignore("apparently this fails")
  fun invalidImageTest() =
    launchScenario(mockServer.url("text").toString()) {
      mockServer.takeRequest()
      assertEquals(1, mockServer.requestCount, "One http request expected")
      assertTrue(binding.error.isVisible, "Error should be shown")

      //            assertEquals(
      //                FabStates.ERROR,
      //                fabAction,
      //                "Text should not be a valid image format, error state expected"
      //            )
      assertEquals("Image format not supported", errorRef?.message, "Error message mismatch")
      assertFalse(tempFile?.exists() == true, "Temp file should have been removed")
    }

  @Test
  fun errorTest() =
    launchScenario(mockServer.url("error").toString()) {
      mockServer.takeRequest()
      assertEquals(1, mockServer.requestCount, "One http request expected")
      assertTrue(binding.error.isVisible, "Error should be shown")
      //            assertEquals(FabStates.ERROR, fabAction, "Error response code, error state
      // expected")
      assertEquals(
        "Unsuccessful response for image: Error mock response",
        errorRef?.message,
        "Error message mismatch"
      )
      assertFalse(tempFile?.exists() == true, "Temp file should have been removed")
    }

  private fun launchScenario(
    imageUrl: String,
    text: String? = null,
    cookie: String? = null,
    action: ImageActivity.() -> Unit
  ) {
    assertFalse(
      imageUrl.isIndirectImageUrl,
      "For simplicity, urls that are direct will be used without modifications in the production code."
    )
    val intent =
      Intent(ApplicationProvider.getApplicationContext(), ImageActivity::class.java).apply {
        putExtra(ARG_IMAGE_URL, imageUrl)
        putExtra(ARG_TEXT, text)
        putExtra(ARG_COOKIE, cookie)
      }
    ActivityScenario.launch<ImageActivity>(intent).use { it.onActivity(action) }
  }

  private fun mockServer(): MockWebServer {
    val img = Buffer()
    img.writeAll(getResource("bayer-pattern.jpg").source())
    return MockWebServer().apply {
      dispatcher =
        object : Dispatcher() {
          override fun dispatch(request: RecordedRequest): MockResponse =
            when {
              request.path?.contains("text") == true ->
                MockResponse().setResponseCode(200).setBody("Valid mock text response")
              request.path?.contains("image") == true ->
                MockResponse().setResponseCode(200).setBody(img)
              else -> MockResponse().setResponseCode(404).setBody("Error mock response")
            }
        }
      start()
    }
  }
}
