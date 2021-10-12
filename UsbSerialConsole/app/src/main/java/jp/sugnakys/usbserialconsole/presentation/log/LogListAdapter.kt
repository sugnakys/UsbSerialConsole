package jp.sugnakys.usbserialconsole.presentation.log

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.databinding.ListLogAtBinding
import java.io.File

class LogListAdapter(
    private val onClick: (file: File) -> Unit,
    private val onDeleteClick: (file: File) -> Unit
) : ListAdapter<File, LogListAdapter.LogListViewHolder>(diffCallback) {
    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<File>() {
            override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
                return oldItem.absolutePath == newItem.absolutePath
            }

            override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return LogListViewHolder(
            DataBindingUtil.inflate(inflater, viewType, parent, false),
            onClick,
            onDeleteClick
        )
    }

    override fun onBindViewHolder(holder: LogListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_log_at
    }

    class LogListViewHolder(
        private val binding: ListLogAtBinding,
        private val onClick: (file: File) -> Unit,
        private val onDeleteClick: (file: File) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(file: File) {
            binding.title = file.name
            binding.logFile.setOnClickListener {
                onClick(file)
            }
            binding.delete.setOnClickListener {
                onDeleteClick(file)
            }
        }
    }
}
