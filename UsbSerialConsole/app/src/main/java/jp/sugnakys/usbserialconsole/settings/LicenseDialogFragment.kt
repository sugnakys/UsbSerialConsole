package jp.sugnakys.usbserialconsole.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.view.View
import android.webkit.WebView
import jp.sugnakys.usbserialconsole.R
import android.webkit.WebViewClient

class LicenseDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater = activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        @SuppressLint("InflateParams") val content =
            inflater.inflate(R.layout.license_view_main, null)
        val webView = content.findViewById<View>(R.id.webview) as WebView
        webView.webViewClient = WebViewClient()
        webView.loadUrl("file:///android_asset/license/license.html")
        builder.setTitle(getString(R.string.license_title))
        builder.setView(content)
        return builder.create()
    }
}