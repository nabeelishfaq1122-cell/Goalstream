package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FootballDao {
    @Query("SELECT * FROM matches ORDER BY id ASC")
    fun getAllMatchesFlow(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches ORDER BY id ASC")
    suspend fun getAllMatches(): List<MatchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Update
    suspend fun updateMatch(match: MatchEntity)

    @Query("SELECT * FROM articles ORDER BY id DESC")
    fun getAllArticlesFlow(): Flow<List<NewsArticleEntity>>

    @Query("SELECT * FROM articles ORDER BY id DESC")
    suspend fun getAllArticles(): List<NewsArticleEntity>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    suspend fun getArticleById(id: Int): NewsArticleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<NewsArticleEntity>)

    @Update
    suspend fun updateArticle(article: NewsArticleEntity)
}
