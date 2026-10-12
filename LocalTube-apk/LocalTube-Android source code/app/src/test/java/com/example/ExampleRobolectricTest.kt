package com.example

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.example.model.LocalVideo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("LocalTube", appName)
  }

  @Test
  fun `local video duration and short formatting`() {
    val video = LocalVideo(
      id = 1L,
      uri = Uri.parse("content://media/external/video/media/1"),
      title = "Test Skate Trick",
      displayName = "Test_Skate_Trick.mp4",
      durationMs = 45_000L,
      sizeBytes = 15_000_000L,
      dateAddedSec = 1700000000L,
      width = 1080,
      height = 1920,
      channelName = "Camera",
      path = "/storage/emulated/0/DCIM/Camera/Test_Skate_Trick.mp4"
    )

    assertEquals("00:45", video.formattedDuration)
    assertTrue("Should be recognized as a short", video.isShort)
    assertEquals("0 views", video.formattedViews)

    val updatedVideo = video.copy(
      viewCount = 1,
      customChannelName = "Action Channel",
      tags = listOf("skate", "tricks")
    )
    assertEquals("1 view", updatedVideo.formattedViews)
    assertEquals("Action Channel", updatedVideo.effectiveChannelName)
    assertTrue(updatedVideo.tags.contains("skate"))
  }
}
