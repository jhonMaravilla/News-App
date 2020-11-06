package com.kotlin.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kotlin.newsapp.R
import com.kotlin.newsapp.adapters.NewsAdapter
import com.kotlin.newsapp.ui.BaseFragment
import com.kotlin.newsapp.util.Helper
import com.kotlin.newsapp.util.Helper.Companion.QUERY_PAGE_SIZE
import com.kotlin.newsapp.util.Resource
import com.kotlin.newsapp.util.Resource.*
import kotlinx.android.synthetic.main.fragment_breaking_news.*

private const val TAG = "BreakingNewsFragment"

class BreakingNewsFragment : BaseFragment() {

    lateinit var newsAdapter: NewsAdapter
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_breaking_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        subscribeObservers()

        viewmodel.getBreakingNews("ph")

        adapterClickListener()
    }

    private fun adapterClickListener() {
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }

            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articleFragment,
                bundle
            )
        }
    }

    private fun subscribeObservers() {
        viewmodel.breakingNews.observe(viewLifecycleOwner, Observer { response ->

            when (response) {
                is Loading -> {
                    showProgressBar(true)
                }

                is Success -> {
                    showProgressBar(false)
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / QUERY_PAGE_SIZE + 2

                        Log.d(
                            TAG,
                            "${newsResponse.totalResults} ${QUERY_PAGE_SIZE + 2} ${viewmodel.breakingNewsPage}"
                        )

                        isLastPage = viewmodel.breakingNewsPage == totalPages

                        if (isLastPage) {
                            rvBreakingNews.setPadding(0, 0, 0, 0)
                        }
                    }
                }

                is Error -> {
                    showProgressBar(false)
                    response.message?.let { message ->
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    }
                }
            }

        })
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addOnScrollListener(this@BreakingNewsFragment.scrollListener)
        }
    }

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }

        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLAstItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate =
                isNotLoadingAndNotLastPage && isAtLAstItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewmodel.getBreakingNews("ph")
                isScrolling = false
            }
        }
    }

    private fun showProgressBar(showProgressBar: Boolean) {
        if (showProgressBar) {
            paginationProgressBar.visibility = VISIBLE
            isLoading = true
        } else {
            paginationProgressBar.visibility = GONE
            isLoading = false
        }
    }


}