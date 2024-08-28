package com.geekstudio.cronettest

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geekstudio.cronettest.ui.theme.CronetTestTheme
import com.google.net.cronet.okhttptransport.CronetCallFactory
import com.google.net.cronet.okhttptransport.CronetInterceptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.chromium.net.CronetEngine
import org.chromium.net.CronetException
import org.chromium.net.UrlRequest
import org.chromium.net.UrlResponseInfo
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import java.nio.ByteBuffer
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class OkhttpAndCronetActivity : ComponentActivity() {
    private val TAG = "OkhttpAndCronetActivity"
    private lateinit var engine: CronetEngine
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var okhttpRetrofit: Retrofit
    private lateinit var callFactory: Call.Factory
    private lateinit var callFactoryRetrofit: Retrofit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        engine = CronetEngine.Builder(this)
            .setStoragePath(cacheDir.absolutePath)
            .enableHttpCache(CronetEngine.Builder.HTTP_CACHE_DISK, 10 * 1024 * 1024)
            .build()

        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
            .addInterceptor(CronetInterceptor.newBuilder(engine).build())
            .build()

        callFactory = CronetCallFactory.newBuilder(engine).build()

        okhttpRetrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(okHttpClient)
            .build()

        callFactoryRetrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .callFactory(callFactory)
            .build()

        enableEdgeToEdge()
        setContent {
            CronetTestTheme {
                Column {
                    Spacer(modifier = Modifier.fillMaxWidth().height(100.dp))
                    Button(onClick = { requestSimpleCallFactory() }) {
                        Text(text = "requestSimpleCallFactory 테스트")
                    }
                    Button(onClick = { requestInterceptorOkHttpAndCronet() }) {
                        Text(text = "requestInterceptorOkHttpAndCronet 테스트")
                    }
                    Button(onClick = { requestCallFactoryOkHttpAndCronet() }) {
                        Text(text = "requestCallFactoryOkHttpAndCronet 테스트")
                    }
                }
            }
        }
    }

    interface Api {
        @GET("/")
        suspend fun getSite(): String
    }

    private fun requestInterceptorOkHttpAndCronet() {
        cacheDir.list()?.forEach { name ->
            Log.d("MainActivity", "before cacheDir list name = $name")
        }

        CoroutineScope(Dispatchers.Main).launch {
            kotlin.runCatching {
                okhttpRetrofit.create(Api::class.java).getSite()
            }.onSuccess { response ->
                cacheDir.list()?.forEach { name ->
                    Log.d("MainActivity", "after cacheDir list name = $name")
                }

                Log.d("MainActivity", "retrofit response message = $response")
            }.onFailure { e ->
                Log.d("MainActivity", "retrofit e = $e")
            }
        }
    }

    private fun requestCallFactoryOkHttpAndCronet() {
        cacheDir.list()?.forEach { name ->
            Log.d("MainActivity", "before cacheDir list name = $name")
        }

        CoroutineScope(Dispatchers.Main).launch {
            kotlin.runCatching {
                callFactoryRetrofit.create(Api::class.java).getSite()
            }.onSuccess { response ->
                cacheDir.list()?.forEach { name ->
                    Log.d("MainActivity", "after cacheDir list name = $name")
                }

                Log.d("MainActivity", "retrofit response message = $response")
            }.onFailure { e ->
                Log.d("MainActivity", "retrofit e = $e")
            }
        }
    }

    private fun requestSimpleCallFactory() {
        CoroutineScope(Dispatchers.Main).launch {
            val request = Request.Builder()
                .url("https://www.google.com")
                .build()
            kotlin.runCatching {
                callFactory.newCall(request).execute()
            }.onSuccess { response ->
                Log.d("MainActivity", "callFactory response message = ${response.body}")
            }.onFailure { e ->
                Log.d("MainActivity", "callFactory e = $e")
            }
        }
    }
}