package jp.sugnakys.usbserialconsole.presentation

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.databinding.FragmentHomeBinding
import jp.sugnakys.usbserialconsole.preference.DefaultPreference
import jp.sugnakys.usbserialconsole.util.Util
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject
    lateinit var preference: DefaultPreference

    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.action_connect)
        item.isEnabled = viewModel.isUSBReady.value ?: false
        if (viewModel.isConnect) {
            item.title = getString(R.string.action_disconnect)
        } else {
            item.title = getString(R.string.action_connect)
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_connect -> {
                (activity as? MainActivity)?.changeConnection()
                true
            }
            R.id.action_clear_log -> {
                viewModel.clearReceivedMessage()
                true
            }
            R.id.action_save_log -> {
                val fileName = Util.createLogFileName()
                val dirName = Util.getLogDir(requireContext())

                if (viewModel.writeToFile(
                        File(dirName, fileName),
                        binding.receivedMsgView.text.toString()
                    )
                ) {
                    Snackbar.make(
                        requireContext(),
                        binding.mainLayout,
                        "${requireContext().getString(R.string.action_save_log)} : $fileName",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                true
            }
            R.id.action_settings -> {
                findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
                true
            }
            R.id.action_log_list -> {
                findNavController().navigate(R.id.action_homeFragment_to_logListFragment)
                true
            }
            else -> false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel

        setDefaultColor()

        activity?.title = getString(R.string.app_name)

        binding.sendMsgView.addTextChangedListener { text ->
            binding.sendBtn.isEnabled = text?.isNotEmpty() ?: false
        }

        binding.sendBtn.setOnClickListener {
            viewModel.sendMessage(binding.sendMsgView.text.toString())
            binding.sendMsgView.text.clear()
        }

        viewModel.receivedMessage.observe(viewLifecycleOwner, {
            binding.scrollView.scrollTo(0, binding.receivedMsgView.bottom)
        })

        binding.sendViewLayout.visibility = if (preference.sendFormVisibility) {
            View.VISIBLE
        } else {
            View.GONE
        }

        preference.colorConsoleBackground?.let {
            binding.mainLayout.setBackgroundColor(it)
        }

        preference.colorConsoleText?.let {
            binding.receivedMsgView.setTextColor(it)
            binding.sendMsgView.setTextColor(it)
        }
    }

    private fun setDefaultColor() {
        if (preference.colorConsoleBackground == null) {
            var defaultBackgroundColor = Color.TRANSPARENT
            val background = binding.mainLayout.background
            if (background is ColorDrawable) {
                defaultBackgroundColor = background.color
            }
            preference.colorConsoleBackground = defaultBackgroundColor
        }

        if (preference.colorConsoleText == null) {
            val defaultTextColor = binding.receivedMsgView.textColors.defaultColor
            preference.colorConsoleText = defaultTextColor
        }
    }
}