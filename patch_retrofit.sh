sed -i 's/@POST("https:\/\/generativelanguage.googleapis.com\/v1beta\/models\/{model}:generateContent")/@POST("v1beta\/models\/{model}")/g' app/src/main/java/com/example/data/remote/GeminiApiService.kt
sed -i 's/@Path("model") model: String,/@Path(value = "model", encoded = true) model: String,/g' app/src/main/java/com/example/data/remote/GeminiApiService.kt
