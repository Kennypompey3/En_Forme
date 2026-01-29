package com.example.enforme

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlin.math.roundToInt

// -------------------- Models --------------------

data class GymPackage(
    val title: String,
    val description: String,
    val includes: List<String>,
    val monthlyPriceLabel: String,
    val amountKobo: Int,
    val planCode: String,
    val imageResId: Int
)

// -------------------- Prices now in "hundreds" --------------------
// Example: 25,000 -> 250
private const val WL_SINGLE = 250
private const val MB_SINGLE = 300
private const val ES_SINGLE = 200

private fun plus75Percent(base: Int): Int {
    // round( base * 1.75 )
    return (base * 1.75f).roundToInt()
}

// Couples (rounded)
private val WL_COUPLES = plus75Percent(WL_SINGLE) // 438
private val MB_COUPLES = plus75Percent(MB_SINGLE) // 525
private val ES_COUPLES = plus75Percent(ES_SINGLE) // 350

// -------------------- Packages --------------------
// Uses 3 independent images for the 3 program cards.
// Put these in res/drawable and name them:
// program_weight_loss, program_muscle_building, program_endurance_stamina

private fun singlesPackages(): List<GymPackage> = listOf(
    GymPackage(
        title = "Weight Loss",
        description = "A comprehensive program designed to help you shed pounds effectively.",
        includes = listOf(
            "Personalized meal plans",
            "HIIT & Cardio sessions",
            "Weekly progress tracking",
            "Nutritionist consultation"
        ),
        monthlyPriceLabel = "₦$WL_SINGLE",
        amountKobo = WL_SINGLE * 100,
        planCode = "plan-singles-weightloss",
        imageResId = R.drawable.program_weight_loss
    ),
    GymPackage(
        title = "Muscle Building",
        description = "Focus on hypertrophy and strength gains with expert-led training.",
        includes = listOf(
            "Customized lifting splits",
            "Strength assessment",
            "Supplementation guide",
            "Form correction workshops"
        ),
        monthlyPriceLabel = "₦$MB_SINGLE",
        amountKobo = MB_SINGLE * 100,
        planCode = "plan-singles-muscle",
        imageResId = R.drawable.program_muscle_building
    ),
    GymPackage(
        title = "Endurance & Stamina",
        description = "Boost cardiovascular health and stamina for sports or general fitness.",
        includes = listOf(
            "Run & Swim coaching",
            "Circuit training",
            "Heart rate monitoring",
            "Recovery strategies"
        ),
        monthlyPriceLabel = "₦$ES_SINGLE",
        amountKobo = ES_SINGLE * 100,
        planCode = "plan-singles-endurance",
        imageResId = R.drawable.program_endurance_stamina
    )
)

private fun couplesPackages(): List<GymPackage> = listOf(
    GymPackage(
        title = "Weight Loss",
        description = "A comprehensive program designed to help you shed pounds effectively.",
        includes = listOf(
            "Personalized meal plans",
            "HIIT & Cardio sessions",
            "Weekly progress tracking",
            "Nutritionist consultation"
        ),
        monthlyPriceLabel = "₦$WL_COUPLES",
        amountKobo = WL_COUPLES * 100,
        planCode = "plan-couples-weightloss",
        imageResId = R.drawable.program_weight_loss
    ),
    GymPackage(
        title = "Muscle Building",
        description = "Focus on hypertrophy and strength gains with expert-led training.",
        includes = listOf(
            "Customized lifting splits",
            "Strength assessment",
            "Supplementation guide",
            "Form correction workshops"
        ),
        monthlyPriceLabel = "₦$MB_COUPLES",
        amountKobo = MB_COUPLES * 100,
        planCode = "plan-couples-muscle",
        imageResId = R.drawable.program_muscle_building
    ),
    GymPackage(
        title = "Endurance & Stamina",
        description = "Boost cardiovascular health and stamina for sports or general fitness.",
        includes = listOf(
            "Run & Swim coaching",
            "Circuit training",
            "Heart rate monitoring",
            "Recovery strategies"
        ),
        monthlyPriceLabel = "₦$ES_COUPLES",
        amountKobo = ES_COUPLES * 100,
        planCode = "plan-couples-endurance",
        imageResId = R.drawable.program_endurance_stamina
    )
)

// -------------------- Routes --------------------

private object ProgramsRoutes {
    const val Chooser = "programs/chooser"
    const val Singles = "programs/singles"
    const val Couples = "programs/couples"
}

/**
 * HomeScreen calls THIS for the Programs tab.
 * When user taps "Join Program", we call onProgramClicked(pkg).
 */
@Composable
fun ProgramsFlowScreen(
    modifier: Modifier = Modifier,
    paymentState: PaymentUiState,
    onProgramClicked: (GymPackage) -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ProgramsRoutes.Chooser,
        modifier = modifier.fillMaxSize()
    ) {
        composable(ProgramsRoutes.Chooser) {
            MembershipChooserScreen(
                onSinglesClick = { navController.navigate(ProgramsRoutes.Singles) },
                onCouplesClick = { navController.navigate(ProgramsRoutes.Couples) }
            )
        }

        composable(ProgramsRoutes.Singles) {
            MembershipPackagesScreen(
                title = "Singles Membership",
                packages = remember { singlesPackages() },
                paymentState = paymentState,
                onBack = { navController.popBackStack() },
                onJoin = { pkg -> onProgramClicked(pkg) }
            )
        }

        composable(ProgramsRoutes.Couples) {
            MembershipPackagesScreen(
                title = "Couples Membership",
                packages = remember { couplesPackages() },
                paymentState = paymentState,
                onBack = { navController.popBackStack() },
                onJoin = { pkg -> onProgramClicked(pkg) }
            )
        }
    }
}

// -------------------- Screen 1: Chooser UI --------------------

@Composable
private fun MembershipChooserScreen(
    onSinglesClick: () -> Unit,
    onCouplesClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            Text(
                text = "Choose Your Membership",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Select the plan type that best fits your lifestyle.\n" +
                        "Whether you're training solo, with a partner,\n" +
                        "or optimizing your team's health.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))
        }

        item {
            NarrowMembershipCard(
                imageResId = R.drawable.singles_membership,
                headline = "Individual Goals focused on\nPersonal goals and Self-\nImprovement",
                buttonLabel = "Singles Membership",
                onClick = onSinglesClick
            )
        }

        item { Spacer(modifier = Modifier.height(18.dp)) }

        item {
            NarrowMembershipCard(
                imageResId = R.drawable.couples_membership,
                headline = "Train together with a Partner. Shared\nGoals, Shared Success, Better value.",
                buttonLabel = "Couples Membership",
                onClick = onCouplesClick
            )
        }
    }
}

@Composable
private fun NarrowMembershipCard(
    imageResId: Int,
    headline: String,
    buttonLabel: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    contentScale = ContentScale.Crop
                )

                Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Text(
                            text = headline,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onClick,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(buttonLabel)
                        }
                    }
                }
            }
        }
    }
}

// -------------------- Screen 2: Packages list --------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MembershipPackagesScreen(
    title: String,
    packages: List<GymPackage>,
    paymentState: PaymentUiState,
    onBack: () -> Unit,
    onJoin: (GymPackage) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(packages, key = { it.planCode }) { pkg ->
                    PackageCard(
                        pkg = pkg,
                        onJoin = { onJoin(pkg) }
                    )
                }
            }

            if (paymentState is PaymentUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun PackageCard(
    pkg: GymPackage,
    onJoin: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .widthIn(max = 420.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Image(
                painter = painterResource(id = pkg.imageResId),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = pkg.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                Text(
                    text = pkg.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(14.dp))
                Text("Includes:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))

                pkg.includes.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(item, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("Monthly", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(pkg.monthlyPriceLabel, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = onJoin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Join Program")
                }
            }
        }
    }
}
