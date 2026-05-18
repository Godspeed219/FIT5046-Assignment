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
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("logoUrl") val logoUrl: String? = null,
    @SerializedName("mission") val mission: String? = null,
    @SerializedName("contactAddress") val contactAddress: String? = null,
    @SerializedName("contactAddress2") val contactAddress2: String? = null,
    @SerializedName("contactPhone") val contactPhone: String? = null,
    @SerializedName("contactUrl") val contactUrl: String? = null,
    @SerializedName("totalProjects") val totalProjects: Int? = null,
    @SerializedName("activeProjects") val activeProjects: Int? = null
)

data class SearchProjectsResponse(
    @SerializedName("search") val search: SearchWrapper? = null
)

data class SearchWrapper(
    @SerializedName("response") val response: SearchResponseBody? = null
)

data class SearchResponseBody(
    @SerializedName("projects") val projects: ProjectWrapper? = null
)

data class GGThemeWrapper(
    @SerializedName("theme") val theme: List<GGTheme> = emptyList()
)

data class GGTheme(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)
