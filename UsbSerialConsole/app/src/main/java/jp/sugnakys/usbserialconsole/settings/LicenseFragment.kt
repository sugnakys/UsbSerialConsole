package jp.sugnakys.usbserialconsole.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import jp.sugnakys.usbserialconsole.databinding.FragmentLicenseBinding

class LicenseFragment : Fragment() {

    private lateinit var binding: FragmentLicenseBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLicenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.webview.settings.javaScriptEnabled = false
        binding.webview.loadUrl("file:///android_asset/license/licenses.html")
    }
}