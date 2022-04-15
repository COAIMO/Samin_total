package com.coai.samin_total.database

import androidx.recyclerview.widget.RecyclerView
import com.coai.samin_total.databinding.ListItemBinding

class PageViewHolder(
    private var binding :ListItemBinding,
    var viewModel: ListItemViewModel) :
    RecyclerView.ViewHolder(binding.root) {
    fun bindTo(pageInfo: AlertData) {
        this.binding.model = viewModel
        this.viewModel.setIdText(pageInfo.apId.toString())
        this.viewModel.setErrorText(pageInfo.content)
        this.viewModel.setModelText(pageInfo.model.toString())
        this.viewModel.setPortTExt(pageInfo.port.toString())
        this.viewModel.setTimeText(pageInfo.time)

    }
}