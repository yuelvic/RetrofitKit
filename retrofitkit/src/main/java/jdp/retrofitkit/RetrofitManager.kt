package jdp.retrofitkit

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit


abstract class RetrofitManager(private val context: Context) : RetrofitConfiguration {
    private var retrofit: Retrofit? = null
    override fun clearRetrofit() {
        retrofit=null
    }
    override fun OkHttpClient.Builder.interceptorConfiguration(builder: OkHttpClient.Builder): OkHttpClient.Builder
            = builder

    override fun releaseMode(cache: Cache?) {
        val  okHttpClientBuilder= OkHttpClient.Builder()
        okHttpClientBuilder.cache(cache)
                .writeTimeout(initWriteTimeOut(), TimeUnit.SECONDS)
                .connectTimeout(initConnectTimeOut(), TimeUnit.SECONDS)
                .readTimeout(initReadTimeOut(), TimeUnit.SECONDS)
                .interceptorConfiguration(okHttpClientBuilder)

        retrofit = Retrofit.Builder()
                .baseUrl(initBaseURL())
                .client(okHttpClientBuilder.build())
                .addConverterFactory(initConverterFactory())
                .addCallAdapterFactory(initRxAdapterFactory())
                .build()
    }

    override fun debugMode(cache: Cache?) {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        val okHttpClient = OkHttpClient.Builder()
                .cache(cache)
                .writeTimeout(initWriteTimeOut(), TimeUnit.SECONDS)
                .connectTimeout(initConnectTimeOut(), TimeUnit.SECONDS)
                .readTimeout(initReadTimeOut(), TimeUnit.SECONDS)
                .addInterceptor(logging)

                .build()
        retrofit = Retrofit.Builder()
                .baseUrl(initBaseURL())
                .client(okHttpClient)
                .addConverterFactory(initConverterFactory())
                .addCallAdapterFactory(initRxAdapterFactory())
                .build()
    }

    override fun create(service: Class<*>): Any {
        if (retrofit == null) {
            if (initCacheSize() != 0) {
                val cacheSize = initCacheSize() * 1024 * 1024
                val cache = Cache(context.cacheDir, cacheSize.toLong())
                if (isPrintLogEnabled()) debugMode(cache)
                else releaseMode(cache)
            } else {
                if (isPrintLogEnabled()) debugMode(null)
                else releaseMode(null)
            }
        }
        return retrofit!!.create(service)
    }

    override fun isPrintLogEnabled(): Boolean = false
}