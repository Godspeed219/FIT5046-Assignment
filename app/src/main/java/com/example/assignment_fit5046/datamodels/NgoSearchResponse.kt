package com.example.assignment_fit5046.datamodels

import com.google.gson.annotations.SerializedName

data class NgoSearchResponse(
    @SerializedName("organizations") val organizations: OrganizationWrapper
)

data class OrganizationWrapper(
    @SerializedName("organization") val organization: List<NgoOrganization> = emptyList()
)

data class NgoOrganization(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("mission") val mission: String?,
    @SerializedName("logoUrl") val logoUrl: String?,
    @SerializedName("projectLink") val projectLink: String?,
    @SerializedName("themes") val themes: ThemeWrapper?,
    @SerializedName("countries") val countries: CountryWrapper?
)

data class ThemeWrapper(
    @SerializedName("theme") val theme: List<NgoTheme> = emptyList()
)

data class NgoTheme(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)

data class CountryWrapper(
    @SerializedName("country") val country: List<NgoCountry> = emptyList()
)

data class NgoCountry(
    @SerializedName("iso3166CountryCode") val countryCode: String,
    @SerializedName("name") val name: String
)
