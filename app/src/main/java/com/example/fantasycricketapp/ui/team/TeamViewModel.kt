package com.example.fantasycricketapp.ui.team



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fantasycricketapp.data.model.Player
import com.example.fantasycricketapp.repository.PlayerApiRepository
import com.example.fantasycricketapp.utils.ApiResult
import kotlinx.coroutines.launch

/**
 * TeamViewModel — Manages player squad data for TeamSelectionActivity.
 *
 * Fetches real squad from CricAPI for the given matchId.
 * Falls back to dummy data automatically via PlayerApiRepository.
 */
class TeamViewModel : ViewModel() {

    private val repository = PlayerApiRepository()

    private val _squadState = MutableLiveData<ApiResult<List<Player>>>()
    val squadState: LiveData<ApiResult<List<Player>>> = _squadState

    private var allPlayers: List<Player> = emptyList()

    /**
     * Loads the squad for a specific match.
     * @param matchId The real CricAPI match ID
     */
    fun loadSquad(matchId: String) {
        _squadState.value = ApiResult.Loading

        viewModelScope.launch {
            val result = repository.getSquadForMatch(matchId)
            if (result is ApiResult.Success) {
                allPlayers = result.data
            }
            _squadState.postValue(result)
        }
    }

    /**
     * Filters the player list by role.
     * Called when user taps role filter tabs.
     */
    fun filterByRole(role: String): List<Player> {
        return when (role) {
            "BAT"  -> allPlayers.filter { it.role == "BAT" }
            "BOWL" -> allPlayers.filter { it.role == "BOWL" }
            "AR"   -> allPlayers.filter { it.role == "AR" }
            "WK"   -> allPlayers.filter { it.role == "WK" }
            else   -> allPlayers
        }
    }

    fun getAllPlayers() = allPlayers
}