package com.example.assignment_fit5046.datamodels

import com.google.gson.annotations.SerializedName

// Quotable API — https://api.quotable.io/random
data class Quote(
    @SerializedName("_id") val id: String,
    @SerializedName("content") val content: String,
    @SerializedName("author") val author: String,
    @SerializedName("tags") val tags: List<String> = emptyList(),
    @SerializedName("length") val length: Int
)
