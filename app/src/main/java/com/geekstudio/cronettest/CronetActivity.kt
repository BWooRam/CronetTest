package com.geekstudio.cronettest

import android.os.Build
import android.os.Bundle
import android.os.ext.SdkExtensions
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.geekstudio.cronettest.ui.theme.CronetTestTheme
import com.google.net.cronet.okhttptransport.CronetCallFactory
import com.google.net.cronet.okhttptransport.CronetInterceptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.OkHttpClient
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


class CronetActivity : ComponentActivity() {
    private val TAG = "CronetActivity"
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val urlRequestCallback = object : UrlRequest.Callback() {
        override fun onRedirectReceived(
            request: UrlRequest?,
            info: UrlResponseInfo?,
            newLocationUrl: String?
        ) {
            Log.d(TAG, "onRedirectReceived method called.")
            request?.followRedirect()
        }

        override fun onResponseStarted(request: UrlRequest?, info: UrlResponseInfo?) {
            Log.d(TAG, "onResponseStarted method called.")
            request?.read(ByteBuffer.allocateDirect(102400))
        }

        override fun onReadCompleted(
            request: UrlRequest?,
            info: UrlResponseInfo?,
            byteBuffer: ByteBuffer?
        ) {
            Log.d(TAG, "onReadCompleted method called.")
            val httpStatusCode = info?.httpStatusCode
            if (httpStatusCode == 200) {
                Log.d(TAG, "onReadCompleted method 200 called. before request = ${info.toString()}")
                /*request?.read(byteBuffer)
                Log.d(TAG, "onReadCompleted method 200 called. after request = ${info.toString()}")*/
            } else if (httpStatusCode == 503) {
                Log.d(TAG, "onReadCompleted method 503 called.")
                request?.read(byteBuffer)
            }
        }

        override fun onSucceeded(request: UrlRequest?, info: UrlResponseInfo?) {
            Log.d(TAG, "onSucceeded method called.")
        }

        override fun onFailed(
            request: UrlRequest?,
            info: UrlResponseInfo?,
            error: CronetException?
        ) {
            Log.d(TAG, "onFailed method called.")
        }
    }

    private val urls = listOf(
        "https://www.kakaocorp.com/page/service/service/KakaoTalk",
        "https://www.facebook.com/",
        "https://www.daum.net",
        "https://www.google.com",
        "https://www.whatsmydns.net/example-301-redirect")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CronetTestTheme {
                Column {
                    Spacer(modifier = Modifier.fillMaxWidth().height(100.dp))
                    Button(onClick = {
                        for(index in 0 .. 4){
                            val cronetEngine: CronetEngine = CronetEngine.Builder(this@CronetActivity).enableHttpCache(CronetEngine.Builder.HTTP_CACHE_IN_MEMORY, 10 * 1024 * 1024).build()
                            val requestBuilder = cronetEngine.newUrlRequestBuilder(
                                urls[index],
                                urlRequestCallback,
                                executor
                            )

                            val request: UrlRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && SdkExtensions.getExtensionVersion(Build.VERSION_CODES.S) >= 7) {
                                Log.d(TAG, "UrlRequest url = ${urls[index]}, priority = $index")
                                requestBuilder
                                    .setPriority(index)
                                    .build()
                            } else {
                                requestBuilder.build()
                            }
                            request.start()
                        }
                    }) {
                        Text(text = "요청 보내기")
                    }
                }
            }
        }
    }
}