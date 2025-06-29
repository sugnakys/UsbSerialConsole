package jp.sugnakys.usbserialconsole.presentation.log

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.databinding.FragmentLogBinding
import timber.log.Timber

class LogViewFragment : Fragment() {

    private val args: LogViewFragmentArgs by navArgs()

    private val viewModel by viewModels<LogViewViewModel>()
    private lateinit var binding: FragmentLogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val file = Uri.parse(args.uriString).toFile()
        (activity as AppCompatActivity).supportActionBar?.title = file.name
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_log_view, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_share -> {
                        viewModel.logFile.value?.let {
                            val uri = FileProvider.getUriForFile(
                                requireContext(),
                                requireContext().packageName + ".provider",
                                it
                            )
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_STREAM, uri)
                                type = "text/plain"
                            }
                            startActivity(
                                Intent.createChooser(
                                    intent,
                                    resources.getText(R.string.send)
                                )
                            )
                        }
                        true
                    }

                    else -> {
                        Timber.e("Unknown id")
                        false
                    }
                }
            }
        }, viewLifecycleOwner)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel

        viewModel.setLogfile(Uri.parse(args.uriString).toFile())
    }
}