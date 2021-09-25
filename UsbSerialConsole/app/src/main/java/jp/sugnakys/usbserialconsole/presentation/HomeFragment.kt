package jp.sugnakys.usbserialconsole.presentation

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
import jp.sugnakys.usbserialconsole.util.Util
import java.io.File

@AndroidEntryPoint
class HomeFragment : Fragment() {

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_connect -> {
                viewModel.changeConnection()
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

        activity?.title = getString(R.string.app_name)

        binding.sendMsgView.addTextChangedListener { text ->
            binding.sendBtn.isEnabled = text?.isNotEmpty() ?: false
        }

        binding.sendBtn.setOnClickListener {
            viewModel.sendMessage(binding.sendMsgView.text.toString())
            binding.sendMsgView.text.clear()
        }
    }
}