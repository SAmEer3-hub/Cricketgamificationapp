package com.example.fantasycricketapp.utils


/**
 * ApiResult — A sealed class that wraps every API call result.
 *
 * Why sealed class?
 * Instead of using callbacks with onSuccess/onFailure lambdas everywhere,
 * sealed classes let us use 'when' expressions that the compiler
 * ensures are exhaustive — no case can be missed accidentally.
 *
 * Usage in Repository:
 *   return ApiResult.Success(data)
 *   return ApiResult.Error("Something went wrong")
 *   return ApiResult.Loading
 *
 * Usage in Activity/ViewModel:
 *   when (result) {
 *       is ApiResult.Success -> showData(result.data)
 *       is ApiResult.Error   -> showError(result.message)
 *       is ApiResult.Loading -> showSpinner()
 *   }
 */
sealed class ApiResult<out T> {

    /**
     * Successful response — contains the parsed data.
     */
    data class Success<T>(val data: T) : ApiResult<T>()

    /**
     * Failed response — contains a human-readable error message.
     *
     * @param message  What went wrong (shown to user)
     * @param cause    Original exception (for logging, not shown to user)
     * @param isNetworkError  True if the device has no internet connection
     * @param isApiLimitError True if the 100/day free tier limit was hit
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null,
        val isNetworkError: Boolean = false,
        val isApiLimitError: Boolean = false
    ) : ApiResult<Nothing>()

    /**
     * In-progress state — shown while the API call is running.
     */
    object Loading : ApiResult<Nothing>()

    // ── Convenience helpers ──

    val isSuccess get() = this is Success
    val isError   get() = this is Error
    val isLoading get() = this is Loading

    fun getOrNull(): T? = if (this is Success) data else null
}