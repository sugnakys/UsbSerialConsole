package jp.sugnakys.usbserialconsole.device

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.WindowManager
import com.google.common.truth.Truth
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import jp.sugnakys.usbserialconsole.R
import org.junit.Before
import org.junit.Test

class DeviceRepositoryTest {

    @MockK(relaxed = true)
    val context = mockk<Context>()

    @MockK(relaxed = true)
    val activity = mockk<Activity>()

    private val target = DeviceRepository(context)

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { context.getString(R.string.line_feed_code_cr_value) } returns "cr"
        every { context.getString(R.string.line_feed_code_lf_value) } returns "lf"
        every { activity.getString(R.string.screen_orientation_portrait_value) } returns "portrait"
        every { activity.getString(R.string.screen_orientation_landscape_value) } returns "landscape"
        every { activity.getString(R.string.screen_orientation_reverse_portrait_value) } returns "reverse_portrait"
        every { activity.getString(R.string.screen_orientation_reverse_landscape_value) } returns "reverse_landscape"
        every { activity.requestedOrientation = any() } just Runs
        every { activity.window.addFlags(any()) } just Runs
        every { activity.window.clearFlags(any()) } just Runs
    }

    @Test
    fun getLineFeedCode_input_cr_return_cr() {
        // arrange

        // act
        val result = target.getLineFeedCode("cr")

        // assert
        Truth.assertThat(result).isEqualTo("\r")
    }

    @Test
    fun getLineFeedCode_input_lf_return_lf() {
        // arrange

        // act
        val result = target.getLineFeedCode("lf")

        // assert
        Truth.assertThat(result).isEqualTo("\n")
    }

    @Test
    fun getLineFeedCode_input_any_return_crlf() {
        // arrange

        // act
        val result = target.getLineFeedCode("any_chars")

        // assert
        Truth.assertThat(result).isEqualTo("\r\n")
    }

    @Test
    fun setScreenOrientation_setPortrait() {
        // arrange

        // act
        target.setScreenOrientation("portrait", activity)

        // assert
        verify(exactly = 1) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    @Test
    fun setScreenOrientation_setLandscape() {
        // arrange

        // act
        target.setScreenOrientation("landscape", activity)

        // assert
        verify(exactly = 1) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    @Test
    fun setScreenOrientation_setReversePortrait() {
        // arrange

        // act
        target.setScreenOrientation("reverse_portrait", activity)

        // assert
        verify(exactly = 1) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
        }
    }

    @Test
    fun setScreenOrientation_setReverseLandscape() {
        // arrange

        // act
        target.setScreenOrientation("reverse_landscape", activity)
        // assert
        verify(exactly = 1) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        }
    }

    @Test
    fun setSleepMode_setTrue() {
        // arrange

        // act
        target.setSleepMode(true, activity)

        // assert
        verify(exactly = 1) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    @Test
    fun setSleepMode_setFalse() {
        // arrange

        // act
        target.setSleepMode(false, activity)

        // assert
        verify(exactly = 1) {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}