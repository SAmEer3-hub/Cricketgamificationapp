package com.example.fantasycricketapp.ui.dashboard


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fantasycricketapp.data.model.Match
import com.example.fantasycricketapp.repository.MatchApiRepository
import com.example.fantasycricketapp.utils.ApiResult
import kotlinx.coroutines.launch

/**
 * MatchViewModel — Manages match data for MainActivity.
 *
 * Why ViewModel?
 *   - Data survives screen rotation (activity recreation)
 *   - API call is not repeated on every configuration change
 *   - Separates data logic from UI logic (clean MVVM)
 *
 * LiveData:
 *   matchesState → observed by MainActivity to update UI
 */
class MatchViewModel : ViewModel() {

    private val repository = MatchApiRepository()

    // ── LiveData observed by MainActivity ──
    private val _matchesState = MutableLiveData<ApiResult<List<Match>>>()
    val matchesState: LiveData<ApiResult<List<Match>>> = _matchesState

    // ── Full unfiltered list (for tab filtering without re-fetching) ──
    private var allMatches: List<Match> = emptyList()

    init {
        // Load matches immediately when ViewModel is created
        loadMatches()
    }

    /**
     * Fetches matches from the API (or cache).
     * Posts Loading state first, then Success or Error.
     */
    fun loadMatches() {
        _matchesState.value = ApiResult.Loading

        // viewModelScope is automatically cancelled when ViewModel is cleared
        viewModelScope.launch {
            val result = repository.getMatches()
            when (result) {
                is ApiResult.Success -> {
                    allMatches = result.data
                }
                else -> { /* error handled by UI */ }
            }
            _matchesState.postValue(result)
        }
    }

    /**
     * Filters the already-loaded match list by status.
     * No new API call — just filters the in-memory list.
     */
    fun filterMatches(filter: String): List<Match> {
        return when (filter) {
            "LIVE"      -> allMatches.filter { it.status == "LIVE" }
            "UPCOMING"  -> allMatches.filter { it.status == "UPCOMING" }
            "COMPLETED" -> allMatches.filter { it.status == "COMPLETED" }
            else        -> allMatches
        }
    }

    /**
     * Forces a fresh API call bypassing the cache.
     * Called when user taps "Refresh".
     */
    fun refreshMatches() {
        repository.clearCache()
        loadMatches()
    }

    fun getAllMatches() = allMatches
}