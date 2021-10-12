package jp.sugnakys.usbserialconsole.presentation.log

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.databinding.FragmentLogListBinding

@AndroidEntryPoint
class LogListFragment : Fragment() {

    private val viewModel by viewModels<LogListViewModel>()
    private lateinit var binding: FragmentLogListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLogListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        val adapter = LogListAdapter(
            onClick = {
                val action = LogListFragmentDirections
                    .actionLogListFragmentToLogViewFragment(it.toUri().toString())
                findNavController().navigate(action)
            },
            onDeleteClick = {
                showDeleteDialog(it)
            }
        )
        binding.listView.adapter = adapter

        viewModel.fileList.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        viewModel.updateFileList()
    }


    private fun showDeleteDialog(file: File) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_log_file_title))
            .setMessage(
                """
                |${getString(R.string.delete_log_file_text)}
                |${getString(R.string.file_name)}: ${file.name}
                """.trimMargin()
            )
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.deleteFile(file)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}