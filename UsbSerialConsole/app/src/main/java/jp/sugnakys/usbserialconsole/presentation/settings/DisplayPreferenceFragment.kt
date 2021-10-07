package jp.sugnakys.usbserialconsole.presentation.settings

import android.app.AlertDialog
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.preference.DefaultPreference
import jp.sugnakys.usbserialconsole.util.Util

@AndroidEntryPoint
class DisplayPreferenceFragment : PreferenceFragmentCompat() {

    @Inject
    lateinit var preference: DefaultPreference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.fragment_display_preference)

        findPreference<ListPreference>(getString(R.string.screen_orientation_key))
            ?.setOnPreferenceChangeListener { _, newValue ->
                Util.setScreenOrientation(
                    newValue as String,
                    requireActivity()
                )
                true
            }

        findPreference<Preference>(getString(R.string.color_console_clear_key))
            ?.setOnPreferenceClickListener {
                AlertDialog.Builder(requireContext()).apply {
                    setMessage(getString(R.string.confirm_clear_color))
                    setPositiveButton(getString(android.R.string.ok)) { _, _ ->
                        preference.colorConsoleText = preference.colorConsoleTextDefault
                        preference.colorConsoleBackground = preference.colorConsoleBackgroundDefault

                        view?.let {
                            Snackbar.make(
                                requireContext(),
                                it,
                                getString(R.string.clear_color),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }

                        findNavController().popBackStack()
                    }
                    setNegativeButton(getString(android.R.string.cancel), null)
                }.create().show()

                true
            }
    }
}