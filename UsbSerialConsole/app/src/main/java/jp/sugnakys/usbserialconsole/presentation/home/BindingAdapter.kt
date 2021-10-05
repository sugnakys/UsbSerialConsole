package jp.sugnakys.usbserialconsole.presentation.home

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("SetTextI18n")
@BindingAdapter("timestamp", "timeFormat", requireAll = true)
fun convertDate(view: TextView, timestamp: Long, format: String) {
    view.text = "[${SimpleDateFormat(format, Locale.US).format(Date(timestamp))}]"
}

@BindingAdapter("visibleOrGone")
fun visibleOrGone(view: View, visibility: Boolean) {
    view.visibility = if (visibility) View.VISIBLE else View.GONE
}