package com.kotlin.newsapp.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.kotlin.newsapp.db.ArticleDatabase
import com.kotlin.newsapp.repository.NewsRepository
import java.lang.Exception

abstract class BaseFragment : Fragment() {

    lateinit var viewmodel: NewsViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = NewsRepository(ArticleDatabase(requireContext()))
        val providerFactory = ViewModelProviderFactory(activity!!.application,repository)

        val newsActivity = activity as NewsActivity

        viewmodel = newsActivity.run {
            ViewModelProvider(this, providerFactory).get(NewsViewModel::class.java)
        } ?: throw Exception("Invalid Activity")
    }

}