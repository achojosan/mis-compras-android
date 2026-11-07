package app.miscompras.com.di

import app.miscompras.com.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Cliente HTTP compartido. Sin interceptor de logs a propósito: las peticiones llevan
     * la ubicación del usuario y no debe acabar en logcat. El User-Agent es genérico
     * (Overpass y Open Food Facts piden identificar la app, no a la persona).
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", "MisCompras/${BuildConfig.VERSION_NAME} (Android)")
                    .build()
            )
        }
        .build()

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
}
