@file:DependsOn("com.squareup.retrofit2:retrofit:2.9.0")
@file:DependsOn("com.squareup.okhttp3:okhttp:4.9.0")

import retrofit2.Retrofit
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Body
import okhttp3.OkHttpClient

interface TestApi {
    @POST("v1beta/models/{model}:generateContent")
    fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String
    ): retrofit2.Call<Any>
}

val client = OkHttpClient.Builder().addInterceptor { chain ->
    println("URL: " + chain.request().url)
    chain.proceed(chain.request())
}.build()

val retrofit = Retrofit.Builder()
    .baseUrl("https://generativelanguage.googleapis.com/")
    .client(client)
    .build()

val api = retrofit.create(TestApi::class.java)
try {
    api.generateContent("gemini-1.5-flash", "my_key").execute()
} catch (e: Exception) {}
