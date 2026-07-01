package app.miscompras.com.data.remote.osm

import retrofit2.http.GET
import retrofit2.http.Query

interface OverpassApiService {
    @GET("api/interpreter")
    suspend fun getNearbySupermarkets(
        @Query("data") query: String
    ): OsmResponse
}

object OverpassQueryBuilder {
    fun buildNearbySupermarketsQuery(lat: Double, lon: Double, radiusMeters: Int = 10000): String {
        return """
            [out:json][timeout:25];
            (
              node["shop"~"supermarket|convenience|frozen_food|greengrocer|butcher|bakery"](around:$radiusMeters,$lat,$lon);
              way["shop"~"supermarket|convenience|frozen_food|greengrocer|butcher|bakery"](around:$radiusMeters,$lat,$lon);
              relation["shop"~"supermarket|convenience|frozen_food|greengrocer|butcher|bakery"](around:$radiusMeters,$lat,$lon);
            );
            out center;
        """.trimIndent()
    }
}
