package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.ui.theme.*
import com.example.ui.viewmodels.GoalStreamViewModel
import kotlinx.coroutines.delay

@Composable
fun GoalStreamApp(viewModel: GoalStreamViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val selectedArticle by viewModel.selectedArticle.collectAsState()
    val selectedMatch by viewModel.selectedMatch.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val activeNotification by viewModel.activeScoreNotification.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                if (selectedArticle == null && selectedMatch == null) {
                    GoalStreamHeader(
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onProfileClick = { viewModel.selectTab("Account") }
                    )
                }
            },
            bottomBar = {
                GoalStreamBottomNavigation(
                    selectedTab = selectedTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    selectedArticle != null -> {
                        ArticleDetailScreen(
                            article = selectedArticle!!,
                            viewModel = viewModel,
                            onBack = { viewModel.selectArticle(null) }
                        )
                    }
                    selectedMatch != null -> {
                        MatchDetailScreen(
                            match = selectedMatch!!,
                            viewModel = viewModel,
                            onBack = { viewModel.selectMatch(null) }
                        )
                    }
                    searchQuery.isNotBlank() -> {
                        GlobalSearchScreen(
                            searchQuery = searchQuery,
                            viewModel = viewModel
                        )
                    }
                    else -> {
                        when (selectedTab) {
                            "Home" -> HomeScreen(viewModel = viewModel)
                            "Matches" -> MatchesScreen(viewModel = viewModel)
                            "Stats" -> StatsScreen(viewModel = viewModel)
                            "News" -> NewsScreen(viewModel = viewModel)
                            "Account" -> AccountScreen(viewModel = viewModel)
                        }
                    }
                }

                // OVERLAY PULSING NOTIFICATION ON SCORE UPDATE
                AnimatedVisibility(
                    visible = activeNotification != null,
                    enter = androidx.compose.animation.slideInVertically(
                        initialOffsetY = { -it }
                    ) + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.slideOutVertically(
                        targetOffsetY = { -it }
                    ) + androidx.compose.animation.fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .zIndex(99f)
                ) {
                    activeNotification?.let { notification ->
                        PulsingScoreUpdateNotification(
                            notification = notification,
                            onDismiss = { viewModel.clearScoreNotification() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GoalStreamHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    var isSearchActive by remember(searchQuery) { mutableStateOf(searchQuery.isNotEmpty()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(GoalStreamBackground)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (isSearchActive) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    isSearchActive = false
                    onSearchChange("")
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = GoalStreamTextMain
                    )
                }
                TextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search players, teams, leagues...", color = GoalStreamTextMuted) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("header_search_input"),
                    singleLine = true,
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = GoalStreamTextMain
                                )
                            }
                        }
                    }
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = GoalStreamTextMain,
                    modifier = Modifier.clickable { /* Side drawer menu trigger placeholder */ }
                )
                Text(
                    text = "GoalStream",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = GoalStreamTextMain,
                    letterSpacing = (-0.5).sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = { isSearchActive = true }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = GoalStreamTextMain
                    )
                }
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GoalStreamSecondary)
                        .border(1.dp, GoalStreamBorder, CircleShape)
                        .clickable { onProfileClick() }
                        .testTag("profile_badge"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamDarkBlue
                    )
                }
            }
        }
    }
}

@Composable
fun GoalStreamBottomNavigation(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Column {
        HorizontalDivider(color = GoalStreamBorder, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(GoalStreamSurface)
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val navItems = listOf(
                NavigationItem("Home", Icons.Default.Home, "home_tab"),
                NavigationItem("Matches", Icons.Default.PlayArrow, "matches_tab"),
                NavigationItem("Stats", Icons.Default.Star, "stats_tab"),
                NavigationItem("News", Icons.Default.Share, "news_tab"),
                NavigationItem("Account", Icons.Default.Person, "account_tab")
            )

            navItems.forEach { item ->
                val isSelected = selectedTab == item.title
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(item.title) }
                        .testTag(item.testTag),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) GoalStreamSecondary else Color.Transparent)
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (isSelected) GoalStreamDarkBlue else GoalStreamTextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) GoalStreamTextMain else GoalStreamTextMuted
                    )
                }
            }
        }
    }
}

data class NavigationItem(val title: String, val icon: ImageVector, val testTag: String)

// --- HOME SCREEN ---
@Composable
fun HomeScreen(viewModel: GoalStreamViewModel) {
    val matches by viewModel.matches.collectAsState()
    val articles by viewModel.articles.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val filteredMatches = remember(searchQuery, matches) {
        if (searchQuery.isBlank()) matches
        else matches.filter {
            it.homeTeam.contains(searchQuery, ignoreCase = true) ||
                    it.awayTeam.contains(searchQuery, ignoreCase = true) ||
                    it.league.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredArticles = remember(searchQuery, articles) {
        if (searchQuery.isBlank()) articles
        else articles.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GoalStreamBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (searchQuery.isNotBlank()) {
            item {
                Text(
                    text = "Search Results",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoalStreamTextMuted,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            if (filteredMatches.isNotEmpty()) {
                item {
                    Text(
                        text = "Matches",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamPrimary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(filteredMatches) { match ->
                    MatchRowItem(match = match, onClick = { viewModel.selectMatch(match) })
                }
            }
            if (filteredArticles.isNotEmpty()) {
                item {
                    Text(
                        text = "Articles",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamPrimary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }
                items(filteredArticles) { article ->
                    NewsRowItem(article = article, onClick = { viewModel.selectArticle(article) })
                }
            }
            if (filteredMatches.isEmpty() && filteredArticles.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matches or articles found for '$searchQuery'",
                            color = GoalStreamTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Live Matches Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE MATCHES",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamTextMuted,
                        letterSpacing = 1.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        LivePulseIndicator()
                        Text(
                            text = "LIVE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoalStreamAccentRed
                        )
                    }
                }
            }

            // Live Match horizontal row
            item {
                val liveMatches = matches.filter { it.status == "LIVE" }
                if (liveMatches.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(GoalStreamSurface)
                            .border(BorderStroke(1.dp, GoalStreamBorder), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No matches live right now.\nTap 'Matches' to view upcoming fixtures.",
                            color = GoalStreamTextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(liveMatches) { match ->
                            LiveMatchCard(match = match, onClick = { viewModel.selectMatch(match) })
                        }
                    }
                }
            }

            // Trending Story Header
            item {
                Text(
                    text = "TOP STORIES",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoalStreamTextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Featured Article Hero Card
            val featuredArticle = articles.firstOrNull()
            if (featuredArticle != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xFF2D3033))
                            .clickable { viewModel.selectArticle(featuredArticle) }
                            .testTag("featured_article_card")
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF1F2937), Color(0xFF111827))
                                )
                            )
                            drawCircle(
                                color = Color(0x0F10B981),
                                radius = size.minDimension * 0.7f,
                                center = androidx.compose.ui.geometry.Offset(size.width, size.height)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GoalStreamDeepBlue)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = featuredArticle.category.uppercase(),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = featuredArticle.title,
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 22.sp
                                )
                                Text(
                                    text = "GoalStream Intelligence • ${featuredArticle.publishTime}",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // AdMob Banner Ad
            item {
                AdMobBanner()
            }

            // Other Articles List
            val otherArticles = if (articles.isNotEmpty()) articles.drop(1) else emptyList()
            if (otherArticles.isNotEmpty()) {
                items(otherArticles) { article ->
                    NewsRowItem(article = article, onClick = { viewModel.selectArticle(article) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun LivePulseIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier.size(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = GoalStreamAccentRed.copy(alpha = 0.25f),
                radius = size.minDimension / 2 * (1f + scale)
            )
            drawCircle(
                color = GoalStreamAccentRed,
                radius = size.minDimension / 4
            )
        }
    }
}

@Composable
fun LiveMatchCard(match: Match, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(224.dp)
            .padding(vertical = 4.dp)
            .clickable { onClick() }
            .testTag("live_match_card_${match.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (match.id == 101) GoalStreamSurface else GoalStreamBackground
        ),
        border = BorderStroke(1.dp, GoalStreamBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (match.id == 101) GoalStreamSecondary else GoalStreamSurface)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = match.league,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamTextMuted
                    )
                }

                Text(
                    text = match.timeElapsed,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoalStreamAccentRed
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Home
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TeamColorBadge(colorHex = match.homeLogoColor, abbreviation = match.homeShort)
                        Text(
                            text = match.homeTeam,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = GoalStreamTextMain
                        )
                    }
                    Text(
                        text = match.homeScore.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamTextMain
                    )
                }

                // Away
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TeamColorBadge(colorHex = match.awayLogoColor, abbreviation = match.awayShort)
                        Text(
                            text = match.awayTeam,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = GoalStreamTextMain
                        )
                    }
                    Text(
                        text = match.awayScore.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamTextMain
                    )
                }
            }
        }
    }
}

@Composable
fun TeamColorBadge(colorHex: String, abbreviation: String) {
    val parsedColor = remember(colorHex) {
        try {
            Color(android.graphics.Color.parseColor(colorHex))
        } catch (e: Exception) {
            Color.Gray
        }
    }

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(parsedColor)
            .border(1.dp, GoalStreamBorder.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = abbreviation.take(1),
            color = if (parsedColor == Color.White) Color.Black else Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun NewsRowItem(article: NewsArticle, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("article_row_${article.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
        border = BorderStroke(1.dp, GoalStreamBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GoalStreamPrimary, GoalStreamDeepBlue)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "News icon",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = article.category,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoalStreamPrimary
                )
                Text(
                    text = article.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GoalStreamTextMain,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Text(
                    text = article.publishTime,
                    fontSize = 11.sp,
                    color = GoalStreamTextMuted
                )
            }
        }
    }
}

@Composable
fun MatchRowItem(match: Match, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("match_row_${match.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
        border = BorderStroke(1.dp, GoalStreamBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TeamColorBadge(colorHex = match.homeLogoColor, abbreviation = match.homeShort)
                    Text(
                        text = match.homeTeam,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = GoalStreamTextMain
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TeamColorBadge(colorHex = match.awayLogoColor, abbreviation = match.awayShort)
                    Text(
                        text = match.awayTeam,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = GoalStreamTextMain
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (match.status == "LIVE" || match.status == "FINISHED") {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${match.homeScore} - ${match.awayScore}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (match.status == "LIVE") GoalStreamAccentRed else GoalStreamTextMain
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (match.status == "LIVE") GoalStreamAccentRed.copy(alpha = 0.1f) else GoalStreamBorder.copy(alpha = 0.3f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (match.status == "LIVE") match.timeElapsed else "FT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (match.status == "LIVE") GoalStreamAccentRed else GoalStreamTextMuted
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoalStreamSecondary)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = match.matchTime,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoalStreamDarkBlue
                        )
                    }
                    Text(
                        text = match.matchDate,
                        fontSize = 11.sp,
                        color = GoalStreamTextMuted
                    )
                }
            }
        }
    }
}

// --- MATCHES SCREEN ---
@Composable
fun MatchesScreen(viewModel: GoalStreamViewModel) {
    val matches by viewModel.matches.collectAsState()
    val favoriteTeams by viewModel.favoriteTeams.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }

    val filteredMatches = remember(selectedFilter, matches, favoriteTeams) {
        when (selectedFilter) {
            "Live" -> matches.filter { it.status == "LIVE" }
            "Favorites" -> matches.filter {
                it.isFavorite || favoriteTeams.contains(it.homeTeam) || favoriteTeams.contains(it.awayTeam)
            }
            else -> matches
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GoalStreamBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("All", "Live", "Favorites")
            filters.forEach { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = {
                        Text(
                            text = if (filter == "Favorites") "★ Fav Teams" else filter,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoalStreamSecondary,
                        selectedLabelColor = GoalStreamDarkBlue,
                        containerColor = GoalStreamSurface,
                        labelColor = GoalStreamTextMuted
                    ),
                    modifier = Modifier.testTag("matches_filter_$filter")
                )
            }
        }

        if (filteredMatches.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (selectedFilter) {
                        "Live" -> "No live fixtures currently progressing."
                        "Favorites" -> "No favorites matched.\nGo to Account to choose your Favorite Teams!"
                        else -> "No matches available."
                    },
                    color = GoalStreamTextMuted,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }
        } else {
            val matchesByLeague = remember(filteredMatches) {
                filteredMatches.groupBy { it.league }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                matchesByLeague.forEach { (league, leagueMatches) ->
                    item {
                        Text(
                            text = league.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoalStreamTextMuted,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                    }
                    items(leagueMatches) { match ->
                        MatchRowItem(match = match, onClick = { viewModel.selectMatch(match) })
                    }
                }
                
                // AdMob Banner Ad
                item {
                    AdMobBanner()
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// --- NEWS SCREEN ---
@Composable
fun NewsScreen(viewModel: GoalStreamViewModel) {
    val articles by viewModel.articles.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Transfers", "Champions League", "Tactical Analysis")

    val filteredArticles = remember(selectedCategory, articles) {
        if (selectedCategory == "All") articles
        else articles.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GoalStreamBackground)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = selectedCategory == category
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = category },
                    label = {
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoalStreamSecondary,
                        selectedLabelColor = GoalStreamDarkBlue,
                        containerColor = GoalStreamSurface,
                        labelColor = GoalStreamTextMuted
                    ),
                    modifier = Modifier.testTag("news_category_$category")
                )
            }
        }

        if (filteredArticles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No articles found in this category.",
                    color = GoalStreamTextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredArticles) { article ->
                    NewsRowItem(article = article, onClick = { viewModel.selectArticle(article) })
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// --- ACCOUNT SCREEN ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountScreen(viewModel: GoalStreamViewModel) {
    val favoriteTeams by viewModel.favoriteTeams.collectAsState()
    val articles by viewModel.articles.collectAsState()
    val bookmarkedArticles = remember(articles) {
        articles.filter { it.isBookmarked }
    }

    val availableTeams = listOf("Man City", "Arsenal", "Real Madrid", "FC Barcelona", "Liverpool", "Chelsea", "Bayern Munich", "Dortmund")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GoalStreamBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
                border = BorderStroke(1.dp, GoalStreamBorder)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(GoalStreamSecondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JD",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoalStreamDarkBlue
                        )
                    }
                    Column {
                        Text(
                            text = "John Doe",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoalStreamTextMain
                        )
                        Text(
                            text = "nabeelishfaq1122@gmail.com",
                            fontSize = 13.sp,
                            color = GoalStreamTextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoalStreamDeepBlue)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PRO MEMBER",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "FAVORITE TEAMS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoalStreamTextMuted,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Personalize your feeds and highlight upcoming fixtures.",
                    fontSize = 12.sp,
                    color = GoalStreamTextMuted
                )

                androidx.compose.foundation.layout.FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableTeams.forEach { team ->
                        val isFav = favoriteTeams.contains(team)
                        FilterChip(
                            selected = isFav,
                            onClick = { viewModel.toggleTeamFavorite(team) },
                            label = { Text(team, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                if (isFav) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoalStreamSecondary,
                                selectedLabelColor = GoalStreamDarkBlue,
                                containerColor = GoalStreamSurface,
                                labelColor = GoalStreamTextMuted
                            ),
                            modifier = Modifier.testTag("fav_team_chip_$team")
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "SAVED STORIES",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GoalStreamTextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (bookmarkedArticles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GoalStreamSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved articles yet. Tap the bookmark icon in any article to save it here.",
                        color = GoalStreamTextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(bookmarkedArticles) { article ->
                NewsRowItem(article = article, onClick = { viewModel.selectArticle(article) })
            }
        }

        item {
            Text(
                text = "GOALSTREAM AI ASSISTANT",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GoalStreamTextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            AiAssistantLauncherPanel(viewModel = viewModel)
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
                border = BorderStroke(1.dp, GoalStreamBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "GoalStream v1.0",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamTextMain
                    )
                    Text(
                        text = "This application utilizes Google AI Studio's server-side Gemini 1.5-Flash model to generate real-time tactics, predictions, and summaries.",
                        fontSize = 12.sp,
                        color = GoalStreamTextMuted
                    )
                }
            }
        }
    }
}

// --- ARTICLE DETAIL SCREEN ---
@Composable
fun ArticleDetailScreen(
    article: NewsArticle,
    viewModel: GoalStreamViewModel,
    onBack: () -> Unit
) {
    val aiLoading by viewModel.aiLoading.collectAsState()
    val aiResult by viewModel.aiResult.collectAsState()

    var isPlaying by remember { mutableStateOf(false) }
    var playProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (playProgress < 1f) {
                delay(1000)
                playProgress += 0.05f
            }
            isPlaying = false
            playProgress = 0f
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GoalStreamBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("article_back_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoalStreamTextMain)
                }

                IconButton(onClick = { viewModel.toggleArticleBookmark(article.id) }) {
                    Icon(
                        imageVector = if (article.isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Bookmark",
                        tint = if (article.isBookmarked) GoalStreamAccentRed else GoalStreamTextMain
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoalStreamSecondary)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = article.category.uppercase(),
                        color = GoalStreamDarkBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = article.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoalStreamTextMain,
                    lineHeight = 28.sp
                )

                Text(
                    text = "GoalStream Journalist • ${article.publishTime}",
                    fontSize = 12.sp,
                    color = GoalStreamTextMuted
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(GoalStreamDarkBlue, GoalStreamDeepBlue)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = size.minDimension / 2
                    )
                }
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
                border = BorderStroke(1.dp, GoalStreamBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = GoalStreamPrimary
                            )
                            Text(
                                text = "Listen to Article (AI Audio)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoalStreamTextMain
                            )
                        }
                        IconButton(
                            onClick = { isPlaying = !isPlaying }
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = GoalStreamPrimary
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { playProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp)),
                        color = GoalStreamPrimary,
                        trackColor = GoalStreamBorder.copy(alpha = 0.5f)
                    )
                }
            }
        }

        item {
            Text(
                text = article.content,
                fontSize = 15.sp,
                color = GoalStreamTextMain,
                lineHeight = 24.sp,
                textAlign = TextAlign.Justify
            )
        }

        if (article.keyTakeaways.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = GoalStreamSecondary.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, GoalStreamBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "KEY TAKEAWAYS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoalStreamDarkBlue,
                            letterSpacing = 1.sp
                        )
                        article.keyTakeaways.split(";").forEach { bullet ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("•", color = GoalStreamDarkBlue, fontSize = 14.sp)
                                Text(
                                    text = bullet,
                                    fontSize = 13.sp,
                                    color = GoalStreamTextMain,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
                border = BorderStroke(1.dp, GoalStreamBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "GEMINI AI ANALYSIS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamPrimary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Generate a custom tactical summary or expand transfer context using Gemini AI.",
                        fontSize = 12.sp,
                        color = GoalStreamTextMuted
                    )

                    Button(
                        onClick = { viewModel.askAiTakeaways(article) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_summarize_article"),
                        colors = ButtonDefaults.buttonColors(containerColor = GoalStreamPrimary)
                    ) {
                        Text("Ask Gemini AI Summarize", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    if (aiLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GoalStreamPrimary)
                        }
                    }

                    if (aiResult != null) {
                        Text(
                            text = aiResult!!,
                            fontSize = 13.sp,
                            color = GoalStreamTextMain,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- MATCH DETAIL SCREEN ---
@Composable
fun MatchDetailScreen(
    match: Match,
    viewModel: GoalStreamViewModel,
    onBack: () -> Unit
) {
    val aiLoading by viewModel.aiLoading.collectAsState()
    val aiResult by viewModel.aiResult.collectAsState()
    val matchEvents by viewModel.matchEvents.collectAsState()
    val highlightsSummary by viewModel.matchHighlightsSummary.collectAsState()

    val timeline = matchEvents[match.id] ?: emptyList()

    var activeSubTab by remember(match.id) { mutableStateOf(if (match.status == "FINISHED") "Highlights" else "Timeline") }

    LaunchedEffect(match.id, activeSubTab) {
        if (activeSubTab == "Highlights" && highlightsSummary == null) {
            viewModel.askMatchHighlightsSummary(match)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GoalStreamBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("match_back_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoalStreamTextMain)
                }

                IconButton(onClick = { viewModel.toggleMatchFavorite(match.id) }) {
                    Icon(
                        imageVector = if (match.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (match.isFavorite) GoalStreamAccentRed else GoalStreamTextMain
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
                border = BorderStroke(1.dp, GoalStreamBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = match.league.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamTextMuted,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            TeamColorBadge(colorHex = match.homeLogoColor, abbreviation = match.homeShort)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = match.homeTeam,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoalStreamTextMain,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            if (match.status == "LIVE" || match.status == "FINISHED") {
                                Text(
                                    text = "${match.homeScore} - ${match.awayScore}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoalStreamTextMain
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (match.status == "LIVE") GoalStreamAccentRed.copy(alpha = 0.1f) else GoalStreamBorder.copy(alpha = 0.3f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (match.status == "LIVE") match.timeElapsed else "FINISHED",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (match.status == "LIVE") GoalStreamAccentRed else GoalStreamTextMuted
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(GoalStreamSecondary)
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = match.matchTime,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoalStreamDarkBlue
                                    )
                                }
                                Text(
                                    text = match.matchDate,
                                    fontSize = 11.sp,
                                    color = GoalStreamTextMuted
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            TeamColorBadge(colorHex = match.awayLogoColor, abbreviation = match.awayShort)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = match.awayTeam,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoalStreamTextMain,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GoalStreamSurface)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val subTabs = if (match.status == "FINISHED") {
                    listOf("Highlights", "Timeline", "Lineups", "AI Prediction")
                } else {
                    listOf("Timeline", "Lineups", "AI Prediction")
                }
                subTabs.forEach { tab ->
                    val isActive = activeSubTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isActive) GoalStreamSecondary else Color.Transparent)
                            .clickable { activeSubTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) GoalStreamDarkBlue else GoalStreamTextMuted
                        )
                    }
                }
            }
        }

        when (activeSubTab) {
            "Highlights" -> {
                item {
                    MatchHighlightsTab(
                        match = match,
                        viewModel = viewModel,
                        aiLoading = aiLoading,
                        highlightsSummary = highlightsSummary,
                        timeline = timeline
                    )
                }
            }
            "Timeline" -> {
                if (timeline.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Timeline events appear once match kicks off.",
                                color = GoalStreamTextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    items(timeline) { event ->
                        TimelineRowEvent(event = event, match = match)
                    }
                }
            }
            "Lineups" -> {
                item {
                    TacticalLineupGrid(match = match)
                }
            }
            "AI Prediction" -> {
                item {
                    MatchAiPredictionTab(match = match, viewModel = viewModel, aiLoading = aiLoading, aiResult = aiResult)
                }
            }
        }


        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TimelineRowEvent(event: MatchTimelineEvent, match: Match) {
    val isHome = event.team == "HOME"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = if (isHome) Arrangement.Start else Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isHome) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GoalStreamSecondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = event.minute,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamDarkBlue
                    )
                }

                TimelineEventIcon(type = event.type)

                Column {
                    Text(
                        text = event.detail,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamTextMain
                    )
                    Text(
                        text = event.description,
                        fontSize = 11.sp,
                        color = GoalStreamTextMuted
                    )
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = event.detail,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamTextMain
                    )
                    Text(
                        text = event.description,
                        fontSize = 11.sp,
                        color = GoalStreamTextMuted
                    )
                }

                TimelineEventIcon(type = event.type)

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GoalStreamSurface)
                        .border(1.dp, GoalStreamBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = event.minute,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamTextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineEventIcon(type: String) {
    when (type) {
        "GOAL" -> {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text("⚽", fontSize = 11.sp)
            }
        }
        "CARD_YELLOW" -> {
            Box(
                modifier = Modifier
                    .size(width = 14.dp, height = 20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFFFEB3B))
            )
        }
        "CARD_RED" -> {
            Box(
                modifier = Modifier
                    .size(width = 14.dp, height = 20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(GoalStreamAccentRed)
            )
        }
    }
}

@Composable
fun TacticalLineupGrid(match: Match) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
        border = BorderStroke(1.dp, GoalStreamBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "TACTICAL FORMATION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoalStreamTextMuted,
                letterSpacing = 1.sp
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = match.homeTeam, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GoalStreamPrimary)
                    Text(text = "4-3-3", fontSize = 12.sp, color = GoalStreamTextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    val homeStarting = listOf("Ederson", "Walker", "Dias", "Ake", "Rodri", "De Bruyne", "Foden", "Silva", "Grealish", "Haaland")
                    homeStarting.forEach { p ->
                        Text(text = "• $p", fontSize = 12.sp, color = GoalStreamTextMain)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = match.awayTeam, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GoalStreamAccentRed)
                    Text(text = "4-2-3-1", fontSize = 12.sp, color = GoalStreamTextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    val awayStarting = listOf("Raya", "White", "Saliba", "Gabriel", "Rice", "Odegaard", "Saka", "Martinelli", "Havertz", "Jesus")
                    awayStarting.forEach { p ->
                        Text(text = "$p •", fontSize = 12.sp, color = GoalStreamTextMain)
                    }
                }
            }
        }
    }
}

@Composable
fun MatchAiPredictionTab(
    match: Match,
    viewModel: GoalStreamViewModel,
    aiLoading: Boolean,
    aiResult: String?
) {
    var customQuery by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
        border = BorderStroke(1.dp, GoalStreamBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "GEMINI AI TACTICAL INSIGHTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoalStreamPrimary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Consult Gemini AI to review lineups, predicted outcomes, and key metrics.",
                fontSize = 12.sp,
                color = GoalStreamTextMuted
            )

            Button(
                onClick = { viewModel.askAiPrediction(match) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_predict_match"),
                colors = ButtonDefaults.buttonColors(containerColor = GoalStreamPrimary)
            ) {
                Text("Predict Score with Gemini AI", color = Color.White, fontWeight = FontWeight.Bold)
            }

            if (aiLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoalStreamPrimary)
                }
            }

            if (aiResult != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GoalStreamBackground),
                    border = BorderStroke(1.dp, GoalStreamBorder)
                ) {
                    Text(
                        text = aiResult,
                        fontSize = 13.sp,
                        color = GoalStreamTextMain,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = GoalStreamBorder)

            Text(
                text = "Ask Live Match Analyst:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoalStreamTextMain
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = customQuery,
                    onValueChange = { customQuery = it },
                    placeholder = { Text("e.g., predicted subs?", fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = GoalStreamBackground,
                        unfocusedContainerColor = GoalStreamBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                Button(
                    onClick = {
                        viewModel.askAssistantQuestion("About ${match.homeTeam} vs ${match.awayTeam}: $customQuery")
                        customQuery = ""
                        viewModel.selectTab("Account")
                    },
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ask", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AiAssistantLauncherPanel(viewModel: GoalStreamViewModel) {
    val assistantChat by viewModel.assistantChat.collectAsState()
    val aiLoading by viewModel.aiLoading.collectAsState()
    var promptInput by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
        border = BorderStroke(1.dp, GoalStreamBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🤖", fontSize = 18.sp)
                    Text(
                        text = "GoalStream Tactical AI",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamTextMain
                    )
                }
                
                Text(
                    text = "Clear",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoalStreamPrimary,
                    modifier = Modifier
                        .clickable { viewModel.clearChat() }
                        .padding(4.dp)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(GoalStreamBackground)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = true
            ) {
                items(assistantChat.reversed()) { chat ->
                    val isUser = chat.second
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isUser) 12.dp else 0.dp,
                                        bottomEnd = if (isUser) 0.dp else 12.dp
                                    )
                                )
                                .background(if (isUser) GoalStreamSecondary else GoalStreamSurface)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = chat.first,
                                fontSize = 12.sp,
                                color = if (isUser) GoalStreamDarkBlue else GoalStreamTextMain,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            if (aiLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GoalStreamPrimary, modifier = Modifier.size(24.dp))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = { Text("Ask about league tables, tactics...", fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = GoalStreamBackground,
                        unfocusedContainerColor = GoalStreamBackground,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    singleLine = true
                )

                Button(
                    onClick = {
                        viewModel.askAssistantQuestion(promptInput)
                        promptInput = ""
                    },
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("btn_send_assistant"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoalStreamPrimary)
                ) {
                    Text("Send", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PulsingScoreUpdateNotification(
    notification: com.example.data.models.ScoreNotification,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "banner_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .border(
                BorderStroke(
                    2.dp,
                    Brush.radialGradient(
                        colors = listOf(
                            GoalStreamPrimary.copy(alpha = glowAlpha),
                            GoalStreamPrimary
                        )
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .testTag("score_update_notification_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = GoalStreamDarkBlue.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pulsing Green Dot & GOAL text
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoalStreamPrimary.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Visual Pulse Indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(GoalStreamPrimary)
                        )
                        Text(
                            text = "GOAL!",
                            color = GoalStreamPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Match Score Details
                Column {
                    Text(
                        text = notification.league.uppercase(),
                        color = GoalStreamTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = notification.homeTeam,
                            color = if (notification.scoringTeam == notification.homeTeam) GoalStreamSecondary else Color.White,
                            fontSize = 14.sp,
                            fontWeight = if (notification.scoringTeam == notification.homeTeam) FontWeight.Bold else FontWeight.Medium
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${notification.homeScore} - ${notification.awayScore}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = notification.awayTeam,
                            color = if (notification.scoringTeam == notification.awayTeam) GoalStreamSecondary else Color.White,
                            fontSize = 14.sp,
                            fontWeight = if (notification.scoringTeam == notification.awayTeam) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Elapsed time badge with glowing background
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GoalStreamAccentRed.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = notification.timeElapsed,
                        color = GoalStreamAccentRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = GoalStreamTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MatchHighlightsTab(
    match: Match,
    viewModel: GoalStreamViewModel,
    aiLoading: Boolean,
    highlightsSummary: String?,
    timeline: List<MatchTimelineEvent>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // AI SUMMARY CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
            border = BorderStroke(1.dp, GoalStreamBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(GoalStreamSecondary)
                        )
                        Text(
                            text = "GOALSTREAM AI SUMMARY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoalStreamPrimary,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.askMatchHighlightsSummary(match) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate Summary",
                            tint = GoalStreamSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (aiLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = GoalStreamSecondary,
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Analyzing match moments with GoalStream AI...",
                            fontSize = 12.sp,
                            color = GoalStreamTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (highlightsSummary != null) {
                    Text(
                        text = highlightsSummary,
                        fontSize = 13.sp,
                        color = Color.White,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No highlights summary available yet.",
                            fontSize = 12.sp,
                            color = GoalStreamTextMuted
                        )
                        Button(
                            onClick = { viewModel.askMatchHighlightsSummary(match) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoalStreamSecondary)
                        ) {
                            Text(
                                text = "Generate AI Summary",
                                color = GoalStreamDarkBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // KEY EVENTS SECTION
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "KEY EVENT TIMESTAMPS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoalStreamTextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            val keyEvents = timeline.filter { it.type == "GOAL" || it.type.startsWith("CARD_") }
            if (keyEvents.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
                    border = BorderStroke(1.dp, GoalStreamBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No key events recorded for this match.",
                            fontSize = 13.sp,
                            color = GoalStreamTextMuted
                        )
                    }
                }
            } else {
                keyEvents.forEach { event ->
                    KeyHighlightEventCard(event = event, match = match)
                }
            }
        }
    }
}

@Composable
fun KeyHighlightEventCard(event: MatchTimelineEvent, match: Match) {
    val isHome = event.team == "HOME"
    val teamName = if (isHome) match.homeTeam else match.awayTeam
    val teamColor = if (isHome) match.homeLogoColor else match.awayLogoColor

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("key_event_card_${event.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
        border = BorderStroke(1.dp, GoalStreamBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Timestamp Circle badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(GoalStreamSecondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = event.minute,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoalStreamSecondary
                )
            }

            // Graphic / Icon based on type
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                when (event.type) {
                    "GOAL" -> {
                        Text("⚽", fontSize = 14.sp)
                    }
                    "CARD_YELLOW" -> {
                        Box(
                            modifier = Modifier
                                .size(width = 10.dp, height = 14.dp)
                                .background(Color(0xFFF1C40F), RoundedCornerShape(2.dp))
                        )
                    }
                    "CARD_RED" -> {
                        Box(
                            modifier = Modifier
                                .size(width = 10.dp, height = 14.dp)
                                .background(GoalStreamAccentRed, RoundedCornerShape(2.dp))
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Key Event",
                            tint = GoalStreamPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Event description text
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Small color indicator for the team
                    val parseColor = try {
                        Color(android.graphics.Color.parseColor(teamColor))
                    } catch (e: Exception) {
                        GoalStreamPrimary
                    }
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(parseColor)
                    )
                    Text(
                        text = teamName.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamTextMuted
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = event.detail,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = event.description,
                    fontSize = 11.sp,
                    color = GoalStreamTextMuted
                )
            }
        }
    }
}

@Composable
fun StatsScreen(viewModel: GoalStreamViewModel) {
    val selectedLeague by viewModel.selectedLeagueForStats.collectAsState()
    var activeSubTab by remember { mutableStateOf("Scorers") } // "Scorers", "Assists", "Clean Sheets"

    val leagues = listOf("Premier League", "La Liga", "Champions League")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GoalStreamBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "PLAYER STATISTICS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GoalStreamSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "League Leaders",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Leagues Selection Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(leagues) { league ->
                    val isSelected = selectedLeague == league
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) GoalStreamSecondary else GoalStreamSurface)
                            .border(1.dp, if (isSelected) Color.Transparent else GoalStreamBorder, RoundedCornerShape(12.dp))
                            .clickable { viewModel.selectLeagueForStats(league) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("league_chip_${league.replace(" ", "_").lowercase()}")
                    ) {
                        Text(
                            text = league,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) GoalStreamDarkBlue else GoalStreamTextMain
                        )
                    }
                }
            }
        }

        // Stats Category Tabs switcher (Scorers, Assists, Clean Sheets)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(GoalStreamSurface)
                    .border(1.dp, GoalStreamBorder, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val subTabs = listOf(
                    Triple("Scorers", "Top Scorers", "⚽"),
                    Triple("Assists", "Assists Leaders", "👟"),
                    Triple("Clean Sheets", "Clean Sheets", "🧤")
                )
                subTabs.forEach { (tabId, label, emoji) ->
                    val isActive = activeSubTab == tabId
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isActive) GoalStreamBackground else Color.Transparent)
                            .clickable { activeSubTab = tabId }
                            .padding(vertical = 10.dp)
                            .testTag("stats_subtab_$tabId"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(emoji, fontSize = 12.sp)
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isActive) GoalStreamSecondary else GoalStreamTextMuted
                            )
                        }
                    }
                }
            }
        }

        // Header column of the list
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PLAYER & TEAM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoalStreamTextMuted,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = when (activeSubTab) {
                        "Scorers" -> "GOALS"
                        "Assists" -> "ASSISTS"
                        else -> "CLEAN SHEETS"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoalStreamTextMuted,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Players Standings List
        when (activeSubTab) {
            "Scorers" -> {
                val scorers = viewModel.getScorersForSelectedLeague(selectedLeague)
                if (scorers.isEmpty()) {
                    item {
                        EmptyStatsState()
                    }
                } else {
                    items(scorers) { scorer ->
                        PlayerScorerRow(scorer = scorer)
                    }
                }
            }
            "Assists" -> {
                val assisters = viewModel.getAssistersForSelectedLeague(selectedLeague)
                if (assisters.isEmpty()) {
                    item {
                        EmptyStatsState()
                    }
                } else {
                    items(assisters) { assister ->
                        PlayerAssisterRow(assister = assister)
                    }
                }
            }
            "Clean Sheets" -> {
                val cleanSheets = viewModel.getCleanSheetsForSelectedLeague(selectedLeague)
                if (cleanSheets.isEmpty()) {
                    item {
                        EmptyStatsState()
                    }
                } else {
                    items(cleanSheets) { keeper ->
                        GoalkeeperCleanSheetRow(keeper = keeper)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun EmptyStatsState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
        border = BorderStroke(1.dp, GoalStreamBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No statistical data available.",
                color = GoalStreamTextMuted,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun PlayerScorerRow(scorer: PlayerScorer) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("scorer_row_${scorer.rank}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
        border = BorderStroke(1.dp, GoalStreamBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rank Badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (scorer.rank == 1) GoalStreamSecondary.copy(alpha = 0.2f)
                            else Color.White.copy(alpha = 0.05f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${scorer.rank}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (scorer.rank == 1) GoalStreamSecondary else Color.White
                    )
                }

                Column {
                    Text(
                        text = scorer.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val parsedColor = try {
                            Color(android.graphics.Color.parseColor(scorer.teamColor))
                        } catch (e: Exception) {
                            GoalStreamPrimary
                        }
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                        )
                        Text(
                            text = scorer.team,
                            fontSize = 11.sp,
                            color = GoalStreamTextMuted
                        )
                    }
                }
            }

            // Stats Value
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${scorer.goals}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoalStreamSecondary
                    )
                    Text(
                        text = "${scorer.matchesPlayed} games (${scorer.penalties} pen)",
                        fontSize = 10.sp,
                        color = GoalStreamTextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerAssisterRow(assister: PlayerAssister) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("assister_row_${assister.rank}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
        border = BorderStroke(1.dp, GoalStreamBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (assister.rank == 1) GoalStreamSecondary.copy(alpha = 0.2f)
                            else Color.White.copy(alpha = 0.05f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${assister.rank}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (assister.rank == 1) GoalStreamSecondary else Color.White
                    )
                }

                Column {
                    Text(
                        text = assister.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val parsedColor = try {
                            Color(android.graphics.Color.parseColor(assister.teamColor))
                        } catch (e: Exception) {
                            GoalStreamPrimary
                        }
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                        )
                        Text(
                            text = assister.team,
                            fontSize = 11.sp,
                            color = GoalStreamTextMuted
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${assister.assists}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoalStreamSecondary
                    )
                    Text(
                        text = "${assister.matchesPlayed} games",
                        fontSize = 10.sp,
                        color = GoalStreamTextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun GoalkeeperCleanSheetRow(keeper: GoalkeeperCleanSheet) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("keeper_row_${keeper.rank}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
        border = BorderStroke(1.dp, GoalStreamBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (keeper.rank == 1) GoalStreamSecondary.copy(alpha = 0.2f)
                            else Color.White.copy(alpha = 0.05f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${keeper.rank}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (keeper.rank == 1) GoalStreamSecondary else Color.White
                    )
                }

                Column {
                    Text(
                        text = keeper.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val parsedColor = try {
                            Color(android.graphics.Color.parseColor(keeper.teamColor))
                        } catch (e: Exception) {
                            GoalStreamPrimary
                        }
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                        )
                        Text(
                            text = keeper.team,
                            fontSize = 11.sp,
                            color = GoalStreamTextMuted
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${keeper.cleanSheets}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoalStreamSecondary
                    )
                    Text(
                        text = "${keeper.matchesPlayed} games",
                        fontSize = 10.sp,
                        color = GoalStreamTextMuted
                    )
                }
            }
        }
    }
}

data class SearchPlayerResult(
    val name: String,
    val team: String,
    val teamColor: String,
    val role: String,
    val statsSummary: String? = null
)

val searchPlayers = listOf(
    // Premier League
    SearchPlayerResult("Erling Haaland", "Man City", "#6B7280", "Forward", "27 goals (4 pen) in 31 games"),
    SearchPlayerResult("Cole Palmer", "Chelsea", "#1D4ED8", "Midfielder", "22 goals (9 pen) in 34 games"),
    SearchPlayerResult("Alexander Isak", "Newcastle", "#3F3F46", "Forward", "21 goals (5 pen) in 30 games"),
    SearchPlayerResult("Ollie Watkins", "Aston Villa", "#701A75", "Forward", "19 goals in 37 games / 13 assists"),
    SearchPlayerResult("Mohamed Salah", "Liverpool", "#DC2626", "Forward", "18 goals (5 pen) in 32 games / 10 assists"),
    SearchPlayerResult("Bukayo Saka", "Arsenal", "#EF4444", "Forward", "16 goals (6 pen) in 35 games / 9 assists"),
    SearchPlayerResult("Kevin De Bruyne", "Man City", "#6B7280", "Midfielder", "10 assists in 18 games"),
    SearchPlayerResult("Martin Ødegaard", "Arsenal", "#EF4444", "Midfielder", "10 assists in 35 games"),
    SearchPlayerResult("David Raya", "Arsenal", "#EF4444", "Goalkeeper", "16 clean sheets in 32 games"),
    SearchPlayerResult("Jordan Pickford", "Everton", "#1D4ED8", "Goalkeeper", "13 clean sheets in 38 games"),
    SearchPlayerResult("Bernd Leno", "Fulham", "#3F3F46", "Goalkeeper", "10 clean sheets in 38 games"),
    SearchPlayerResult("Ederson", "Man City", "#6B7280", "Goalkeeper", "10 clean sheets in 33 games"),
    SearchPlayerResult("André Onana", "Man United", "#DA291C", "Goalkeeper", "9 clean sheets in 38 games"),
    
    // La Liga
    SearchPlayerResult("Artem Dovbyk", "Girona", "#EA580C", "Forward", "24 goals (7 pen) in 36 games"),
    SearchPlayerResult("Alexander Sørloth", "Villarreal", "#EAB308", "Forward", "23 goals in 34 games"),
    SearchPlayerResult("Jude Bellingham", "Real Madrid", "#FFFFFF", "Midfielder", "19 goals (1 pen) in 28 games / 5 assists"),
    SearchPlayerResult("Robert Lewandowski", "FC Barcelona", "#991B1B", "Forward", "19 goals (4 pen) in 35 games / 8 assists"),
    SearchPlayerResult("Ante Budimir", "Osasuna", "#991B1B", "Forward", "17 goals (3 pen) in 33 games"),
    SearchPlayerResult("Alex Baena", "Villarreal", "#EAB308", "Midfielder", "14 assists in 34 games"),
    SearchPlayerResult("Nico Williams", "Athletic Club", "#EF4444", "Forward", "11 assists in 31 games"),
    SearchPlayerResult("Sávio", "Girona", "#EA580C", "Forward", "10 assists in 37 games"),
    SearchPlayerResult("İlkay Gündoğan", "FC Barcelona", "#991B1B", "Midfielder", "9 assists in 36 games"),
    SearchPlayerResult("Unai Simón", "Athletic Club", "#EF4444", "Goalkeeper", "16 clean sheets in 36 games"),
    SearchPlayerResult("Marc-André ter Stegen", "FC Barcelona", "#991B1B", "Goalkeeper", "15 clean sheets in 28 games"),
    SearchPlayerResult("Alex Remiro", "Real Sociedad", "#1D4ED8", "Goalkeeper", "15 clean sheets in 37 games"),
    SearchPlayerResult("Giorgi Mamardashvili", "Valencia", "#FFFFFF", "Goalkeeper", "13 clean sheets in 37 games"),
    SearchPlayerResult("Jan Oblak", "Atletico Madrid", "#DC2626", "Goalkeeper", "13 clean sheets in 38 games"),

    // Champions League / Others
    SearchPlayerResult("Kylian Mbappé", "Real Madrid", "#FFFFFF", "Forward", "8 goals in 12 games"),
    SearchPlayerResult("Harry Kane", "Bayern Munich", "#DC2626", "Forward", "8 goals in 12 games"),
    SearchPlayerResult("Antoine Griezmann", "Atletico Madrid", "#DC2626", "Forward", "6 goals in 10 games"),
    SearchPlayerResult("Vinicius Jr.", "Real Madrid", "#FFFFFF", "Forward", "6 goals / 5 assists in 10 games"),
    SearchPlayerResult("Marcel Sabitzer", "Dortmund", "#EAB308", "Midfielder", "5 assists in 12 games"),
    SearchPlayerResult("Gregor Kobel", "Dortmund", "#EAB308", "Goalkeeper", "6 clean sheets in 12 games"),
    SearchPlayerResult("Yann Sommer", "Inter Milan", "#1D4ED8", "Goalkeeper", "4 clean sheets in 7 games"),
    SearchPlayerResult("Manuel Neuer", "Bayern Munich", "#DC2626", "Goalkeeper", "4 clean sheets in 9 games"),
    SearchPlayerResult("Andriy Lunin", "Real Madrid", "#FFFFFF", "Goalkeeper", "4 clean sheets in 8 games"),

    // Starting squads
    SearchPlayerResult("Kyle Walker", "Man City", "#6B7280", "Defender", "Squad Player"),
    SearchPlayerResult("Rúben Dias", "Man City", "#6B7280", "Defender", "Squad Player"),
    SearchPlayerResult("Nathan Aké", "Man City", "#6B7280", "Defender", "Squad Player"),
    SearchPlayerResult("Rodri", "Man City", "#6B7280", "Midfielder", "Squad Player"),
    SearchPlayerResult("Phil Foden", "Man City", "#6B7280", "Forward", "Squad Player"),
    SearchPlayerResult("Bernardo Silva", "Man City", "#6B7280", "Midfielder", "Squad Player"),
    SearchPlayerResult("Jack Grealish", "Man City", "#6B7280", "Forward", "Squad Player"),
    SearchPlayerResult("Ben White", "Arsenal", "#EF4444", "Defender", "Squad Player"),
    SearchPlayerResult("William Saliba", "Arsenal", "#EF4444", "Defender", "Squad Player"),
    SearchPlayerResult("Gabriel Magalhães", "Arsenal", "#EF4444", "Defender", "Squad Player"),
    SearchPlayerResult("Declan Rice", "Arsenal", "#EF4444", "Midfielder", "Squad Player"),
    SearchPlayerResult("Gabriel Martinelli", "Arsenal", "#EF4444", "Forward", "Squad Player"),
    SearchPlayerResult("Kai Havertz", "Arsenal", "#EF4444", "Forward", "Squad Player"),
    SearchPlayerResult("Gabriel Jesus", "Arsenal", "#EF4444", "Forward", "Squad Player")
)

@Composable
fun GlobalSearchScreen(
    searchQuery: String,
    viewModel: GoalStreamViewModel
) {
    val matches by viewModel.matches.collectAsState()
    val articles by viewModel.articles.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") } // "All", "Players", "Teams", "Leagues", "Matches", "News"

    // 1. Players Search
    val matchingPlayers = remember(searchQuery) {
        searchPlayers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.team.contains(searchQuery, ignoreCase = true)
        }
    }

    // 2. Teams Search (gathered dynamically from actual matches)
    val allTeams = remember(matches) {
        matches.flatMap { listOf(
            Triple(it.homeTeam, it.homeLogoColor, it.league),
            Triple(it.awayTeam, it.awayLogoColor, it.league)
        ) }.distinctBy { it.first }
    }
    val matchingTeams = remember(searchQuery, allTeams) {
        allTeams.filter {
            it.first.contains(searchQuery, ignoreCase = true)
        }
    }

    // 3. Leagues Search (gathered dynamically)
    val allLeagues = remember(matches) {
        matches.map { it.league }.distinct()
    }
    val matchingLeagues = remember(searchQuery, allLeagues) {
        allLeagues.filter {
            it.contains(searchQuery, ignoreCase = true)
        }
    }

    // 4. Matches Search
    val matchingMatches = remember(searchQuery, matches) {
        matches.filter {
            it.homeTeam.contains(searchQuery, ignoreCase = true) ||
            it.awayTeam.contains(searchQuery, ignoreCase = true) ||
            it.league.contains(searchQuery, ignoreCase = true)
        }
    }

    // 5. News Search
    val matchingNews = remember(searchQuery, articles) {
        articles.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    val filters = listOf("All", "Players", "Teams", "Leagues", "Matches", "News")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(GoalStreamBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Space/Padding
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Horizontal filter chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) GoalStreamSecondary else GoalStreamSurface)
                            .border(1.dp, if (isSelected) Color.Transparent else GoalStreamBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("search_filter_chip_${filter.lowercase()}")
                    ) {
                        Text(
                            text = filter,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) GoalStreamDarkBlue else GoalStreamTextMain
                        )
                    }
                }
            }
        }

        // Empty State Check
        val totalResults = matchingPlayers.size + matchingTeams.size + matchingLeagues.size + matchingMatches.size + matchingNews.size
        if (totalResults == 0) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🔍",
                            fontSize = 48.sp
                        )
                        Text(
                            text = "No results found for \"$searchQuery\"",
                            color = GoalStreamTextMuted,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Check spelling or search for something else.",
                            color = GoalStreamTextMuted.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Render category sections
            
            // 1. PLAYERS
            if ((selectedFilter == "All" || selectedFilter == "Players") && matchingPlayers.isNotEmpty()) {
                item {
                    Text(
                        text = "⚽ PLAYERS (${matchingPlayers.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(matchingPlayers) { player ->
                    SearchPlayerRow(player = player, onClick = {
                        val playerLeague = when (player.team) {
                            "Man City", "Chelsea", "Newcastle", "Aston Villa", "Liverpool", "Arsenal", "Everton", "Fulham", "Man United" -> "Premier League"
                            "Girona", "Villarreal", "Real Madrid", "FC Barcelona", "Osasuna", "Athletic Club", "Real Sociedad", "Valencia", "Atletico Madrid" -> "La Liga"
                            else -> "Champions League"
                        }
                        viewModel.selectLeagueForStats(playerLeague)
                        viewModel.selectTab("Stats")
                        viewModel.setSearchQuery("") // Close search and focus stats
                    })
                }
            }

            // 2. TEAMS
            if ((selectedFilter == "All" || selectedFilter == "Teams") && matchingTeams.isNotEmpty()) {
                item {
                    Text(
                        text = "🛡️ TEAMS (${matchingTeams.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(matchingTeams) { team ->
                    SearchTeamRow(teamName = team.first, logoColor = team.second, leagueName = team.third, onClick = {
                        viewModel.setSearchQuery(team.first) // Set query to focus on team's matches/news
                    })
                }
            }

            // 3. LEAGUES
            if ((selectedFilter == "All" || selectedFilter == "Leagues") && matchingLeagues.isNotEmpty()) {
                item {
                    Text(
                        text = "🏆 LEAGUES (${matchingLeagues.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(matchingLeagues) { league ->
                    SearchLeagueRow(leagueName = league, onClick = {
                        viewModel.selectLeagueForStats(league)
                        viewModel.selectTab("Stats")
                        viewModel.setSearchQuery("") // Close search and open league stats
                    })
                }
            }

            // 4. MATCHES
            if ((selectedFilter == "All" || selectedFilter == "Matches") && matchingMatches.isNotEmpty()) {
                item {
                    Text(
                        text = "⏱️ MATCHES (${matchingMatches.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(matchingMatches) { match ->
                    MatchRowItem(match = match, onClick = {
                        viewModel.selectMatch(match)
                        viewModel.setSearchQuery("") // Open match and close search
                    })
                }
            }

            // 5. NEWS
            if ((selectedFilter == "All" || selectedFilter == "News") && matchingNews.isNotEmpty()) {
                item {
                    Text(
                        text = "📰 NEWS STORIES (${matchingNews.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamSecondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(matchingNews) { article ->
                    NewsRowItem(article = article, onClick = {
                        viewModel.selectArticle(article)
                        viewModel.setSearchQuery("") // Open article and close search
                    })
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SearchPlayerRow(
    player: SearchPlayerResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("search_player_${player.name.replace(" ", "_").lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
        border = BorderStroke(1.dp, GoalStreamBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Initial Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = if (player.name.isNotEmpty()) player.name.take(1) else "P"
                    Text(
                        text = initial,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoalStreamSecondary
                    )
                }

                Column {
                    Text(
                        text = player.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val parsedColor = try {
                            Color(android.graphics.Color.parseColor(player.teamColor))
                        } catch (e: Exception) {
                            GoalStreamPrimary
                        }
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                        )
                        Text(
                            text = "${player.team} • ${player.role}",
                            fontSize = 12.sp,
                            color = GoalStreamTextMuted
                        )
                    }
                }
            }

            // Right Chevron / Quick stats summary
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                if (player.statsSummary != null) {
                    Text(
                        text = player.statsSummary,
                        fontSize = 11.sp,
                        color = GoalStreamSecondary,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = "View Details",
                    tint = GoalStreamTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SearchTeamRow(
    teamName: String,
    logoColor: String,
    leagueName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("search_team_${teamName.replace(" ", "_").lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
        border = BorderStroke(1.dp, GoalStreamBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Team Badge Color
                val parsedColor = try {
                    Color(android.graphics.Color.parseColor(logoColor))
                } catch (e: Exception) {
                    GoalStreamPrimary
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(parsedColor.copy(alpha = 0.15f))
                        .border(1.5.dp, parsedColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = teamName.take(2).uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = parsedColor
                    )
                }

                Column {
                    Text(
                        text = teamName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = leagueName,
                        fontSize = 12.sp,
                        color = GoalStreamTextMuted
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Matches",
                tint = GoalStreamSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SearchLeagueRow(
    leagueName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("search_league_${leagueName.replace(" ", "_").lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
        border = BorderStroke(1.dp, GoalStreamBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoalStreamSecondary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏆",
                        fontSize = 18.sp
                    )
                }

                Column {
                    Text(
                        text = leagueName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "View Standings & Stats",
                        fontSize = 12.sp,
                        color = GoalStreamTextMuted
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "View Details",
                tint = GoalStreamTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111" // Standard Test Banner ID
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GoalStreamSurface),
        border = BorderStroke(1.dp, GoalStreamBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFEAB308)) // Yellow AdBadge
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "AD",
                        color = Color.Black,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sponsored Partner",
                    color = GoalStreamTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            // Real AdMob AdView integrated with play-services-ads
            androidx.compose.ui.viewinterop.AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                factory = { context ->
                    com.google.android.gms.ads.AdView(context).apply {
                        setAdSize(com.google.android.gms.ads.AdSize.BANNER)
                        setAdUnitId(adUnitId)
                        loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
                    }
                }
            )
        }
    }
}



