package app.miscompras.com.recognition

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imageembedder.ImageEmbedder
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Convierte una imagen en un vector de características (MobileNet V3, en el dispositivo).
 * El embedder de MediaPipe no es seguro entre hilos, por eso las llamadas se serializan.
 */
@Singleton
class ItemEmbedder @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val embedder: ImageEmbedder by lazy {
        val options = ImageEmbedder.ImageEmbedderOptions.builder()
            .setBaseOptions(BaseOptions.builder().setModelAssetPath(MODEL_ASSET).build())
            .setRunningMode(RunningMode.IMAGE)
            .setL2Normalize(true)
            .build()
        ImageEmbedder.createFromOptions(context, options)
    }

    @Synchronized
    fun embed(bitmap: Bitmap): FloatArray {
        val result = embedder.embed(BitmapImageBuilder(bitmap).build())
        return result.embeddingResult().embeddings()[0].floatEmbedding()
    }

    private companion object {
        const val MODEL_ASSET = "mobilenet_v3_small.tflite"
    }
}
