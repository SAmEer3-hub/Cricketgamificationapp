package com.example.fantasycricketapp.utils



import com.example.fantasycricketapp.data.model.Match

/**
 * DummyData — Provides static/hardcoded data for development and testing.
 *
 * In Phase 5+, real data will be loaded from Firebase Firestore.
 * This object keeps our dummy data centralized and easy to replace.
 */
object DummyData {

    /**
     * Returns a list of dummy cricket matches.
     * Contains a mix of UPCOMING, LIVE, and COMPLETED matches
     * so we can test different UI states.
     */
    fun getMatches(): List<Match> {
        return listOf(

            Match(
                matchId = "match_001",
                matchName = "India vs Australia",
                team1 = "India",
                team2 = "Australia",
                team1Short = "IND",
                team2Short = "AUS",
                team1Flag = "🇮🇳",
                team2Flag = "🇦🇺",
                venue = "Wankhede Stadium, Mumbai",
                matchDate = "Today, 7:30 PM",
                matchType = "T20",
                status = "LIVE"
            ),

            Match(
                matchId = "match_002",
                matchName = "England vs New Zealand",
                team1 = "England",
                team2 = "New Zealand",
                team1Short = "ENG",
                team2Short = "NZ",
                team1Flag = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
                team2Flag = "🇳🇿",
                venue = "Lord's Cricket Ground, London",
                matchDate = "Tomorrow, 3:00 PM",
                matchType = "ODI",
                status = "UPCOMING"
            ),

            Match(
                matchId = "match_003",
                matchName = "Pakistan vs South Africa",
                team1 = "Pakistan",
                team2 = "South Africa",
                team1Short = "PAK",
                team2Short = "SA",
                team1Flag = "🇵🇰",
                team2Flag = "🇿🇦",
                venue = "National Stadium, Karachi",
                matchDate = "28 Jan, 2:00 PM",
                matchType = "T20",
                status = "UPCOMING"
            ),

            Match(
                matchId = "match_004",
                matchName = "West Indies vs Sri Lanka",
                team1 = "West Indies",
                team2 = "Sri Lanka",
                team1Short = "WI",
                team2Short = "SL",
                team1Flag = "🏝️",
                team2Flag = "🇱🇰",
                venue = "Kensington Oval, Barbados",
                matchDate = "29 Jan, 6:00 PM",
                matchType = "T20",
                status = "UPCOMING"
            ),

            Match(
                matchId = "match_005",
                matchName = "India vs Bangladesh",
                team1 = "India",
                team2 = "Bangladesh",
                team1Short = "IND",
                team2Short = "BAN",
                team1Flag = "🇮🇳",
                team2Flag = "🇧🇩",
                venue = "Eden Gardens, Kolkata",
                matchDate = "22 Jan, 7:00 PM",
                matchType = "ODI",
                status = "COMPLETED"
            ),

            Match(
                matchId = "match_006",
                matchName = "Australia vs New Zealand",
                team1 = "Australia",
                team2 = "New Zealand",
                team1Short = "AUS",
                team2Short = "NZ",
                team1Flag = "🇦🇺",
                team2Flag = "🇳🇿",
                venue = "MCG, Melbourne",
                matchDate = "20 Jan, 1:30 PM",
                matchType = "Test",
                status = "COMPLETED"
            ),

            Match(
                matchId = "match_007",
                matchName = "South Africa vs England",
                team1 = "South Africa",
                team2 = "England",
                team1Short = "SA",
                team2Short = "ENG",
                team1Flag = "🇿🇦",
                team2Flag = "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
                venue = "Newlands Cricket Ground, Cape Town",
                matchDate = "31 Jan, 4:00 PM",
                matchType = "T20",
                status = "UPCOMING"
            ),

            Match(
                matchId = "match_008",
                matchName = "India vs Pakistan",
                team1 = "India",
                team2 = "Pakistan",
                team1Short = "IND",
                team2Short = "PAK",
                team1Flag = "🇮🇳",
                team2Flag = "🇵🇰",
                venue = "Dubai International Stadium",
                matchDate = "2 Feb, 7:30 PM",
                matchType = "T20",
                status = "UPCOMING"
            )
        )
    }
}