package jp.sugnakys.usbserialconsole.preference

import android.content.Context
import android.content.SharedPreferences
import jp.sugnakys.usbserialconsole.R
import javax.inject.Inject

class DefaultPreference @Inject constructor(
    context: Context,
    preferences: SharedPreferences
) {
    var sendFormVisibility: Boolean by preferences.boolean(
        true,
        context.getString(R.string.send_form_visible_key)
    )

    var colorConsoleBackground: Int? by preferences.nullableInt(
        context.getString(R.string.color_console_background_key)
    )

    var colorConsoleBackgroundDefault: Int? by preferences.nullableInt(
        context.getString(R.string.color_console_background_key_default)
    )

    var colorConsoleText: Int? by preferences.nullableInt(
        context.getString(R.string.color_console_text_key)
    )

    var colorConsoleTextDefault: Int? by preferences.nullableInt(
        context.getString(R.string.color_console_text_key_default)
    )

    var sleepMode: Boolean by preferences.boolean(
        false,
        context.getString(R.string.sleep_mode_key)
    )

    var screenOrientation: String by preferences.string(
        context.getString(R.string.screen_orientation_default),
        context.getString(R.string.screen_orientation_key)
    )

    var lineFeedCodeSend: String by preferences.string(
        context.getString(R.string.line_feed_code_cr_lf_value),
        context.getString(R.string.line_feed_code_send_key)
    )

    var baudrate: String by preferences.string(
        context.getString(R.string.baudrate_default),
        context.getString(R.string.baudrate_key)
    )

    var databits: String by preferences.string(
        context.getString(R.string.databits_default),
        context.getString(R.string.databits_key)
    )

    var stopbits: String by preferences.string(
        context.getString(R.string.stopbits_default),
        context.getString(R.string.stopbits_key)
    )

    var parity: String by preferences.string(
        context.getString(R.string.parity_default),
        context.getString(R.string.parity_key)
    )

    var flowcontrol: String by preferences.string(
        context.getString(R.string.flowcontrol_default),
        context.getString(R.string.flowcontrol_key)
    )

    var timestampVisibility: Boolean by preferences.boolean(
        true,
        context.getString(R.string.timestamp_visible_key)
    )

    var timestampFormat: String by preferences.string(
        context.getString(R.string.timestamp_format_default),
        context.getString(R.string.timestamp_format_key)
    )
}