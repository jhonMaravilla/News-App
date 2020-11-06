package com.kotlin.newsapp.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.*
import com.kotlin.newsapp.NewsApplication
import com.kotlin.newsapp.model.Article
import com.kotlin.newsapp.model.NewsResponse
import com.kotlin.newsapp.repository.NewsRepository
import com.kotlin.newsapp.util.Resource
import com.kotlin.newsapp.util.Resource.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(
    app: Application,
    val repository: NewsRepository
) : AndroidViewModel(app) {

    val _breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    val breakingNews: LiveData<Resource<NewsResponse>> = _breakingNews
    var breakingNewsPage = 1
    var breakingNewsResponse: NewsResponse? = null

    val _searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    val searchNews: LiveData<Resource<NewsResponse>> = _searchNews
    var searchPage = 1
    var searchNewsResponse: NewsResponse? = null

    fun getBreakingNews(countryCode: String) = viewModelScope.launch {
        if (hasInternetConnection()) {
            _breakingNews.postValue(Loading())

            delay(1000L)

            val response = repository.getBreakingNews(countryCode, breakingNewsPage)

            _breakingNews.postValue(handleBreakingNewsResponse(response))
        } else {
            _breakingNews.postValue(Resource.Error("Internet Connection is Required"))
        }

    }

    fun searchNews(searchQuery: String) = viewModelScope.launch {
        _searchNews.postValue(Loading())

        delay(1000L)

        val response = repository.searchNews(searchQuery, searchPage)

        _searchNews.postValue(handleSearchNewsResponse(response))
    }

    fun getSavedNews() = repository.getSavedNews()

    fun saveArticle(article: Article) = viewModelScope.launch {
        repository.upsert(article)
    }

    fun deleteArticle(article: Article) = viewModelScope.launch {
        repository.deleteArticle(article)
    }


    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {

            response.body()?.let { result ->
                if (result.status.equals("ok")) {
                    breakingNewsPage++

                    if (breakingNewsResponse == null) {
                        breakingNewsResponse = result
                    } else {
                        val oldArticles = breakingNewsResponse?.articles
                        val newArticles = result.articles

                        oldArticles?.addAll(newArticles)
                    }

                    return Success(breakingNewsResponse ?: result)
                } else {
                    return Error(response.message())
                }

            }
        }

        return Error(response.message())
    }


    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {

            response.body()?.let { result ->
                if (result.status.equals("ok")) {
                    searchPage++

                    if (searchNewsResponse == null) {
                        searchNewsResponse = result
                    } else {
                        val oldArticles = searchNewsResponse?.articles
                        val newArticles = result.articles

                        oldArticles?.addAll(newArticles)
                    }

                    return Success(searchNewsResponse ?: result)
                } else {
                    return Error(response.message())
                }

            }
        }

        return Error(response.message())
    }

    private fun hasInternetConnection(): Boolean {
        var result = false

        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }

        return result
    }
}