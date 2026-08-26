package com.openlauncher.app.ui.map

import com.here.sdk.core.GeoCoordinates
import com.here.sdk.core.LanguageCode
import com.here.sdk.core.errors.InstantiationErrorException
import com.here.sdk.search.SearchCallback
import com.here.sdk.search.SearchEngine
import com.here.sdk.search.SearchError
import com.here.sdk.search.SearchOptions
import com.here.sdk.search.TextQuery

data class HereSearchResult(
    val title: String,
    val address: String,
    val coordinates: GeoCoordinates
)

class HereSearchController {

    private val searchEngine: SearchEngine =
        try {
            SearchEngine()
        } catch (e: InstantiationErrorException) {
            throw RuntimeException(
                "Failed to initialize SearchEngine: ${e.error.name}",
                e
            )
        }

    fun search(
        queryText: String,
        center: GeoCoordinates,
        onSuccess: (List<HereSearchResult>) -> Unit,
        onError: (SearchError) -> Unit
    ) {
        val query = TextQuery(
            queryText,
            TextQuery.Area(center)
        )

        val options = SearchOptions().apply {
            languageCode = LanguageCode.EN_GB
            maxItems = 8
        }

        searchEngine.searchByText(
            query,
            options,
            SearchCallback { searchError, places ->
                if (searchError != null) {
                    onError(searchError)
                    return@SearchCallback
                }

                val results =
                    places.orEmpty()
                        .mapNotNull { place ->
                            val coordinates =
                                place.geoCoordinates
                                    ?: return@mapNotNull null

                            HereSearchResult(
                                title = place.title,
                                address = place.address.addressText,
                                coordinates = coordinates
                            )
                        }

                onSuccess(results)
            }
        )
    }
}