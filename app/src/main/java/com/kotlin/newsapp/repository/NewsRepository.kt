package com.kotlin.newsapp.repository

import com.kotlin.newsapp.api.RetrofitInstance
import com.kotlin.newsapp.db.ArticleDatabase
import com.kotlin.newsapp.model.Article

class NewsRepository(
    val db: ArticleDatabase
) {

    suspend fun getBreakingNews(countryCode: String, pageNumber: Int) =
        RetrofitInstance.api.getBreakingNews(countryCode, pageNumber)

    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        RetrofitInstance.api.searchForNews(searchQuery, pageNumber)

    fun getSavedNews() = db.getArticleDao().getAllArticles()

    suspend fun upsert(article: Article) = db.getArticleDao().upsert(article)

    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)

}