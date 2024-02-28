package com.coai.samin_total.database

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState

open class PagingSourc(private  val alertDAO: AlertDAO): PagingSource<Int, AlertData>() {
    private companion object {
        const val INIT_PAGE_INDEX = 0
    }

    override fun getRefreshKey(state: PagingState<Int, AlertData>): Int? {
        return state.anchorPosition?.let { achorPosition ->
            state.closestPageToPosition(achorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(achorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AlertData> {
        val position = params.key ?: INIT_PAGE_INDEX
        val loadData = alertDAO.getPage(position)
        Log.d("load", " load =============params.key : ${params.key} params.loadSize: ${params.loadSize} position : $position loadData : ${loadData.size}============ load")

        return LoadResult.Page(
            data = loadData,
            prevKey = if (position == INIT_PAGE_INDEX) null else position - 1,
            nextKey = if (loadData.isNullOrEmpty()) null else position + 1
        )
    }
}