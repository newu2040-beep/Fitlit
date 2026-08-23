import retrofit2.Retrofit
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Body
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Interceptor

fun main() {
    val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            println("URL IS: \${request.url}")
            chain.proceed(request)
        }
        .build()

    // Just building to trigger interceptor is hard without running a coroutine.
    // Instead we can use HttpUrl directly?
}
