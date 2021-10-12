package jp.sugnakys.usbserialconsole.presentation.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.sugnakys.usbserialconsole.R
import jp.sugnakys.usbserialconsole.data.LogItem
import jp.sugnakys.usbserialconsole.databinding.ListLogViewAtBinding
import jp.sugnakys.usbserialconsole.preference.DefaultPreference

class LogViewListAdapter(
    private val preference: DefaultPreference
) : ListAdapter<LogItem, LogViewListAdapter.LogViewHolder>(diffCallback) {
    companion object {
        val diffCallback = object : DiffUtil.ItemCallback<LogItem>() {
            override fun areItemsTheSame(oldItem: LogItem, newItem: LogItem): Boolean {
                return oldItem.timestamp == newItem.timestamp
            }

            override fun areContentsTheSame(oldItem: LogItem, newItem: LogItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return LogViewHolder(DataBindingUtil.inflate(inflater, viewType, parent, false))
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(getItem(position), preference)
    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_log_view_at
    }

    class LogViewHolder(
        private val binding: ListLogViewAtBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(data: LogItem, pref: DefaultPreference) {
            binding.log = data
            binding.timeFormat = pref.timestampFormat
            binding.timeVisibility = pref.timestampVisibility

            pref.colorConsoleText?.let{
                binding.receivedMessage.setTextColor(it)
                binding.receivedTimestamp.setTextColor(it)
            }

        }
    }
}
