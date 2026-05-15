package com.example.assignment_fit5046.datamodels

object DummyData {

    val VOLUNTEER_USER = User(
        uid = "v1",
        name = "Alex Johnson",
        email = "volunteer@test.com",
        role = UserRole.VOLUNTEER,
        phoneNumber = "",
        bio = "Passionate about giving back",
        profileImageUrl = ""
    )

    val NGO_USER = User(
        uid = "n1",
        name = "Green Earth Australia",
        email = "ngo@test.com",
        role = UserRole.NGO,
        phoneNumber = "",
        bio = "Environmental NGO based in Melbourne",
        profileImageUrl = "",
        ngoName = "Green Earth Australia",
        ngoDescription = "Environmental NGO based in Melbourne"
    )

    val DRIVES = listOf(
        Drive(
            driveId = "d1",
            ngoId = "n1",
            ngoName = "Green Earth Australia",
            title = "Yarra River Clean-Up",
            description = "Join us to clean up the Yarra River banks and surrounding parklands.",
            location = "Richmond, Melbourne VIC 3121",
            date = "2026-05-10",
            maxVolunteers = 30,
            currentVolunteers = 12,
            category = "Environment",
            status = DriveStatus.ACTIVE,
            createdAt = 1744300800000L
        ),
        Drive(
            driveId = "d2",
            ngoId = "n2",
            ngoName = "Melbourne Learns",
            title = "Tutoring for Underprivileged Kids",
            description = "Help primary school students in Fitzroy with reading and mathematics.",
            location = "Fitzroy, Melbourne VIC 3065",
            date = "2026-05-17",
            maxVolunteers = 15,
            currentVolunteers = 8,
            category = "Education",
            status = DriveStatus.ACTIVE,
            createdAt = 1744387200000L
        ),
        Drive(
            driveId = "d3",
            ngoId = "n3",
            ngoName = "HealthCare Volunteers VIC",
            title = "Community Health Fair",
            description = "Assist at a free health screening and wellness event in Footscray.",
            location = "Footscray, Melbourne VIC 3011",
            date = "2026-06-01",
            maxVolunteers = 25,
            currentVolunteers = 10,
            category = "Health",
            status = DriveStatus.ACTIVE,
            createdAt = 1744473600000L
        ),
        Drive(
            driveId = "d4",
            ngoId = "n4",
            ngoName = "RSPCA Victoria",
            title = "Animal Shelter Volunteer Day",
            description = "Spend a day caring for animals and helping with adoptions at our Burwood shelter.",
            location = "Burwood, Melbourne VIC 3125",
            date = "2026-06-14",
            maxVolunteers = 20,
            currentVolunteers = 5,
            category = "Animal Welfare",
            status = DriveStatus.ACTIVE,
            createdAt = 1744560000000L
        ),
        Drive(
            driveId = "d5",
            ngoId = "n5",
            ngoName = "Brunswick Community Hub",
            title = "Community Garden Build",
            description = "Help design and build a community vegetable garden in Brunswick for local residents.",
            location = "Brunswick, Melbourne VIC 3056",
            date = "2026-07-05",
            maxVolunteers = 40,
            currentVolunteers = 15,
            category = "Community",
            status = DriveStatus.ACTIVE,
            createdAt = 1744646400000L
        )
    )

    val APPLICATIONS = listOf(
        Application(
            applicationId = "a1",
            driveId = "d1",
            driveTitle = "Yarra River Clean-Up",
            volunteerId = "v1",
            volunteerName = "Alex Johnson",
            status = ApplicationStatus.PENDING,
            appliedAt = 1744300800000L,
            message = "I would love to help clean up the Yarra River."
        ),
        Application(
            applicationId = "a2",
            driveId = "d2",
            driveTitle = "Tutoring for Underprivileged Kids",
            volunteerId = "v1",
            volunteerName = "Alex Johnson",
            status = ApplicationStatus.APPROVED,
            appliedAt = 1744387200000L,
            message = "Excited to assist with tutoring and mentoring."
        ),
        Application(
            applicationId = "a3",
            driveId = "d3",
            driveTitle = "Community Health Fair",
            volunteerId = "v1",
            volunteerName = "Alex Johnson",
            status = ApplicationStatus.REJECTED,
            appliedAt = 1744473600000L,
            message = "Happy to help at the health fair in any capacity."
        )
    )

    val QUOTE = Quote(
        id = "q1",
        content = "The best way to find yourself is to lose yourself in the service of others.",
        author = "Mahatma Gandhi",
        tags = listOf("service", "self-discovery", "volunteering"),
        length = 75
    )

    val NGO_OWN_DRIVES = DRIVES.filter { it.ngoId == "n1" }

    val NGO_RECEIVED_APPLICATIONS = listOf(
        Application(
            applicationId = "na1",
            driveId = "d1",
            driveTitle = "Yarra River Clean-Up",
            volunteerId = "v1",
            volunteerName = "Alex Johnson",
            status = ApplicationStatus.PENDING,
            appliedAt = 1744300800000L,
            message = "I would love to help clean up the Yarra River."
        ),
        Application(
            applicationId = "na2",
            driveId = "d1",
            driveTitle = "Yarra River Clean-Up",
            volunteerId = "v2",
            volunteerName = "Sarah Chen",
            status = ApplicationStatus.PENDING,
            appliedAt = 1744310800000L,
            message = "Environmental conservation is my passion!"
        ),
        Application(
            applicationId = "na3",
            driveId = "d1",
            driveTitle = "Yarra River Clean-Up",
            volunteerId = "v3",
            volunteerName = "Marcus Williams",
            status = ApplicationStatus.APPROVED,
            appliedAt = 1744290800000L,
            message = "I have experience with river cleanup events."
        ),
        Application(
            applicationId = "na4",
            driveId = "d1",
            driveTitle = "Yarra River Clean-Up",
            volunteerId = "v4",
            volunteerName = "Emma Rodriguez",
            status = ApplicationStatus.REJECTED,
            appliedAt = 1744280800000L,
            message = "Would love to participate in any way I can!"
        )
    )

    val NGO_RESULTS = listOf(
        NgoSearchResponse(
            organizations = OrganizationWrapper(
                organization = listOf(
                    NgoOrganization(
                        id = 1,
                        name = "OzHarvest",
                        mission = "To nourish our country by saving good food and delivering it to people in need.",
                        logoUrl = null,
                        projectLink = null,
                        themes = ThemeWrapper(listOf(NgoTheme("food_security", "Food Security"))),
                        countries = CountryWrapper(listOf(NgoCountry("AUS", "Australia")))
                    )
                )
            )
        ),
        NgoSearchResponse(
            organizations = OrganizationWrapper(
                organization = listOf(
                    NgoOrganization(
                        id = 2,
                        name = "Beyond Blue",
                        mission = "To work towards a society where all people achieve their best mental health.",
                        logoUrl = null,
                        projectLink = null,
                        themes = ThemeWrapper(listOf(NgoTheme("mental_health", "Mental Health"))),
                        countries = CountryWrapper(listOf(NgoCountry("AUS", "Australia")))
                    )
                )
            )
        ),
        NgoSearchResponse(
            organizations = OrganizationWrapper(
                organization = listOf(
                    NgoOrganization(
                        id = 3,
                        name = "Australian Red Cross",
                        mission = "To support people in crisis and build more resilient communities.",
                        logoUrl = null,
                        projectLink = null,
                        themes = ThemeWrapper(listOf(NgoTheme("humanitarian", "Humanitarian Aid"))),
                        countries = CountryWrapper(listOf(NgoCountry("AUS", "Australia")))
                    )
                )
            )
        )
    )
}
