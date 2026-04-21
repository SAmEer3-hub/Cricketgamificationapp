package com.example.fantasycricketapp.utils


/**
 * ApiConstants — Central place for all CricAPI configuration.
 *
 * ⚠️ IMPORTANT FOR COLLEGE PROJECT:
 * For a real production app, API keys should NEVER be in source code.
 * They should be in gradle.properties (excluded from git) or a backend server.
 * For this college project, keeping it here is acceptable.
 *
 * CricAPI Documentation: https://cricapi.com/how-to-use/
 *
 * Free Tier Limits:
 *   - 100 API calls per day
 *   - Reset at midnight UTC
 *   - Check usage at cricapi.com dashboard
 */
object ApiConstants {

    // ── PASTE YOUR CRICAPI KEY HERE ──
    const val API_KEY = "4d56824a-8174-4657-af15-f7b7d37de07c"

    // ── Base URL ──
    const val BASE_URL = "https://api.cricapi.com/v1/"

    // ── Endpoints ──
    const val ENDPOINT_CURRENT_MATCHES = "currentMatches"
    const val ENDPOINT_MATCHES         = "matches"
    const val ENDPOINT_MATCH_INFO      = "match_info"
    const val ENDPOINT_MATCH_SQUAD     = "match_squad"
    const val ENDPOINT_SERIES_LIST     = "series"
    const val ENDPOINT_PLAYERS         = "players"

    // ── Pagination ──
    const val DEFAULT_OFFSET = 0
    const val DEFAULT_LIMIT  = 25

    // ── Timeouts (seconds) ──
    const val CONNECT_TIMEOUT = 15L
    const val READ_TIMEOUT    = 30L
    const val WRITE_TIMEOUT   = 15L

    // ── Cache ──
    // How long to cache API responses (in milliseconds)
    // Matches: 5 minutes (they don't change that fast)
    // Squads: 30 minutes (stable data)
    const val MATCH_CACHE_DURATION  = 5 * 60 * 1000L
    const val SQUAD_CACHE_DURATION  = 30 * 60 * 1000L
}