package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.local.FootballDatabase
import com.example.data.local.MatchEntity
import com.example.data.local.NewsArticleEntity
import com.example.data.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class GoalStreamViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FootballDatabase.getDatabase(application)
    private val dao = db.footballDao()

    // UI state
    private val _selectedTab = MutableStateFlow("Home") // Home, Matches, News, Account
    val selectedTab: StateFlow<String> = _selectedTab.asStateFlow()

    private val _selectedArticle = MutableStateFlow<NewsArticle?>(null)
    val selectedArticle: StateFlow<NewsArticle?> = _selectedArticle.asStateFlow()

    private val _selectedMatch = MutableStateFlow<Match?>(null)
    val selectedMatch: StateFlow<Match?> = _selectedMatch.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // DB backed flows
    private val _matches = MutableStateFlow<List<Match>>(emptyList())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    private val _articles = MutableStateFlow<List<NewsArticle>>(emptyList())
    val articles: StateFlow<List<NewsArticle>> = _articles.asStateFlow()

    // Favorite teams for personalization
    private val _favoriteTeams = MutableStateFlow<Set<String>>(setOf("Man City", "Real Madrid"))
    val favoriteTeams: StateFlow<Set<String>> = _favoriteTeams.asStateFlow()

    // Timeline Events for Matches
    private val _matchEvents = MutableStateFlow<Map<Int, List<MatchTimelineEvent>>>(emptyMap())
    val matchEvents: StateFlow<Map<Int, List<MatchTimelineEvent>>> = _matchEvents.asStateFlow()

    // AI Chat/Query states
    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _aiResult = MutableStateFlow<String?>(null)
    val aiResult: StateFlow<String?> = _aiResult.asStateFlow()

    // General AI Assistant chat history
    private val _assistantChat = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf("GoalStream AI" to false) // Pair(Message, isUser)
    )
    val assistantChat: StateFlow<List<Pair<String, Boolean>>> = _assistantChat.asStateFlow()

    private val _activeScoreNotification = MutableStateFlow<ScoreNotification?>(null)
    val activeScoreNotification: StateFlow<ScoreNotification?> = _activeScoreNotification.asStateFlow()

    private val _matchHighlightsSummary = MutableStateFlow<String?>(null)
    val matchHighlightsSummary: StateFlow<String?> = _matchHighlightsSummary.asStateFlow()

    private val _selectedLeagueForStats = MutableStateFlow("Premier League")
    val selectedLeagueForStats: StateFlow<String> = _selectedLeagueForStats.asStateFlow()

    fun selectLeagueForStats(league: String) {
        _selectedLeagueForStats.value = league
    }

    fun getScorersForSelectedLeague(league: String): List<PlayerScorer> {
        return when (league) {
            "Premier League" -> listOf(
                PlayerScorer(1, "Erling Haaland", "Man City", "#6B7280", 27, 31, 4),
                PlayerScorer(2, "Cole Palmer", "Chelsea", "#1D4ED8", 22, 34, 9),
                PlayerScorer(3, "Alexander Isak", "Newcastle", "#3F3F46", 21, 30, 5),
                PlayerScorer(4, "Ollie Watkins", "Aston Villa", "#701A75", 19, 37, 0),
                PlayerScorer(5, "Mohamed Salah", "Liverpool", "#DC2626", 18, 32, 5),
                PlayerScorer(6, "Bukayo Saka", "Arsenal", "#EF4444", 16, 35, 6)
            )
            "La Liga" -> listOf(
                PlayerScorer(1, "Artem Dovbyk", "Girona", "#EA580C", 24, 36, 7),
                PlayerScorer(2, "Alexander Sørloth", "Villarreal", "#EAB308", 23, 34, 0),
                PlayerScorer(3, "Jude Bellingham", "Real Madrid", "#FFFFFF", 19, 28, 1),
                PlayerScorer(4, "Robert Lewandowski", "FC Barcelona", "#991B1B", 19, 35, 4),
                PlayerScorer(5, "Ante Budimir", "Osasuna", "#991B1B", 17, 33, 3)
            )
            else -> listOf(
                PlayerScorer(1, "Kylian Mbappé", "Real Madrid", "#FFFFFF", 8, 12, 3),
                PlayerScorer(2, "Harry Kane", "Bayern Munich", "#DC2626", 8, 12, 3),
                PlayerScorer(3, "Erling Haaland", "Man City", "#6B7280", 6, 9, 2),
                PlayerScorer(4, "Antoine Griezmann", "Atletico Madrid", "#DC2626", 6, 10, 1),
                PlayerScorer(5, "Vinicius Jr.", "Real Madrid", "#FFFFFF", 6, 10, 0)
            )
        }
    }

    fun getAssistersForSelectedLeague(league: String): List<PlayerAssister> {
        return when (league) {
            "Premier League" -> listOf(
                PlayerAssister(1, "Ollie Watkins", "Aston Villa", "#701A75", 13, 37),
                PlayerAssister(2, "Cole Palmer", "Chelsea", "#1D4ED8", 11, 34),
                PlayerAssister(3, "Kevin De Bruyne", "Man City", "#6B7280", 10, 18),
                PlayerAssister(4, "Mohamed Salah", "Liverpool", "#DC2626", 10, 32),
                PlayerAssister(5, "Martin Ødegaard", "Arsenal", "#EF4444", 10, 35),
                PlayerAssister(6, "Bukayo Saka", "Arsenal", "#EF4444", 9, 35)
            )
            "La Liga" -> listOf(
                PlayerAssister(1, "Alex Baena", "Villarreal", "#EAB308", 14, 34),
                PlayerAssister(2, "Nico Williams", "Athletic Club", "#EF4444", 11, 31),
                PlayerAssister(3, "Sávio", "Girona", "#EA580C", 10, 37),
                PlayerAssister(4, "İlkay Gündoğan", "FC Barcelona", "#991B1B", 9, 36),
                PlayerAssister(5, "Robert Lewandowski", "FC Barcelona", "#991B1B", 8, 35)
            )
            else -> listOf(
                PlayerAssister(1, "Marcel Sabitzer", "Dortmund", "#EAB308", 5, 12),
                PlayerAssister(2, "Vinicius Jr.", "Real Madrid", "#FFFFFF", 5, 10),
                PlayerAssister(3, "Jude Bellingham", "Real Madrid", "#FFFFFF", 5, 11),
                PlayerAssister(4, "Harry Kane", "Bayern Munich", "#DC2626", 4, 12),
                PlayerAssister(5, "İlkay Gündoğan", "FC Barcelona", "#991B1B", 4, 10)
            )
        }
    }

    fun getCleanSheetsForSelectedLeague(league: String): List<GoalkeeperCleanSheet> {
        return when (league) {
            "Premier League" -> listOf(
                GoalkeeperCleanSheet(1, "David Raya", "Arsenal", "#EF4444", 16, 32),
                GoalkeeperCleanSheet(2, "Jordan Pickford", "Everton", "#1D4ED8", 13, 38),
                GoalkeeperCleanSheet(3, "Bernd Leno", "Fulham", "#3F3F46", 10, 38),
                GoalkeeperCleanSheet(4, "Ederson", "Man City", "#6B7280", 10, 33),
                GoalkeeperCleanSheet(5, "André Onana", "Man United", "#DA291C", 9, 38)
            )
            "La Liga" -> listOf(
                GoalkeeperCleanSheet(1, "Unai Simón", "Athletic Club", "#EF4444", 16, 36),
                GoalkeeperCleanSheet(2, "Marc-André ter Stegen", "FC Barcelona", "#991B1B", 15, 28),
                GoalkeeperCleanSheet(3, "Alex Remiro", "Real Sociedad", "#1D4ED8", 15, 37),
                GoalkeeperCleanSheet(4, "Giorgi Mamardashvili", "Valencia", "#FFFFFF", 13, 37),
                GoalkeeperCleanSheet(5, "Jan Oblak", "Atletico Madrid", "#DC2626", 13, 38)
            )
            else -> listOf(
                GoalkeeperCleanSheet(1, "Gregor Kobel", "Dortmund", "#EAB308", 6, 12),
                GoalkeeperCleanSheet(2, "Alex Remiro", "Real Sociedad", "#1D4ED8", 4, 8),
                GoalkeeperCleanSheet(3, "Yann Sommer", "Inter Milan", "#1D4ED8", 4, 7),
                GoalkeeperCleanSheet(4, "Manuel Neuer", "Bayern Munich", "#DC2626", 4, 9),
                GoalkeeperCleanSheet(5, "Andriy Lunin", "Real Madrid", "#FFFFFF", 4, 8)
            )
        }
    }


    init {
        // Pre-populate data if DB is empty, then launch Flow collection
        viewModelScope.launch {
            val existingMatches = dao.getAllMatches()
            if (existingMatches.isEmpty()) {
                prepopulateDatabase()
            }
            
            // Gather initial timeline events
            initializeTimelineEvents()

            // Collect database matches
            launch {
                dao.getAllMatchesFlow().collect { entities ->
                    val nextMatches = entities.map { it.toDomain() }
                    val prevMatches = _matches.value
                    if (prevMatches.isNotEmpty()) {
                        for (next in nextMatches) {
                            val prev = prevMatches.find { it.id == next.id }
                            if (prev != null) {
                                if (next.homeScore > prev.homeScore) {
                                    triggerScoreUpdateNotification(next, next.homeTeam)
                                } else if (next.awayScore > prev.awayScore) {
                                    triggerScoreUpdateNotification(next, next.awayTeam)
                                }
                            }
                        }
                    }
                    _matches.value = nextMatches
                }
            }

            // Collect database articles
            launch {
                dao.getAllArticlesFlow().collect { entities ->
                    _articles.value = entities.map { it.toDomain() }
                }
            }

            // Launch active Live match updates simulation ticker!
            launch {
                liveMatchSimulationTicker()
            }
        }
    }

    fun selectTab(tab: String) {
        _selectedTab.value = tab
        // Clear full detail screens when jumping tabs
        _selectedArticle.value = null
        _selectedMatch.value = null
        _aiResult.value = null
    }

    fun selectArticle(article: NewsArticle?) {
        _selectedArticle.value = article
        _aiResult.value = null
    }

    fun selectMatch(match: Match?) {
        _selectedMatch.value = match
        _aiResult.value = null
        _matchHighlightsSummary.value = null
    }


    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Toggle Favorite Match status
    fun toggleMatchFavorite(matchId: Int) {
        viewModelScope.launch {
            val matchEntity = matches.value.find { it.id == matchId } ?: return@launch
            val updated = matchEntity.copy(isFavorite = !matchEntity.isFavorite)
            dao.updateMatch(MatchEntity.fromDomain(updated))
        }
    }

    // Toggle News bookmark
    fun toggleArticleBookmark(articleId: Int) {
        viewModelScope.launch {
            val articleEntity = articles.value.find { it.id == articleId } ?: return@launch
            val updated = articleEntity.copy(isBookmarked = !articleEntity.isBookmarked)
            dao.updateArticle(NewsArticleEntity.fromDomain(updated))
            // Also update selectedArticle state if viewing it
            if (_selectedArticle.value?.id == articleId) {
                _selectedArticle.value = updated
            }
        }
    }

    // Customize personalized club favorites
    fun toggleTeamFavorite(teamName: String) {
        val current = _favoriteTeams.value.toMutableSet()
        if (current.contains(teamName)) {
            current.remove(teamName)
        } else {
            current.add(teamName)
        }
        _favoriteTeams.value = current
    }

    fun clearScoreNotification() {
        _activeScoreNotification.value = null
    }

    private fun triggerScoreUpdateNotification(match: Match, scoringTeam: String) {
        viewModelScope.launch {
            val notification = ScoreNotification(
                matchId = match.id,
                league = match.league,
                scoringTeam = scoringTeam,
                homeTeam = match.homeTeam,
                awayTeam = match.awayTeam,
                homeScore = match.homeScore,
                awayScore = match.awayScore,
                timeElapsed = match.timeElapsed
            )
            _activeScoreNotification.value = notification
            // Auto-dismiss after 6 seconds
            delay(6000)
            if (_activeScoreNotification.value?.id == notification.id) {
                _activeScoreNotification.value = null
            }
        }
    }

    // AI Prediction for Match
    fun askAiPrediction(match: Match) {
        _aiLoading.value = true
        _aiResult.value = null
        viewModelScope.launch {
            val prompt = "Provide a professional, concise, tactical match analysis and score prediction for the football fixture: ${match.homeTeam} vs ${match.awayTeam} in the ${match.league}. Current score is ${match.homeScore}-${match.awayScore} in the ${match.timeElapsed} minute."
            val response = GeminiApiClient.generateContent(prompt)
            _aiResult.value = response
            _aiLoading.value = false
        }
    }

    // AI Match Highlights Summary
    fun askMatchHighlightsSummary(match: Match) {
        _aiLoading.value = true
        _matchHighlightsSummary.value = null
        viewModelScope.launch {
            val events = _matchEvents.value[match.id] ?: emptyList()
            val eventsStr = if (events.isEmpty()) {
                "No timeline events recorded."
            } else {
                events.joinToString("; ") { "${it.minute}: ${it.type} for ${it.team} by ${it.detail} (${it.description})" }
            }
            val prompt = "Analyze this finished football match: ${match.homeTeam} vs ${match.awayTeam} in the ${match.league}. " +
                    "Final score was ${match.homeScore}-${match.awayScore}. " +
                    "Timeline events: $eventsStr. " +
                    "Provide a highly engaging, concise, professional 3-sentence summary of the match highlights, detailing the turning points and overall narrative of the game. Use modern tone."
            val response = GeminiApiClient.generateContent(prompt)
            _matchHighlightsSummary.value = response
            _aiLoading.value = false
        }
    }


    // AI Summarization of Article
    fun askAiTakeaways(article: NewsArticle) {
        _aiLoading.value = true
        _aiResult.value = null
        viewModelScope.launch {
            val prompt = "Based on this football news article: '${article.title}', content: '${article.content}'. Provide exactly 4 concise, high-impact key takeaways. Structure them as bullet points with short explanations."
            val response = GeminiApiClient.generateContent(prompt)
            _aiResult.value = response
            _aiLoading.value = false
        }
    }

    // AI Assistant Chatbot Question
    fun askAssistantQuestion(question: String) {
        if (question.isBlank()) return
        
        val currentChat = _assistantChat.value.toMutableList()
        currentChat.add(question to true)
        _assistantChat.value = currentChat
        
        _aiLoading.value = true
        viewModelScope.launch {
            val prompt = "You are GoalStream AI, an expert sports journalist and football tactical analyst assistant. Provide a highly engaging, informative, and authoritative answer to this football question: '$question'."
            val response = GeminiApiClient.generateContent(prompt)
            
            val updatedChat = _assistantChat.value.toMutableList()
            updatedChat.add(response to false)
            _assistantChat.value = updatedChat
            _aiLoading.value = false
        }
    }

    fun clearChat() {
        _assistantChat.value = listOf("Hello! I'm your GoalStream AI Assistant. Ask me anything about matches, transfers, tactics, or leagues!" to false)
    }

    // Simulated Event Generator to make the live matches tick dynamically in real-time
    private suspend fun liveMatchSimulationTicker() {
        while (true) {
            delay(10000) // Ticks every 10 seconds for testing/preview excitement
            val liveMatches = matches.value.filter { it.status == "LIVE" }
            if (liveMatches.isEmpty()) continue

            // Randomly update time elapsed
            val randomMatch = liveMatches.random()
            val currentMinsStr = randomMatch.timeElapsed.replace("'", "")
            val currentMins = currentMinsStr.toIntOrNull() ?: 72
            
            if (currentMins >= 90) {
                // End the match
                val updated = randomMatch.copy(status = "FINISHED", timeElapsed = "FT")
                dao.updateMatch(MatchEntity.fromDomain(updated))
                continue
            }

            val nextMins = currentMins + 1
            var updatedScoreMatch = randomMatch.copy(timeElapsed = "$nextMins'")

            // 20% chance of a goal or card in a tick
            if (Random.nextInt(100) < 20) {
                val isHome = Random.nextBoolean()
                val eventType = if (Random.nextInt(100) < 60) "GOAL" else "CARD_YELLOW"
                
                if (eventType == "GOAL") {
                    updatedScoreMatch = if (isHome) {
                        updatedScoreMatch.copy(homeScore = updatedScoreMatch.homeScore + 1)
                    } else {
                        updatedScoreMatch.copy(awayScore = updatedScoreMatch.awayScore + 1)
                    }
                    
                    // Add event
                    val event = MatchTimelineEvent(
                        id = Random.nextInt(10000, 99999),
                        matchId = updatedScoreMatch.id,
                        minute = "$nextMins'",
                        type = "GOAL",
                        team = if (isHome) "HOME" else "AWAY",
                        detail = if (isHome) getPlayerForTeam(updatedScoreMatch.homeShort) else getPlayerForTeam(updatedScoreMatch.awayShort),
                        description = "Goal scored! Fantastic effort!"
                    )
                    appendTimelineEvent(updatedScoreMatch.id, event)
                } else {
                    val event = MatchTimelineEvent(
                        id = Random.nextInt(10000, 99999),
                        matchId = updatedScoreMatch.id,
                        minute = "$nextMins'",
                        type = "CARD_YELLOW",
                        team = if (isHome) "HOME" else "AWAY",
                        detail = if (isHome) getPlayerForTeam(updatedScoreMatch.homeShort) else getPlayerForTeam(updatedScoreMatch.awayShort),
                        description = "Yellow card for professional foul."
                    )
                    appendTimelineEvent(updatedScoreMatch.id, event)
                }
            }

            dao.updateMatch(MatchEntity.fromDomain(updatedScoreMatch))
            // Also update selectedMatch if the user has it open
            if (_selectedMatch.value?.id == updatedScoreMatch.id) {
                _selectedMatch.value = updatedScoreMatch
            }
        }
    }

    private fun getPlayerForTeam(shortName: String): String {
        return when (shortName) {
            "MCI" -> listOf("E. Haaland", "K. De Bruyne", "Phil Foden", "Bernardo Silva", "Ronaldo").random()
            "ARS" -> listOf("Bukayo Saka", "Martin Odegaard", "Declan Rice", "Gabriel Martinelli", "Kai Havertz").random()
            "RMA" -> listOf("Vinicius Jr.", "Jude Bellingham", "Kylian Mbappé", "Rodrygo", "Luka Modric").random()
            "FCB" -> listOf("Robert Lewandowski", "Lamine Yamal", "Raphinha", "Pedri", "Gavi").random()
            "LIV" -> listOf("Mohamed Salah", "Luis Diaz", "Darwin Nunez", "Alexis Mac Allister", "Virgil van Dijk").random()
            "CHE" -> listOf("Cole Palmer", "Nicolas Jackson", "Noni Madueke", "Enzo Fernandez").random()
            else -> "Player X"
        }
    }

    private fun appendTimelineEvent(matchId: Int, event: MatchTimelineEvent) {
        val current = _matchEvents.value.toMutableMap()
        val list = (current[matchId] ?: emptyList()).toMutableList()
        list.add(0, event) // Add to top
        current[matchId] = list
        _matchEvents.value = current
    }

    private fun initializeTimelineEvents() {
        val initialEvents = mapOf(
            101 to listOf(
                MatchTimelineEvent(1, 101, "34'", "GOAL", "HOME", "E. Haaland", "Spectacular header. Assist by K. De Bruyne"),
                MatchTimelineEvent(2, 101, "55'", "CARD_YELLOW", "AWAY", "Declan Rice", "Tactical pull back on Foden"),
                MatchTimelineEvent(3, 101, "62'", "GOAL", "AWAY", "Bukayo Saka", "Curled shot into bottom corner"),
                MatchTimelineEvent(4, 101, "68'", "GOAL", "HOME", "Phil Foden", "Stunning volley from outside the box")
            ),
            102 to listOf(
                MatchTimelineEvent(5, 102, "12'", "CARD_YELLOW", "HOME", "Vinicius Jr.", "Dissent toward referee")
            ),
            104 to listOf(
                MatchTimelineEvent(6, 104, "15'", "GOAL", "AWAY", "L. Martinez", "Clinical finish under the keeper"),
                MatchTimelineEvent(7, 104, "42'", "GOAL", "HOME", "Rafael Leao", "Slick solo run and side foot finish"),
                MatchTimelineEvent(8, 104, "70'", "GOAL", "AWAY", "H. Mkhitaryan", "Rebound strike after a corner"),
                MatchTimelineEvent(9, 104, "85'", "GOAL", "HOME", "Olivier Giroud", "Towering header from a crossing flank")
            )
        )
        _matchEvents.value = initialEvents
    }

    private suspend fun prepopulateDatabase() {
        // Build initial pre-populated matches matching the Professional Polish HTML structure and details
        val initialMatches = listOf(
            MatchEntity(
                id = 101,
                homeTeam = "Man City",
                awayTeam = "Arsenal",
                homeScore = 2,
                awayScore = 1,
                status = "LIVE",
                timeElapsed = "72'",
                league = "Premier League",
                homeLogoColor = "#6B7280", // Slate grey
                awayLogoColor = "#EF4444", // Red
                homeShort = "MCI",
                awayShort = "ARS",
                isFavorite = false,
                matchDate = "Today",
                matchTime = "12:30"
            ),
            MatchEntity(
                id = 102,
                homeTeam = "Real Madrid",
                awayTeam = "FC Barcelona",
                homeScore = 0,
                awayScore = 0,
                status = "LIVE",
                timeElapsed = "45'",
                league = "La Liga",
                homeLogoColor = "#FFFFFF", // White
                awayLogoColor = "#991B1B", // Deep maroon
                homeShort = "RMA",
                awayShort = "FCB",
                isFavorite = true,
                matchDate = "Today",
                matchTime = "14:45"
            ),
            MatchEntity(
                id = 103,
                homeTeam = "Liverpool",
                awayTeam = "Chelsea",
                homeScore = 0,
                awayScore = 0,
                status = "UPCOMING",
                timeElapsed = "0'",
                league = "Premier League",
                homeLogoColor = "#DC2626", // Liverpool Red
                awayLogoColor = "#1D4ED8", // Chelsea Blue
                homeShort = "LIV",
                awayShort = "CHE",
                isFavorite = false,
                matchDate = "Today",
                matchTime = "19:30"
            ),
            MatchEntity(
                id = 104,
                homeTeam = "AC Milan",
                awayTeam = "Inter Milan",
                homeScore = 2,
                awayScore = 2,
                status = "FINISHED",
                timeElapsed = "FT",
                league = "Serie A",
                homeLogoColor = "#B91C1C", // Milan Red
                awayLogoColor = "#1E3A8A", // Inter Blue
                homeShort = "MIL",
                awayShort = "INT",
                isFavorite = false,
                matchDate = "Yesterday",
                matchTime = "18:00"
            ),
            MatchEntity(
                id = 105,
                homeTeam = "Bayern Munich",
                awayTeam = "Dortmund",
                homeScore = 0,
                awayScore = 0,
                status = "UPCOMING",
                timeElapsed = "0'",
                league = "Bundesliga",
                homeLogoColor = "#EF4444", // Bayern Red
                awayLogoColor = "#FBBF24", // Dortmund Yellow
                homeShort = "FCB",
                awayShort = "BVB",
                isFavorite = false,
                matchDate = "Tomorrow",
                matchTime = "15:30"
            )
        )
        dao.insertMatches(initialMatches)

        // Build initial pre-populated articles matching the design theme exactly
        val initialArticles = listOf(
            NewsArticleEntity(
                id = 1,
                title = "Bellingham eyes record-breaking move to Madrid this summer",
                category = "Transfers",
                content = "Jude Bellingham is reportedly in advanced discussions for a potential blockbuster transfer to Real Madrid. Insiders suggest that personal terms are nearly agreed upon, with a record fee of over €110 million being discussed. The English prodigy has been stellar for his club, and his dynamic midfield progressive carries are seen as the perfect fit for the Spanish giants. The deal would reshape the European football landscape, marking Madrid's most ambitious signing since Hazard.",
                publishTime = "2 hours ago",
                isBookmarked = false,
                keyTakeaways = "Record €110m fee under discussion;Personal terms nearly finalized;Designed to anchor Madrid's future midfield;Re-defines summer transfer market dynamics",
                isAiGenerated = false
            ),
            NewsArticleEntity(
                id = 2,
                title = "Quarter-final draw results: Real Madrid vs Man City rematch",
                category = "Champions League",
                content = "The UEFA Champions League quarter-final draw has produced a mouthwatering encounter as holders Manchester City prepare to face Real Madrid. This rematch of last year's thrilling semi-final is widely regarded as the 'final before the final'. Carlo Ancelotti and Pep Guardiola will lock tactical horns yet again in what promises to be an epic battle of possession control versus quick vertical transitions.",
                publishTime = "4 hours ago",
                isBookmarked = false,
                keyTakeaways = "Rematch of last year's semi-final;Pep vs Ancelotti tactical showdown;Two tournament favorites meeting early",
                isAiGenerated = false
            ),
            NewsArticleEntity(
                id = 3,
                title = "How Klopp's final season is redefining the Liverpool midfield",
                category = "Tactical Analysis",
                content = "Jurgen Klopp's swan song at Anfield has witnessed a fascinating tactical evolution. The midfield, once characterized by heavy-metal pressing workhorses, has transitioned into a more fluid, technically creative hub. With key positional rotations and inverted wing-backs overloading the half-spaces, Liverpool is choke-holding opponents in transition, keeping their title charge alive.",
                publishTime = "1 day ago",
                isBookmarked = true,
                keyTakeaways = "Fluid midfield rotations replace rigid workhorse lines;Inverted wing-backs overloading half-spaces;Elite counter-pressing index maintained",
                isAiGenerated = false
            ),
            NewsArticleEntity(
                id = 4,
                title = "Arsenal's defensive solidity is the key to their title ambitions",
                category = "Tactical Analysis",
                content = "While Manchester City dominates the headlines, Arsenal's outstanding defensive record is quiet but deadly. The pairing of Saliba and Gabriel has formed a brick wall, conceding the fewest expected goals (xG) in the league. By constricting central spaces and dominating aerial duels, Arteta has built a robust structure capable of winning the tightest of championship races.",
                publishTime = "2 days ago",
                isBookmarked = false,
                keyTakeaways = "Saliba-Gabriel partnership forms league's best defense;Arteta's tactical structure minimizes central exposure;Conceded fewest expected goals (xG) in the division",
                isAiGenerated = false
            )
        )
        dao.insertArticles(initialArticles)
    }
}
