package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.models.Match
import com.example.data.models.NewsArticle

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val status: String,
    val timeElapsed: String,
    val league: String,
    val homeLogoColor: String,
    val awayLogoColor: String,
    val homeShort: String,
    val awayShort: String,
    val isFavorite: Boolean,
    val matchDate: String,
    val matchTime: String
) {
    fun toDomain(): Match = Match(
        id = id,
        homeTeam = homeTeam,
        awayTeam = awayTeam,
        homeScore = homeScore,
        awayScore = awayScore,
        status = status,
        timeElapsed = timeElapsed,
        league = league,
        homeLogoColor = homeLogoColor,
        awayLogoColor = awayLogoColor,
        homeShort = homeShort,
        awayShort = awayShort,
        isFavorite = isFavorite,
        matchDate = matchDate,
        matchTime = matchTime
    )

    companion object {
        fun fromDomain(match: Match): MatchEntity = MatchEntity(
            id = match.id,
            homeTeam = match.homeTeam,
            awayTeam = match.awayTeam,
            homeScore = match.homeScore,
            awayScore = match.awayScore,
            status = match.status,
            timeElapsed = match.timeElapsed,
            league = match.league,
            homeLogoColor = match.homeLogoColor,
            awayLogoColor = match.awayLogoColor,
            homeShort = match.homeShort,
            awayShort = match.awayShort,
            isFavorite = match.isFavorite,
            matchDate = match.matchDate,
            matchTime = match.matchTime
        )
    }
}

@Entity(tableName = "articles")
data class NewsArticleEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val category: String,
    val content: String,
    val publishTime: String,
    val isBookmarked: Boolean,
    val keyTakeaways: String,
    val isAiGenerated: Boolean
) {
    fun toDomain(): NewsArticle = NewsArticle(
        id = id,
        title = title,
        category = category,
        content = content,
        publishTime = publishTime,
        isBookmarked = isBookmarked,
        keyTakeaways = keyTakeaways,
        isAiGenerated = isAiGenerated
    )

    companion object {
        fun fromDomain(article: NewsArticle): NewsArticleEntity = NewsArticleEntity(
            id = article.id,
            title = article.title,
            category = article.category,
            content = article.content,
            publishTime = article.publishTime,
            isBookmarked = article.isBookmarked,
            keyTakeaways = article.keyTakeaways,
            isAiGenerated = article.isAiGenerated
        )
    }
}
