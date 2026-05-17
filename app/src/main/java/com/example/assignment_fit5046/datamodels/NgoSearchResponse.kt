package com.example.assignment_fit5046.datamodels

import com.google.gson.annotations.SerializedName

data class ProjectSearchResponse(
    @SerializedName("projects") val projects: ProjectWrapper
)

data class ProjectWrapper(
    @SerializedName("project") val project: List<GlobalGivingProject> = emptyList()
)

data class GlobalGivingProject(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("summary") val summary: String?,
    @SerializedName("projectLink") val projectLink: String?,
    @SerializedName("organization") val organization: GGOrganization?,
    @SerializedName("themes") val themes: GGThemeWrapper?
)

data class GGOrganization(
    @SerializedName("name") val name: String?,
    @SerializedName("logoUrl") val logoUrl: String?
)

data class GGThemeWrapper(
    @SerializedName("theme") val theme: List<GGTheme> = emptyList()
)

data class GGTheme(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)
