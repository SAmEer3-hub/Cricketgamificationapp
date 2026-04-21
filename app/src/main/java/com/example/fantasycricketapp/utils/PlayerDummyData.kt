package com.example.fantasycricketapp.utils

import com.example.fantasycricketapp.data.model.Player


object PlayerDummyData {

    /**
     * Returns the correct player list based on matchId.
     * Falls back to a generic India vs Australia squad.
     */
    fun getPlayersForMatch(matchId: String): List<Player> {
        return when (matchId) {
            "match_001" -> getIndiaVsAustraliaPlayers()
            "match_002" -> getEnglandVsNewZealandPlayers()
            "match_003" -> getPakistanVsSouthAfricaPlayers()
            "match_004" -> getWestIndiesVsSriLankaPlayers()
            "match_005" -> getIndiaVsBangladeshPlayers()
            "match_006" -> getAustraliaVsNewZealandPlayers()
            "match_007" -> getSouthAfricaVsEnglandPlayers()
            "match_008" -> getIndiaVsPakistanPlayers()
            else        -> getIndiaVsAustraliaPlayers()
        }
    }

    // ──────────────────────────────────────────
    // MATCH 001 — India vs Australia
    // ──────────────────────────────────────────
    private fun getIndiaVsAustraliaPlayers(): List<Player> = listOf(
        // --- INDIA ---
        Player("p_ind_1",  "Virat Kohli",      "IND", "BAT",  9.5, "Right-hand bat",  "N/A"),
        Player("p_ind_2",  "Rohit Sharma",     "IND", "BAT",  9.0, "Right-hand bat",  "N/A"),
        Player("p_ind_3",  "KL Rahul",         "IND", "WK",   8.5, "Right-hand bat",  "N/A"),
        Player("p_ind_4",  "Hardik Pandya",    "IND", "AR",   9.0, "Right-hand bat",  "Right-arm fast-medium"),
        Player("p_ind_5",  "Ravindra Jadeja",  "IND", "AR",   8.5, "Left-hand bat",   "Slow left-arm orthodox"),
        Player("p_ind_6",  "Jasprit Bumrah",   "IND", "BOWL", 9.5, "Right-hand bat",  "Right-arm fast"),
        Player("p_ind_7",  "Mohammed Shami",   "IND", "BOWL", 8.0, "Right-hand bat",  "Right-arm fast-medium"),

        // --- AUSTRALIA ---
        Player("p_aus_1",  "Steve Smith",      "AUS", "BAT",  9.0, "Right-hand bat",  "Right-arm leg break"),
        Player("p_aus_2",  "David Warner",     "AUS", "BAT",  8.5, "Left-hand bat",   "N/A"),
        Player("p_aus_3",  "Pat Cummins",      "AUS", "BOWL", 9.5, "Right-hand bat",  "Right-arm fast"),
        Player("p_aus_4",  "Mitchell Starc",   "AUS", "BOWL", 8.5, "Left-hand bat",   "Left-arm fast"),
        Player("p_aus_5",  "Glenn Maxwell",    "AUS", "AR",   9.0, "Right-hand bat",  "Right-arm off break"),
        Player("p_aus_6",  "Alex Carey",       "AUS", "WK",   7.5, "Left-hand bat",   "N/A"),
        Player("p_aus_7",  "Josh Hazlewood",   "AUS", "BOWL", 8.0, "Right-hand bat",  "Right-arm fast-medium"),

        // --- Extra ---
        Player("p_mix_1",  "Axar Patel",       "IND", "AR",   7.5, "Left-hand bat",   "Left-arm orthodox")
    )

    // ──────────────────────────────────────────
    // MATCH 002 — England vs New Zealand
    // ──────────────────────────────────────────
    private fun getEnglandVsNewZealandPlayers(): List<Player> = listOf(
        Player("p_eng_1", "Joe Root",          "ENG", "BAT",  9.5, "Right-hand bat",  "Right-arm off break"),
        Player("p_eng_2", "Ben Stokes",        "ENG", "AR",   9.5, "Left-hand bat",   "Right-arm fast-medium"),
        Player("p_eng_3", "Jos Buttler",       "ENG", "WK",   9.0, "Right-hand bat",  "N/A"),
        Player("p_eng_4", "Jofra Archer",      "ENG", "BOWL", 8.5, "Right-hand bat",  "Right-arm fast"),
        Player("p_eng_5", "Jonny Bairstow",    "ENG", "BAT",  8.0, "Right-hand bat",  "N/A"),
        Player("p_eng_6", "Stuart Broad",      "ENG", "BOWL", 8.0, "Right-hand bat",  "Right-arm fast-medium"),
        Player("p_eng_7", "Moeen Ali",         "ENG", "AR",   8.0, "Right-hand bat",  "Right-arm off break"),
        Player("p_nz_1",  "Kane Williamson",   "NZ",  "BAT",  9.5, "Right-hand bat",  "Right-arm off break"),
        Player("p_nz_2",  "Trent Boult",       "NZ",  "BOWL", 9.0, "Right-hand bat",  "Left-arm fast-medium"),
        Player("p_nz_3",  "Tim Southee",       "NZ",  "BOWL", 8.5, "Right-hand bat",  "Right-arm fast-medium"),
        Player("p_nz_4",  "Devon Conway",      "NZ",  "WK",   8.0, "Left-hand bat",   "N/A"),
        Player("p_nz_5",  "Martin Guptill",    "NZ",  "BAT",  8.0, "Right-hand bat",  "N/A"),
        Player("p_nz_6",  "Mitchell Santner",  "NZ",  "AR",   7.5, "Left-hand bat",   "Slow left-arm orthodox"),
        Player("p_nz_7",  "Lockie Ferguson",   "NZ",  "BOWL", 7.5, "Right-hand bat",  "Right-arm fast"),
        Player("p_mix_2", "Mark Wood",         "ENG", "BOWL", 8.0, "Right-hand bat",  "Right-arm fast")
    )

    // ──────────────────────────────────────────
    // MATCH 003 — Pakistan vs South Africa
    // ──────────────────────────────────────────
    private fun getPakistanVsSouthAfricaPlayers(): List<Player> = listOf(
        Player("p_pak_1", "Babar Azam",        "PAK", "BAT",  9.5, "Right-hand bat",  "N/A"),
        Player("p_pak_2", "Shaheen Afridi",    "PAK", "BOWL", 9.0, "Left-hand bat",   "Left-arm fast"),
        Player("p_pak_3", "Mohammad Rizwan",   "PAK", "WK",   9.0, "Right-hand bat",  "N/A"),
        Player("p_pak_4", "Shadab Khan",       "PAK", "AR",   8.5, "Right-hand bat",  "Right-arm leg break"),
        Player("p_pak_5", "Fakhar Zaman",      "PAK", "BAT",  8.0, "Left-hand bat",   "N/A"),
        Player("p_pak_6", "Haris Rauf",        "PAK", "BOWL", 8.0, "Right-hand bat",  "Right-arm fast"),
        Player("p_pak_7", "Imad Wasim",        "PAK", "AR",   7.5, "Left-hand bat",   "Slow left-arm orthodox"),
        Player("p_sa_1",  "Quinton de Kock",   "SA",  "WK",   9.0, "Left-hand bat",   "N/A"),
        Player("p_sa_2",  "Kagiso Rabada",     "SA",  "BOWL", 9.5, "Right-hand bat",  "Right-arm fast"),
        Player("p_sa_3",  "Faf du Plessis",    "SA",  "BAT",  8.5, "Right-hand bat",  "N/A"),
        Player("p_sa_4",  "Anrich Nortje",     "SA",  "BOWL", 8.0, "Right-hand bat",  "Right-arm fast"),
        Player("p_sa_5",  "David Miller",      "SA",  "BAT",  8.0, "Left-hand bat",   "N/A"),
        Player("p_sa_6",  "Keshav Maharaj",    "SA",  "BOWL", 7.5, "Right-hand bat",  "Slow left-arm orthodox"),
        Player("p_sa_7",  "Temba Bavuma",      "SA",  "BAT",  8.0, "Right-hand bat",  "N/A"),
        Player("p_mix_3", "Tabraiz Shamsi",    "SA",  "BOWL", 7.5, "Left-hand bat",   "Left-arm wrist spin")
    )

    // ──────────────────────────────────────────
    // MATCH 004 — West Indies vs Sri Lanka
    // ──────────────────────────────────────────
    private fun getWestIndiesVsSriLankaPlayers(): List<Player> = listOf(
        Player("p_wi_1",  "Kieron Pollard",    "WI",  "AR",   9.0, "Right-hand bat",  "Right-arm medium"),
        Player("p_wi_2",  "Nicholas Pooran",   "WI",  "WK",   8.5, "Left-hand bat",   "N/A"),
        Player("p_wi_3",  "Andre Russell",     "WI",  "AR",   9.5, "Right-hand bat",  "Right-arm fast-medium"),
        Player("p_wi_4",  "Jason Holder",      "WI",  "AR",   8.5, "Right-hand bat",  "Right-arm fast-medium"),
        Player("p_wi_5",  "Chris Gayle",       "WI",  "BAT",  8.0, "Left-hand bat",   "Right-arm off break"),
        Player("p_wi_6",  "Shimron Hetmyer",   "WI",  "BAT",  7.5, "Left-hand bat",   "N/A"),
        Player("p_wi_7",  "Alzarri Joseph",    "WI",  "BOWL", 7.5, "Right-hand bat",  "Right-arm fast"),
        Player("p_sl_1",  "Wanindu Hasaranga", "SL",  "AR",   9.0, "Right-hand bat",  "Right-arm leg break"),
        Player("p_sl_2",  "Dasun Shanaka",     "SL",  "AR",   8.0, "Right-hand bat",  "Right-arm medium"),
        Player("p_sl_3",  "Pathum Nissanka",   "SL",  "BAT",  7.5, "Right-hand bat",  "N/A"),
        Player("p_sl_4",  "Dushmantha Chameera","SL", "BOWL", 8.0, "Right-hand bat",  "Right-arm fast"),
        Player("p_sl_5",  "Kusal Mendis",      "SL",  "WK",   8.0, "Right-hand bat",  "N/A"),
        Player("p_sl_6",  "Maheesh Theekshana","SL",  "BOWL", 7.5, "Right-hand bat",  "Right-arm off break"),
        Player("p_sl_7",  "Charith Asalanka",  "SL",  "BAT",  7.5, "Left-hand bat",   "N/A"),
        Player("p_mix_4", "Romario Shepherd",  "WI",  "AR",   7.5, "Right-hand bat",  "Right-arm fast-medium")
    )

    // ──────────────────────────────────────────
    // MATCH 005 — India vs Bangladesh
    // ──────────────────────────────────────────
    private fun getIndiaVsBangladeshPlayers(): List<Player> = listOf(
        Player("p_ind_b1","Shikhar Dhawan",    "IND", "BAT",  8.5, "Left-hand bat",   "N/A"),
        Player("p_ind_b2","Ishan Kishan",      "IND", "WK",   8.0, "Left-hand bat",   "N/A"),
        Player("p_ind_b3","Suryakumar Yadav",  "IND", "BAT",  9.0, "Right-hand bat",  "N/A"),
        Player("p_ind_b4","Deepak Chahar",     "IND", "BOWL", 7.5, "Right-hand bat",  "Right-arm fast-medium"),
        Player("p_ind_b5","Kuldeep Yadav",     "IND", "BOWL", 8.0, "Left-hand bat",   "Left-arm wrist spin"),
        Player("p_ind_b6","Washington Sundar", "IND", "AR",   7.5, "Right-hand bat",  "Right-arm off break"),
        Player("p_ind_b7","Shreyas Iyer",      "IND", "BAT",  8.5, "Right-hand bat",  "N/A"),
        Player("p_ban_1", "Shakib Al Hasan",   "BAN", "AR",   9.5, "Left-hand bat",   "Slow left-arm orthodox"),
        Player("p_ban_2", "Mushfiqur Rahim",   "BAN", "WK",   8.5, "Right-hand bat",  "N/A"),
        Player("p_ban_3", "Tamim Iqbal",       "BAN", "BAT",  8.5, "Left-hand bat",   "N/A"),
        Player("p_ban_4", "Mustafizur Rahman", "BAN", "BOWL", 8.5, "Right-hand bat",  "Left-arm fast-medium"),
        Player("p_ban_5", "Mahmudullah",       "BAN", "AR",   8.0, "Right-hand bat",  "Right-arm off break"),
        Player("p_ban_6", "Liton Das",         "BAN", "BAT",  7.5, "Right-hand bat",  "N/A"),
        Player("p_ban_7", "Taskin Ahmed",      "BAN", "BOWL", 7.5, "Right-hand bat",  "Right-arm fast"),
        Player("p_mix_5", "Mehidy Hasan",      "BAN", "AR",   7.5, "Right-hand bat",  "Right-arm off break")
    )

    // ──────────────────────────────────────────
    // MATCH 006 — Australia vs New Zealand
    // ──────────────────────────────────────────
    private fun getAustraliaVsNewZealandPlayers(): List<Player> = listOf(
        Player("p_aus_b1","Travis Head",       "AUS", "BAT",  8.5, "Left-hand bat",   "Right-arm off break"),
        Player("p_aus_b2","Marnus Labuschagne","AUS", "BAT",  9.0, "Right-hand bat",  "Right-arm leg break"),
        Player("p_aus_b3","Matthew Wade",      "AUS", "WK",   7.5, "Left-hand bat",   "N/A"),
        Player("p_aus_b4","Nathan Lyon",       "AUS", "BOWL", 8.5, "Right-hand bat",  "Right-arm off break"),
        Player("p_aus_b5","Cameron Green",     "AUS", "AR",   8.0, "Right-hand bat",  "Right-arm fast-medium"),
        Player("p_aus_b6","Aaron Finch",       "AUS", "BAT",  8.0, "Right-hand bat",  "N/A"),
        Player("p_aus_b7","Adam Zampa",        "AUS", "BOWL", 8.0, "Right-hand bat",  "Right-arm leg break"),
        Player("p_nz_b1", "Ross Taylor",       "NZ",  "BAT",  8.5, "Right-hand bat",  "Right-arm off break"),
        Player("p_nz_b2", "Tom Latham",        "NZ",  "WK",   8.0, "Left-hand bat",   "N/A"),
        Player("p_nz_b3", "Kyle Jamieson",     "NZ",  "AR",   8.5, "Right-hand bat",  "Right-arm fast"),
        Player("p_nz_b4", "Ish Sodhi",         "NZ",  "BOWL", 7.5, "Right-hand bat",  "Right-arm leg break"),
        Player("p_nz_b5", "Henry Nicholls",    "NZ",  "BAT",  7.5, "Left-hand bat",   "N/A"),
        Player("p_nz_b6", "Matt Henry",        "NZ",  "BOWL", 7.5, "Right-hand bat",  "Right-arm fast-medium"),
        Player("p_nz_b7", "Glenn Phillips",    "NZ",  "AR",   8.0, "Right-hand bat",  "Right-arm off break"),
        Player("p_mix_6", "Michael Bracewell", "NZ",  "AR",   7.5, "Right-hand bat",  "Right-arm off break")
    )

    // ──────────────────────────────────────────
    // MATCH 007 — South Africa vs England
    // ──────────────────────────────────────────
    private fun getSouthAfricaVsEnglandPlayers(): List<Player> = listOf(
        Player("p_sa_e1", "Aiden Markram",     "SA",  "BAT",  8.5, "Right-hand bat",  "Right-arm off break"),
        Player("p_sa_e2", "Rassie van der Dussen","SA","BAT", 8.0, "Right-hand bat",  "N/A"),
        Player("p_sa_e3", "Marco Jansen",      "SA",  "AR",   8.0, "Right-hand bat",  "Left-arm fast"),
        Player("p_sa_e4", "Gerald Coetzee",    "SA",  "BOWL", 7.5, "Right-hand bat",  "Right-arm fast"),
        Player("p_sa_e5", "Ryan Rickelton",    "SA",  "WK",   7.5, "Left-hand bat",   "N/A"),
        Player("p_sa_e6", "Wiaan Mulder",      "SA",  "AR",   7.5, "Right-hand bat",  "Right-arm fast-medium"),
        Player("p_sa_e7", "Lungi Ngidi",       "SA",  "BOWL", 8.0, "Right-hand bat",  "Right-arm fast"),
        Player("p_eng_e1","Harry Brook",       "ENG", "BAT",  8.5, "Right-hand bat",  "Right-arm off break"),
        Player("p_eng_e2","Zak Crawley",       "ENG", "BAT",  7.5, "Right-hand bat",  "N/A"),
        Player("p_eng_e3","Ollie Pope",        "ENG", "BAT",  8.0, "Right-hand bat",  "Right-arm off break"),
        Player("p_eng_e4","Gus Atkinson",      "ENG", "BOWL", 8.0, "Right-hand bat",  "Right-arm fast"),
        Player("p_eng_e5","Ollie Robinson",    "ENG", "BOWL", 8.0, "Right-hand bat",  "Right-arm fast-medium"),
        Player("p_eng_e6","Ben Duckett",       "ENG", "BAT",  7.5, "Left-hand bat",   "N/A"),
        Player("p_eng_e7","Jamie Overton",     "ENG", "AR",   7.5, "Right-hand bat",  "Right-arm fast"),
        Player("p_mix_7", "Sam Curran",        "ENG", "AR",   8.5, "Left-hand bat",   "Left-arm fast-medium")
    )

    // ──────────────────────────────────────────
    // MATCH 008 — India vs Pakistan
    // ──────────────────────────────────────────
    private fun getIndiaVsPakistanPlayers(): List<Player> = listOf(
        Player("p_ind_p1","Virat Kohli",       "IND", "BAT",  9.5, "Right-hand bat",  "N/A"),
        Player("p_ind_p2","Rohit Sharma",      "IND", "BAT",  9.5, "Right-hand bat",  "N/A"),
        Player("p_ind_p3","KL Rahul",          "IND", "WK",   9.0, "Right-hand bat",  "N/A"),
        Player("p_ind_p4","Hardik Pandya",     "IND", "AR",   9.0, "Right-hand bat",  "Right-arm fast-medium"),
        Player("p_ind_p5","Jasprit Bumrah",    "IND", "BOWL", 9.5, "Right-hand bat",  "Right-arm fast"),
        Player("p_ind_p6","Ravindra Jadeja",   "IND", "AR",   8.5, "Left-hand bat",   "Slow left-arm orthodox"),
        Player("p_ind_p7","Suryakumar Yadav",  "IND", "BAT",  9.0, "Right-hand bat",  "N/A"),
        Player("p_pak_p1","Babar Azam",        "PAK", "BAT",  9.5, "Right-hand bat",  "N/A"),
        Player("p_pak_p2","Shaheen Afridi",    "PAK", "BOWL", 9.0, "Left-hand bat",   "Left-arm fast"),
        Player("p_pak_p3","Mohammad Rizwan",   "PAK", "WK",   9.0, "Right-hand bat",  "N/A"),
        Player("p_pak_p4","Shadab Khan",       "PAK", "AR",   8.5, "Right-hand bat",  "Right-arm leg break"),
        Player("p_pak_p5","Fakhar Zaman",      "PAK", "BAT",  8.5, "Left-hand bat",   "N/A"),
        Player("p_pak_p6","Haris Rauf",        "PAK", "BOWL", 8.5, "Right-hand bat",  "Right-arm fast"),
        Player("p_pak_p7","Naseem Shah",       "PAK", "BOWL", 8.0, "Right-hand bat",  "Right-arm fast"),
        Player("p_mix_8", "Imad Wasim",        "PAK", "AR",   7.5, "Left-hand bat",   "Slow left-arm orthodox")
    )
}