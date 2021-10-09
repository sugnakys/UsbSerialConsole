package jp.sugnakys.usbserialconsole.presentation.home

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.Date
import javax.inject.Inject
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.databinding.FragmentHomeBinding
import jp.sugnakys.usbserialconsole.preference.DefaultPreference
import jp.sugnakys.usbserialconsole.presentation.log.LogViewFragmentArgs

@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject
    lateinit var preference: DefaultPreference

    private val viewModel by viewModels<HomeViewModel>()
    private lateinit var binding: FragmentHomeBinding

    private var isAutoScroll = true

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
        item.isEnabled = viewModel.isUSBReady
        item.title = if (viewModel.isConnect.value == true) {
            getString(R.string.action_disconnect)
        } else {
            getString(R.string.action_connect)
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_connect -> {
                if (viewModel.isConnect.value == true) {
                    viewModel.changeConnection(false)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.stop_connection),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    viewModel.changeConnection(true)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.start_connection),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                true
            }
            R.id.action_clear_log -> {
                viewModel.clearReceivedMessage()
                true
            }
            R.id.action_save_log -> {
                val fileName = viewModel.getFileName(Date(System.currentTimeMillis()))
                val dirName = viewModel.getLogDir()
                val targetFile = File(dirName, fileName)
                if (viewModel.writeToFile(
                        file = targetFile,
                        isTimestamp = preference.timestampVisibility
                    )
                ) {
                   val snackBar = Snackbar.make(
                        requireContext(),
                        binding.mainLayout,
                        "${requireContext().getString(R.string.action_save_log)} : $fileName",
                        Snackbar.LENGTH_LONG
                    )
                    snackBar.setAction(R.string.open) {
                        val args = LogViewFragmentArgs(targetFile.toUri().toString()).toBundle()
                        findNavController()
                            .navigate(
                                R.id.action_homeFragment_to_logViewFragment,
                                args
                            )
                    }
                    snackBar.show()
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

        val adapter = LogViewListAdapter(preference)
        binding.receivedMsgView.adapter = adapter
        binding.receivedMsgView.itemAnimator = null

        binding.receivedMsgView.addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_DRAGGING) {
                    isAutoScroll = false
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!recyclerView.canScrollVertically(1)) {
                    isAutoScroll = true
                }
            }
        })

        binding.sendMsgView.addTextChangedListener { text ->
            binding.sendBtn.isEnabled = text?.isNotEmpty() ?: false
        }

        binding.sendBtn.setOnClickListener {
            viewModel.sendMessage(binding.sendMsgView.text.toString())
            binding.sendMsgView.text.clear()
        }

        viewModel.receivedMessage.observe(viewLifecycleOwner, {
            adapter.submitList(it)
            if (isAutoScroll) {
                binding.receivedMsgView.scrollToPosition(adapter.itemCount - 1)
            }
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
            binding.sendMsgView.setTextColor(it)
        }

    }

    private fun setDefaultColor() {
        if (preference.colorConsoleBackgroundDefault == null) {
            var defaultBackgroundColor = Color.TRANSPARENT
            val background = binding.mainLayout.background
            if (background is ColorDrawable) {
                defaultBackgroundColor = background.color
            }
            preference.colorConsoleBackgroundDefault = defaultBackgroundColor
            if (preference.colorConsoleBackground == null) {
                preference.colorConsoleBackground = preference.colorConsoleBackgroundDefault
            }
        }

        if (preference.colorConsoleTextDefault == null) {
            val defaultTextColor = binding.sendMsgView.textColors.defaultColor
            preference.colorConsoleTextDefault = defaultTextColor
            if (preference.colorConsoleText == null) {
                preference.colorConsoleText = preference.colorConsoleTextDefault
            }
        }
    }
}