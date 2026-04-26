package com.alvinfungai.providers

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvinfungai.providers.domain.model.ServiceProvider
import com.alvinfungai.providers.domain.usecase.GetServiceProvidersUseCase
import com.alvinfungai.reviews.domain.GetReviewsForProviderUseCase
import com.alvinfungai.reviews.domain.Review
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val getProvidersUseCase: GetServiceProvidersUseCase,
    private val getReviewsForProviderUseCase: GetReviewsForProviderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExploreUiState>(ExploreUiState.Loading)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _cameraPosition = MutableStateFlow(CameraPosition.fromLatLngZoom(
        LatLng(17.8276279, 31.0504518), 12f))
    val cameraPosition = _cameraPosition.asStateFlow()

    private var fetchJob: Job? = null

    init {
        Log.d("COWORK_DEBUG", "ExploreViewModel: init called")
        
        // Reactive search: whenever searchQuery or selectedCategory changes, fetch data
        combine(_searchQuery, _selectedCategory) { query, category ->
            Pair(query, category)
        }
            .debounce(500)
            .distinctUntilChanged()
            .onEach { (query, category) ->
                Log.d("COWORK_DEBUG", "ExploreViewModel: Triggering fetch via reactive flow. Query: $query, Category: $category")
                fetchProviders(category, query)
            }
            .launchIn(viewModelScope)
    }

    fun onSearchChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategoryChange(category: String) {
        _selectedCategory.value = category
    }

    fun getProviders(
        category: String, 
        query: String, 
        lat: Double? = null, 
        lng: Double? = null, 
        radiusKm: Double? = null
    ) {
        Log.d("COWORK_DEBUG", "ExploreViewModel: getProviders manual call. Query: $query, Category: $category")
        fetchProviders(category, query, lat, lng, radiusKm)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun fetchProviders(
        category: String, 
        query: String, 
        lat: Double? = null, 
        lng: Double? = null, 
        radiusKm: Double? = null
    ) {
        Log.d("COWORK_DEBUG", "ExploreViewModel: fetchProviders started")
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _uiState.value = ExploreUiState.Loading
            
            getProvidersUseCase(
                category = category, 
                query = query, 
                lat = lat, 
                lng = lng, 
                radiusKm = radiusKm
            )
                .flowOn(Dispatchers.IO)
                .flatMapLatest { result ->
                    result.fold(
                        onSuccess = { providers ->
                            if (providers.isEmpty()) {
                                flowOf(Result.success(emptyList<ServiceProvider>()))
                            } else {
                                // Aggregate review stats from Firestore for each provider
                                val providerFlows: List<Flow<ServiceProvider>> = providers.map { provider ->
                                    getReviewsForProviderUseCase(provider.userId).map { reviewsResult ->
                                        val reviews: List<Review> = reviewsResult.getOrDefault(emptyList())
                                        if (reviews.isNotEmpty()) {
                                            provider.copy(
                                                ratingAvg = reviews.map { it.rating }.average(),
                                                ratingCount = reviews.size
                                            )
                                        } else {
                                            provider
                                        }
                                    }
                                }
                                combine(providerFlows) { updatedProvidersArray ->
                                    Result.success(updatedProvidersArray.toList())
                                }
                            }
                        },
                        onFailure = { error -> flowOf(Result.failure(error)) }
                    )
                }
                .catch { error ->
                    Log.e("COWORK_DEBUG", "ExploreViewModel: Flow catch error: ${error.message}", error)
                    _uiState.value = ExploreUiState.Error(error.message ?: "Connection error")
                }
                .collect { result ->
                    Log.d("COWORK_DEBUG", "ExploreViewModel: Result collected. Success: ${result.isSuccess}")
                    result.onSuccess { providers ->
                        Log.d("COWORK_DEBUG", "ExploreViewModel: Emitting Success with ${providers.size} providers")
                        _uiState.value = ExploreUiState.Success(providers)

                        if (providers.isNotEmpty()) {
                            val firstProvider = providers.first()
                            if (firstProvider.latitude != 0.0) {
                                _cameraPosition.value = CameraPosition.fromLatLngZoom(
                                    LatLng(firstProvider.latitude, firstProvider.longitude),
                                    13f
                                )
                            }
                        }
                    }.onFailure { error ->
                        Log.e("COWORK_DEBUG", "ExploreViewModel: Result failure: ${error.message}", error)
                        _uiState.value = ExploreUiState.Error(error.message ?: "Unknown error")
                    }
                }
        }
    }
}

sealed interface ExploreUiState {
    data object Loading : ExploreUiState
    data class Success(val providers: List<ServiceProvider>) : ExploreUiState
    data class Error(val message: String) : ExploreUiState
}
