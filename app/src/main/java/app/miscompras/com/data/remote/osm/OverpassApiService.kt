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
    fun buildNearbySupermarketsQuery(lat: Double, lon: Double, radiusMeters: Int = 5000): String {
        return """
            [out:json][timeout:25];
            (
              node["shop"="supermarket"](around:$radiusMeters,$lat,$lon);
              way["shop"="supermarket"](around:$radiusMeters,$lat,$lon);
              relation["shop"="supermarket"](around:$radiusMeters,$lat,$lon);
              node["shop"="convenience"](around:$radiusMeters,$lat,$lon);
              way["shop"="convenience"](around:$radiusMeters,$lat,$lon);
            );
            out center;
        """.trimIndent()
    }
}
