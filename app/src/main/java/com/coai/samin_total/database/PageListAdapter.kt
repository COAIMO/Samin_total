package com.coai.samin_total.database
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.*
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.coai.samin_total.databinding.ListItemBinding

class PageListAdapter : PagingDataAdapter<AlertData, PageViewHolder>(DIFF_COMPARATOR){
    companion object {
        private val DIFF_COMPARATOR = object : DiffUtil.ItemCallback<AlertData>(){
            override fun areItemsTheSame(oldItem: AlertData, newItem: AlertData): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: AlertData, newItem: AlertData): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bindTo(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        var binding : ListItemBinding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        var viewModel : ListItemViewModel = ViewModelProvider(ViewModelStore(), ViewModelFactory(null)).get(ListItemViewModel::class.java)

        return PageViewHolder(binding, viewModel)
    }
}