package com.example.assignment_fit5046.datamodels

import com.google.gson.annotations.SerializedName

// Quotable API — https://api.quotable.io/random
data class Quote(
    @SerializedName("q") val content: String = "",
    @SerializedName("a") val author: String = "",
    val id: String = "",
    val authorSlug: String = "",
    val length: Int = 0,
    val tags: List<String> = emptyList()
)