package com.example.data.models

data class Match(
    val id: Int,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val status: String, // "LIVE", "UPCOMING", "FINISHED"
    val timeElapsed: String, // e.g. "72'"
    val league: String, // "Premier League", "La Liga", "Champions League"
    val homeLogoColor: String, // Hex color for custom badge, e.g. "#6B7280"
    val awayLogoColor: String, // Hex color for custom badge, e.g. "#EF4444"
    val homeShort: String, // e.g. "MCI"
    val awayShort: String, // e.g. "ARS"
    val isFavorite: Boolean = false,
    val matchDate: String, // e.g. "Today", "Tomorrow", "June 28"
    val matchTime: String // e.g. "15:00"
)

data class NewsArticle(
    val id: Int,
    val title: String,
    val category: String, // "Transfers", "Tactical Analysis", "Champions League"
    val content: String,
    val publishTime: String,
    val isBookmarked: Boolean = false,
    val keyTakeaways: String = "", // Semicolon separated list
    val isAiGenerated: Boolean = false
)

data class MatchTimelineEvent(
    val id: Int,
    val matchId: Int,
    val minute: String, // e.g. "34'"
    val type: String, // "GOAL", "CARD_YELLOW", "CARD_RED", "SUB"
    val team: String, // "HOME", "AWAY"
    val detail: String, // e.g. "Erling Haaland"
    val description: String // e.g. "Goal! Assist by Kevin De Bruyne"
)

data class ScoreNotification(
    val id: Long = System.currentTimeMillis(),
    val matchId: Int,
    val league: String,
    val scoringTeam: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val timeElapsed: String
)

data class PlayerScorer(
    val rank: Int,
    val name: String,
    val team: String,
    val teamColor: String,
    val goals: Int,
    val matchesPlayed: Int,
    val penalties: Int
)

data class PlayerAssister(
    val rank: Int,
    val name: String,
    val team: String,
    val teamColor: String,
    val assists: Int,
    val matchesPlayed: Int
)

data class GoalkeeperCleanSheet(
    val rank: Int,
    val name: String,
    val team: String,
    val teamColor: String,
    val cleanSheets: Int,
    val matchesPlayed: Int
)


