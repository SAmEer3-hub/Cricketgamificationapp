package com.example.fantasycricketapp.utils



import com.example.fantasycricketapp.data.model.PlayerPoints


object MockPointsData {

    fun getPointsForMatch(matchId: String): List<PlayerPoints> {
        return when (matchId) {
            "match_001" -> getMatch001Points()   // IND vs AUS
            "match_002" -> getMatch002Points()   // ENG vs NZ
            "match_003" -> getMatch003Points()   // PAK vs SA
            "match_004" -> getMatch004Points()   // WI vs SL
            "match_005" -> getMatch005Points()   // IND vs BAN
            "match_006" -> getMatch006Points()   // AUS vs NZ
            "match_007" -> getMatch007Points()   // SA vs ENG
            "match_008" -> getMatch008Points()   // IND vs PAK
            else        -> getMatch001Points()
        }
    }


    private fun calcPoints(
        runs: Int = 0, fours: Int = 0, sixes: Int = 0, isDuck: Boolean = false,
        wickets: Int = 0, maidens: Int = 0,
        catches: Int = 0, stumpings: Int = 0, runOuts: Int = 0
    ): Int {
        var pts = 0

        // ── Batting ──
        pts += runs                         // 1 pt per run
        pts += fours                        // +1 per boundary
        pts += sixes * 2                    // +2 per six
        if (runs >= 100) pts += 16          // Century bonus
        else if (runs >= 50) pts += 8       // Half-century bonus
        if (isDuck && runs == 0) pts -= 2   // Duck penalty

        // ── Bowling ──
        pts += wickets * 25                 // 25 pts per wicket
        pts += maidens * 8                  // 8 pts per maiden
        if (wickets >= 5) pts += 16         // 5-wicket haul bonus
        else if (wickets >= 4) pts += 8     // 4-wicket bonus

        // ── Fielding ──
        pts += catches * 8                  // 8 pts per catch
        pts += stumpings * 12              // 12 pts per stumping
        pts += runOuts * 6                  // 6 pts per run-out

        return pts
    }

    // ──────────────────────────────────────────────────────────
    // MATCH 001 — India vs Australia (LIVE match)
    // ──────────────────────────────────────────────────────────
    private fun getMatch001Points(): List<PlayerPoints> = listOf(

        // INDIA
        makePoints("match_001", "p_ind_1",  "Virat Kohli",     "IND", "BAT",
            runs=82, ballsFaced=61, fours=7, sixes=2,
            catches=1
        ),
        makePoints("match_001", "p_ind_2",  "Rohit Sharma",    "IND", "BAT",
            runs=54, ballsFaced=38, fours=5, sixes=3
        ),
        makePoints("match_001", "p_ind_3",  "KL Rahul",        "IND", "WK",
            runs=31, ballsFaced=28, fours=3,
            stumpings=1, catches=1
        ),
        makePoints("match_001", "p_ind_4",  "Hardik Pandya",   "IND", "AR",
            runs=29, ballsFaced=16, fours=2, sixes=2,
            wickets=2, maidens=0, catches=1
        ),
        makePoints("match_001", "p_ind_5",  "Ravindra Jadeja", "IND", "AR",
            runs=18, ballsFaced=14, fours=1,
            wickets=2, maidens=1, runOuts=1
        ),
        makePoints("match_001", "p_ind_6",  "Jasprit Bumrah",  "IND", "BOWL",
            wickets=3, maidens=1, catches=0
        ),
        makePoints("match_001", "p_ind_7",  "Mohammed Shami",  "IND", "BOWL",
            wickets=2, maidens=0
        ),

        // AUSTRALIA
        makePoints("match_001", "p_aus_1",  "Steve Smith",     "AUS", "BAT",
            runs=47, ballsFaced=42, fours=4, sixes=1, catches=1
        ),
        makePoints("match_001", "p_aus_2",  "David Warner",    "AUS", "BAT",
            runs=6, ballsFaced=9, isDuck=false, catches=1
        ),
        makePoints("match_001", "p_aus_3",  "Pat Cummins",     "AUS", "BOWL",
            wickets=3, maidens=1
        ),
        makePoints("match_001", "p_aus_4",  "Mitchell Starc",  "AUS", "BOWL",
            wickets=2, maidens=0, runs=8, fours=1
        ),
        makePoints("match_001", "p_aus_5",  "Glenn Maxwell",   "AUS", "AR",
            runs=38, ballsFaced=22, fours=2, sixes=3,
            wickets=1
        ),
        makePoints("match_001", "p_aus_6",  "Alex Carey",      "AUS", "WK",
            runs=22, ballsFaced=20, fours=2,
            catches=2, stumpings=1
        ),
        makePoints("match_001", "p_aus_7",  "Josh Hazlewood",  "AUS", "BOWL",
            wickets=1, maidens=0
        ),
        makePoints("match_001", "p_mix_1",  "Axar Patel",      "IND", "AR",
            runs=14, ballsFaced=10, fours=1,
            wickets=1, maidens=1
        )
    )


    // MATCH 002 — England vs New Zealand (UPCOMING)

    private fun getMatch002Points(): List<PlayerPoints> = listOf(
        makePoints("match_002", "p_eng_1", "Joe Root",         "ENG", "BAT",
            runs=91, ballsFaced=98, fours=9, sixes=1, catches=1),
        makePoints("match_002", "p_eng_2", "Ben Stokes",       "ENG", "AR",
            runs=56, ballsFaced=44, fours=4, sixes=2, wickets=2, catches=1),
        makePoints("match_002", "p_eng_3", "Jos Buttler",      "ENG", "WK",
            runs=43, ballsFaced=31, fours=3, sixes=2, catches=2, stumpings=1),
        makePoints("match_002", "p_eng_4", "Jofra Archer",     "ENG", "BOWL",
            wickets=3, maidens=1),
        makePoints("match_002", "p_eng_5", "Jonny Bairstow",   "ENG", "BAT",
            runs=28, ballsFaced=24, fours=3),
        makePoints("match_002", "p_eng_6", "Stuart Broad",     "ENG", "BOWL",
            wickets=2, maidens=0),
        makePoints("match_002", "p_eng_7", "Moeen Ali",        "ENG", "AR",
            runs=19, ballsFaced=14, fours=1, sixes=1, wickets=1),
        makePoints("match_002", "p_nz_1",  "Kane Williamson",  "NZ",  "BAT",
            runs=77, ballsFaced=84, fours=6, sixes=1, catches=1),
        makePoints("match_002", "p_nz_2",  "Trent Boult",      "NZ",  "BOWL",
            wickets=4, maidens=1),
        makePoints("match_002", "p_nz_3",  "Tim Southee",      "NZ",  "BOWL",
            wickets=2, maidens=0),
        makePoints("match_002", "p_nz_4",  "Devon Conway",     "NZ",  "WK",
            runs=52, ballsFaced=48, fours=5, catches=1, stumpings=1),
        makePoints("match_002", "p_nz_5",  "Martin Guptill",   "NZ",  "BAT",
            runs=14, ballsFaced=11, fours=1),
        makePoints("match_002", "p_nz_6",  "Mitchell Santner", "NZ",  "AR",
            runs=12, ballsFaced=10, wickets=1, maidens=1),
        makePoints("match_002", "p_nz_7",  "Lockie Ferguson",  "NZ",  "BOWL",
            wickets=2, maidens=0),
        makePoints("match_002", "p_mix_2", "Mark Wood",        "ENG", "BOWL",
            wickets=1, maidens=0)
    )


    // MATCH 003 — Pakistan vs South Africa

    private fun getMatch003Points(): List<PlayerPoints> = listOf(
        makePoints("match_003", "p_pak_1", "Babar Azam",       "PAK", "BAT",
            runs=103, ballsFaced=88, fours=9, sixes=3),
        makePoints("match_003", "p_pak_2", "Shaheen Afridi",   "PAK", "BOWL",
            wickets=4, maidens=1),
        makePoints("match_003", "p_pak_3", "Mohammad Rizwan",  "PAK", "WK",
            runs=61, ballsFaced=49, fours=5, sixes=2, catches=2, stumpings=1),
        makePoints("match_003", "p_pak_4", "Shadab Khan",      "PAK", "AR",
            runs=22, ballsFaced=17, fours=2, wickets=2, maidens=0),
        makePoints("match_003", "p_pak_5", "Fakhar Zaman",     "PAK", "BAT",
            runs=39, ballsFaced=32, fours=4, sixes=1),
        makePoints("match_003", "p_pak_6", "Haris Rauf",       "PAK", "BOWL",
            wickets=2, maidens=0),
        makePoints("match_003", "p_pak_7", "Imad Wasim",       "PAK", "AR",
            runs=15, ballsFaced=12, wickets=1, maidens=1),
        makePoints("match_003", "p_sa_1",  "Quinton de Kock",  "SA",  "WK",
            runs=58, ballsFaced=43, fours=6, sixes=2, catches=2),
        makePoints("match_003", "p_sa_2",  "Kagiso Rabada",    "SA",  "BOWL",
            wickets=3, maidens=0),
        makePoints("match_003", "p_sa_3",  "Faf du Plessis",   "SA",  "BAT",
            runs=44, ballsFaced=38, fours=4, sixes=1),
        makePoints("match_003", "p_sa_4",  "Anrich Nortje",    "SA",  "BOWL",
            wickets=2, maidens=1),
        makePoints("match_003", "p_sa_5",  "David Miller",     "SA",  "BAT",
            runs=31, ballsFaced=20, fours=2, sixes=2),
        makePoints("match_003", "p_sa_6",  "Keshav Maharaj",   "SA",  "BOWL",
            wickets=1, maidens=1),
        makePoints("match_003", "p_sa_7",  "Temba Bavuma",     "SA",  "BAT",
            runs=27, ballsFaced=25, fours=2),
        makePoints("match_003", "p_mix_3", "Tabraiz Shamsi",   "SA",  "BOWL",
            wickets=2, maidens=0)
    )

    // ──────────────────────────────────────────────────────────
    // MATCH 004 — West Indies vs Sri Lanka
    // ──────────────────────────────────────────────────────────
    private fun getMatch004Points(): List<PlayerPoints> = listOf(
        makePoints("match_004", "p_wi_1",  "Kieron Pollard",   "WI",  "AR",
            runs=48, ballsFaced=28, fours=3, sixes=4, wickets=1),
        makePoints("match_004", "p_wi_2",  "Nicholas Pooran",  "WI",  "WK",
            runs=67, ballsFaced=38, fours=5, sixes=5, catches=1, stumpings=1),
        makePoints("match_004", "p_wi_3",  "Andre Russell",    "WI",  "AR",
            runs=54, ballsFaced=22, fours=3, sixes=6, wickets=2),
        makePoints("match_004", "p_wi_4",  "Jason Holder",     "WI",  "AR",
            runs=18, ballsFaced=14, wickets=2, maidens=1),
        makePoints("match_004", "p_wi_5",  "Chris Gayle",      "WI",  "BAT",
            runs=35, ballsFaced=27, fours=2, sixes=3),
        makePoints("match_004", "p_wi_6",  "Shimron Hetmyer",  "WI",  "BAT",
            runs=22, ballsFaced=15, fours=1, sixes=2),
        makePoints("match_004", "p_wi_7",  "Alzarri Joseph",   "WI",  "BOWL",
            wickets=3, maidens=0),
        makePoints("match_004", "p_sl_1",  "Wanindu Hasaranga","SL",  "AR",
            runs=21, ballsFaced=16, wickets=3, maidens=0),
        makePoints("match_004", "p_sl_2",  "Dasun Shanaka",    "SL",  "AR",
            runs=38, ballsFaced=26, fours=3, sixes=2, wickets=1),
        makePoints("match_004", "p_sl_3",  "Pathum Nissanka",  "SL",  "BAT",
            runs=51, ballsFaced=46, fours=5, sixes=1),
        makePoints("match_004", "p_sl_4",  "Dushmantha Chameera","SL","BOWL",
            wickets=2, maidens=0),
        makePoints("match_004", "p_sl_5",  "Kusal Mendis",     "SL",  "WK",
            runs=29, ballsFaced=22, fours=2, catches=2),
        makePoints("match_004", "p_sl_6",  "Maheesh Theekshana","SL", "BOWL",
            wickets=1, maidens=1),
        makePoints("match_004", "p_sl_7",  "Charith Asalanka", "SL",  "BAT",
            runs=18, ballsFaced=14, fours=2),
        makePoints("match_004", "p_mix_4", "Romario Shepherd", "WI",  "AR",
            runs=12, ballsFaced=8, wickets=1)
    )

    // ──────────────────────────────────────────────────────────
    // MATCH 005 — India vs Bangladesh (COMPLETED)
    // ──────────────────────────────────────────────────────────
    private fun getMatch005Points(): List<PlayerPoints> = listOf(
        makePoints("match_005", "p_ind_b1","Shikhar Dhawan",   "IND", "BAT",
            runs=72, ballsFaced=64, fours=8, sixes=1),
        makePoints("match_005", "p_ind_b2","Ishan Kishan",     "IND", "WK",
            runs=44, ballsFaced=33, fours=4, sixes=2, catches=2, stumpings=1),
        makePoints("match_005", "p_ind_b3","Suryakumar Yadav", "IND", "BAT",
            runs=68, ballsFaced=36, fours=6, sixes=5),
        makePoints("match_005", "p_ind_b4","Deepak Chahar",    "IND", "BOWL",
            wickets=3, maidens=1),
        makePoints("match_005", "p_ind_b5","Kuldeep Yadav",    "IND", "BOWL",
            wickets=3, maidens=0),
        makePoints("match_005", "p_ind_b6","Washington Sundar","IND", "AR",
            runs=16, ballsFaced=13, wickets=1, maidens=1),
        makePoints("match_005", "p_ind_b7","Shreyas Iyer",     "IND", "BAT",
            runs=53, ballsFaced=45, fours=5, sixes=2),
        makePoints("match_005", "p_ban_1", "Shakib Al Hasan",  "BAN", "AR",
            runs=48, ballsFaced=42, fours=4, sixes=1, wickets=2),
        makePoints("match_005", "p_ban_2", "Mushfiqur Rahim",  "BAN", "WK",
            runs=35, ballsFaced=32, fours=3, catches=1, stumpings=1),
        makePoints("match_005", "p_ban_3", "Tamim Iqbal",      "BAN", "BAT",
            runs=29, ballsFaced=27, fours=3),
        makePoints("match_005", "p_ban_4", "Mustafizur Rahman","BAN", "BOWL",
            wickets=2, maidens=0),
        makePoints("match_005", "p_ban_5", "Mahmudullah",      "BAN", "AR",
            runs=22, ballsFaced=18, fours=2, wickets=1),
        makePoints("match_005", "p_ban_6", "Liton Das",        "BAN", "BAT",
            runs=14, ballsFaced=12, fours=1),
        makePoints("match_005", "p_ban_7", "Taskin Ahmed",     "BAN", "BOWL",
            wickets=1, maidens=0),
        makePoints("match_005", "p_mix_5", "Mehidy Hasan",     "BAN", "AR",
            runs=11, ballsFaced=9, wickets=2, maidens=1)
    )


    // MATCH 006 — Australia vs New Zealand (COMPLETED)

    private fun getMatch006Points(): List<PlayerPoints> = listOf(
        makePoints("match_006", "p_aus_b1","Travis Head",      "AUS", "BAT",
            runs=88, ballsFaced=72, fours=9, sixes=3),
        makePoints("match_006", "p_aus_b2","Marnus Labuschagne","AUS","BAT",
            runs=106, ballsFaced=118, fours=10, sixes=2, catches=1),
        makePoints("match_006", "p_aus_b3","Matthew Wade",     "AUS", "WK",
            runs=24, ballsFaced=21, fours=2, catches=2, stumpings=1),
        makePoints("match_006", "p_aus_b4","Nathan Lyon",      "AUS", "BOWL",
            wickets=4, maidens=2),
        makePoints("match_006", "p_aus_b5","Cameron Green",    "AUS", "AR",
            runs=41, ballsFaced=36, fours=4, wickets=1),
        makePoints("match_006", "p_aus_b6","Aaron Finch",      "AUS", "BAT",
            runs=33, ballsFaced=28, fours=3, sixes=1),
        makePoints("match_006", "p_aus_b7","Adam Zampa",       "AUS", "BOWL",
            wickets=3, maidens=0),
        makePoints("match_006", "p_nz_b1", "Ross Taylor",      "NZ",  "BAT",
            runs=62, ballsFaced=58, fours=6, sixes=1, catches=1),
        makePoints("match_006", "p_nz_b2", "Tom Latham",       "NZ",  "WK",
            runs=38, ballsFaced=34, fours=3, catches=1, stumpings=1),
        makePoints("match_006", "p_nz_b3", "Kyle Jamieson",    "NZ",  "AR",
            wickets=3, maidens=1, runs=14, fours=1),
        makePoints("match_006", "p_nz_b4", "Ish Sodhi",        "NZ",  "BOWL",
            wickets=2, maidens=0),
        makePoints("match_006", "p_nz_b5", "Henry Nicholls",   "NZ",  "BAT",
            runs=29, ballsFaced=31, fours=3),
        makePoints("match_006", "p_nz_b6", "Matt Henry",       "NZ",  "BOWL",
            wickets=2, maidens=1),
        makePoints("match_006", "p_nz_b7", "Glenn Phillips",   "NZ",  "AR",
            runs=44, ballsFaced=29, fours=3, sixes=3, wickets=1),
        makePoints("match_006", "p_mix_6", "Michael Bracewell","NZ",  "AR",
            runs=19, ballsFaced=16, wickets=1)
    )


    // MATCH 007 — South Africa vs England

    private fun getMatch007Points(): List<PlayerPoints> = listOf(
        makePoints("match_007", "p_sa_e1", "Aiden Markram",    "SA",  "BAT",
            runs=65, ballsFaced=54, fours=6, sixes=2),
        makePoints("match_007", "p_sa_e2", "Rassie van der Dussen","SA","BAT",
            runs=48, ballsFaced=41, fours=4, sixes=1, catches=1),
        makePoints("match_007", "p_sa_e3", "Marco Jansen",     "SA",  "AR",
            wickets=3, maidens=0, runs=22, fours=2),
        makePoints("match_007", "p_sa_e4", "Gerald Coetzee",   "SA",  "BOWL",
            wickets=2, maidens=1),
        makePoints("match_007", "p_sa_e5", "Ryan Rickelton",   "SA",  "WK",
            runs=31, ballsFaced=26, fours=3, catches=2, stumpings=1),
        makePoints("match_007", "p_sa_e6", "Wiaan Mulder",     "SA",  "AR",
            runs=18, ballsFaced=14, wickets=1),
        makePoints("match_007", "p_sa_e7", "Lungi Ngidi",      "SA",  "BOWL",
            wickets=2, maidens=0),
        makePoints("match_007", "p_eng_e1","Harry Brook",      "ENG", "BAT",
            runs=94, ballsFaced=79, fours=9, sixes=3),
        makePoints("match_007", "p_eng_e2","Zak Crawley",      "ENG", "BAT",
            runs=41, ballsFaced=36, fours=5),
        makePoints("match_007", "p_eng_e3","Ollie Pope",       "ENG", "BAT",
            runs=55, ballsFaced=48, fours=5, sixes=1),
        makePoints("match_007", "p_eng_e4","Gus Atkinson",     "ENG", "BOWL",
            wickets=3, maidens=1),
        makePoints("match_007", "p_eng_e5","Ollie Robinson",   "ENG", "BOWL",
            wickets=2, maidens=0),
        makePoints("match_007", "p_eng_e6","Ben Duckett",      "ENG", "BAT",
            runs=27, ballsFaced=21, fours=3, sixes=1),
        makePoints("match_007", "p_eng_e7","Jamie Overton",    "ENG", "AR",
            runs=19, ballsFaced=14, wickets=1),
        makePoints("match_007", "p_mix_7", "Sam Curran",       "ENG", "AR",
            runs=33, ballsFaced=22, fours=2, sixes=2, wickets=2)
    )


    // MATCH 008 — India vs Pakistan (THE BIGGEST GAME)

    private fun getMatch008Points(): List<PlayerPoints> = listOf(
        makePoints("match_008", "p_ind_p1","Virat Kohli",      "IND", "BAT",
            runs=122, ballsFaced=97, fours=11, sixes=4),
        makePoints("match_008", "p_ind_p2","Rohit Sharma",     "IND", "BAT",
            runs=47, ballsFaced=36, fours=5, sixes=2),
        makePoints("match_008", "p_ind_p3","KL Rahul",         "IND", "WK",
            runs=39, ballsFaced=33, fours=3, catches=2, stumpings=1),
        makePoints("match_008", "p_ind_p4","Hardik Pandya",    "IND", "AR",
            runs=31, ballsFaced=19, fours=2, sixes=2, wickets=2),
        makePoints("match_008", "p_ind_p5","Jasprit Bumrah",   "IND", "BOWL",
            wickets=4, maidens=1),
        makePoints("match_008", "p_ind_p6","Ravindra Jadeja",  "IND", "AR",
            runs=15, ballsFaced=12, wickets=1, maidens=1, runOuts=1),
        makePoints("match_008", "p_ind_p7","Suryakumar Yadav", "IND", "BAT",
            runs=55, ballsFaced=29, fours=4, sixes=5),
        makePoints("match_008", "p_pak_p1","Babar Azam",       "PAK", "BAT",
            runs=68, ballsFaced=61, fours=6, sixes=2),
        makePoints("match_008", "p_pak_p2","Shaheen Afridi",   "PAK", "BOWL",
            wickets=3, maidens=0),
        makePoints("match_008", "p_pak_p3","Mohammad Rizwan",  "PAK", "WK",
            runs=44, ballsFaced=38, fours=4, catches=1, stumpings=1),
        makePoints("match_008", "p_pak_p4","Shadab Khan",      "PAK", "AR",
            runs=18, ballsFaced=14, wickets=2),
        makePoints("match_008", "p_pak_p5","Fakhar Zaman",     "PAK", "BAT",
            runs=29, ballsFaced=24, fours=3, sixes=1),
        makePoints("match_008", "p_pak_p6","Haris Rauf",       "PAK", "BOWL",
            wickets=2, maidens=0),
        makePoints("match_008", "p_pak_p7","Naseem Shah",      "PAK", "BOWL",
            wickets=1, maidens=1),
        makePoints("match_008", "p_mix_8", "Imad Wasim",       "PAK", "AR",
            runs=13, ballsFaced=10, wickets=1, maidens=1)
    )


    // FACTORY HELPER — builds a PlayerPoints with auto total

    private fun makePoints(
        matchId: String,
        playerId: String,
        playerName: String,
        team: String,
        role: String,
        runs: Int = 0,
        ballsFaced: Int = 0,
        fours: Int = 0,
        sixes: Int = 0,
        isDuck: Boolean = false,
        wickets: Int = 0,
        maidens: Int = 0,
        catches: Int = 0,
        stumpings: Int = 0,
        runOuts: Int = 0
    ): PlayerPoints {
        val total = calcPoints(
            runs = runs, fours = fours, sixes = sixes, isDuck = isDuck,
            wickets = wickets, maidens = maidens,
            catches = catches, stumpings = stumpings, runOuts = runOuts
        )
        return PlayerPoints(
            pointsId     = "${matchId}_${playerId}",
            matchId      = matchId,
            playerId     = playerId,
            playerName   = playerName,
            team         = team,
            role         = role,
            runsScored   = runs,
            ballsFaced   = ballsFaced,
            fours        = fours,
            sixes        = sixes,
            isDuck       = isDuck,
            wicketsTaken = wickets,
            maidenOvers  = maidens,
            catches      = catches,
            stumpings    = stumpings,
            runOuts      = runOuts,
            totalPoints  = total
        )
    }
}