package com.example.assignment_fit5046.screens.company

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsConditionsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Conditions") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Terms & Conditions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Last updated: 1 May 2026",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            TermsSection(
                title = "1. Acceptance of Terms",
                body = "By downloading or using VolunteerLink, you agree to be bound by these Terms and Conditions. If you do not agree to these terms, please do not use the application."
            )
            TermsSection(
                title = "2. Use of the Platform",
                body = "VolunteerLink is intended for use by registered volunteers and non-governmental organisations in Australia. Users must provide accurate information during registration and maintain the confidentiality of their account credentials."
            )
            TermsSection(
                title = "3. NGO Responsibilities",
                body = "NGOs posting volunteer drives are responsible for ensuring that all drive information is accurate, lawful, and up to date. VolunteerLink reserves the right to remove any drive that violates these terms or applicable law."
            )
            TermsSection(
                title = "4. Volunteer Responsibilities",
                body = "Volunteers who apply for a drive commit to attending or providing reasonable notice of cancellation. Repeated no-shows may result in account suspension at VolunteerLink's discretion."
            )
            TermsSection(
                title = "5. Privacy",
                body = "Your personal information is handled in accordance with our Privacy Policy and the Australian Privacy Act 1988. We do not sell or share your personal data with third parties without your consent."
            )
            TermsSection(
                title = "6. Intellectual Property",
                body = "All content, branding, and code within the VolunteerLink application is the intellectual property of Speed Tech Pty Ltd. Unauthorised reproduction or distribution is prohibited."
            )
            TermsSection(
                title = "7. Limitation of Liability",
                body = "VolunteerLink is provided on an 'as is' basis. We do not guarantee uninterrupted access and are not liable for any loss or damage arising from the use of the platform."
            )
            TermsSection(
                title = "8. Changes to Terms",
                body = "We may update these terms from time to time. Continued use of the application after changes constitutes acceptance of the revised terms."
            )
            TermsSection(
                title = "9. Contact",
                body = "For questions regarding these terms, contact us at legal@volunteerlink.com.au or at Level 5, 123 Collins Street, Melbourne VIC 3000."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TermsSection(title: String, body: String) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )
    }
}
