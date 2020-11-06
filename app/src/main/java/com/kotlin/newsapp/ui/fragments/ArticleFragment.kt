package com.kotlin.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.kotlin.newsapp.R
import com.kotlin.newsapp.model.Article
import com.kotlin.newsapp.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_article.*

private const val TAG = "ArticleFragment"

class ArticleFragment : BaseFragment() {

    val args: ArticleFragmentArgs by navArgs()
    var article: Article? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_article, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupWebView()

        setSaveNews(view)
    }

    private fun setupWebView() {
        article = args.article

        webView.apply {
            // To open the url in the app and not in the phones brower
            webViewClient = WebViewClient()

            article?.let {
                loadUrl(it.url)
            }
        }
    }

    private fun setSaveNews(view: View) {

        fab.setOnClickListener {

            article?.let {
                viewmodel.saveArticle(it)
            }

            Snackbar.make(view, "Article saved!", Snackbar.LENGTH_LONG).show()
        }


    }

}