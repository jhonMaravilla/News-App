package com.kotlin.newsapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kotlin.newsapp.model.Article
import com.kotlin.newsapp.util.Helper
import com.kotlin.newsapp.util.Helper.Companion.DATABASE_NAME

@Database(
    entities = [Article::class], version = 1
)
@TypeConverters(Converters::class)
abstract class ArticleDatabase : RoomDatabase() {

    abstract fun getArticleDao(): ArticleDao

    companion object {
        // Other threads can immidiately see if a thread changed our instance object
        @Volatile
        private var instance: ArticleDatabase? = null

        // Synchronize
        private val LOCK = Any()

        // operator function is called whenever we create an instance of our database or call the ArticleDatabase()
        // If the instance is null, we call the synchronized function which prevents other threads from accessing the code inside it
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also {
                instance = it
            }
        }

        private fun createDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            ArticleDatabase::class.java,
            DATABASE_NAME
        ).build()

    }
}