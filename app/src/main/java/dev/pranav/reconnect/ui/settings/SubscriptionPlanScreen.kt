package dev.pranav.reconnect.ui.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.pranav.reconnect.ui.components.AppTopBar
import dev.pranav.reconnect.ui.components.ScreenTitle
import dev.pranav.reconnect.ui.theme.GoldPrimary

private enum class PlanType(val title: String, val subtitle: String, val price: String) {
    FREE("Free", "Basic access to ReConnect features", "$0/mo"),
    PREMIUM("Premium", "Cloud backup, multi-device sync, and priority reminders", "$4.99/mo")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionPlanScreen(
    onBack: () -> Unit
) {
    val selectedPlan = remember { mutableStateOf(PlanType.FREE) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AppTopBar(
                title = "Subscription Plan",
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            ScreenTitle(
                text = "Choose a plan",
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Text(
                text = "Pick the plan that suits your relationship goals. Premium unlocks cloud backup, multi-device sync, and smarter reminders.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            SubscriptionPlanCard(
                plan = PlanType.FREE,
                selected = selectedPlan.value == PlanType.FREE,
                onSelect = { selectedPlan.value = PlanType.FREE }
            )

            SubscriptionPlanCard(
                plan = PlanType.PREMIUM,
                selected = selectedPlan.value == PlanType.PREMIUM,
                onSelect = { selectedPlan.value = PlanType.PREMIUM }
            )

            Spacer(Modifier.height(24.dp))
            Text(
                text = "Premium benefits",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            SubscriptionBenefitItem(
                title = "Cloud backup",
                description = "Keep your contacts, moments, and images safe in the cloud."
            )
            SubscriptionBenefitItem(
                title = "Multi-device sync",
                description = "Access the same circle from your phone, tablet, or future devices."
            )
            SubscriptionBenefitItem(
                title = "Priority reminders",
                description = "Get smarter alerts for your highest-priority relationships."
            )

            Spacer(modifier = Modifier.weight(1f, fill = false))
            Spacer(modifier = Modifier.height(16.dp))

            val actionLabel =
                if (selectedPlan.value == PlanType.PREMIUM) "Coming soon" else "Continue with Free"
            Button(
                onClick = { },
                enabled = selectedPlan.value == PlanType.FREE,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text(
                    text = actionLabel,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Premium will be available in a future release.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun SubscriptionPlanCard(
    plan: PlanType,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) GoldPrimary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface
        ),
        border = if (selected) BorderStroke(2.dp, GoldPrimary) else null
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (plan == PlanType.PREMIUM) Icons.Default.Star else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (selected) GoldPrimary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = plan.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Text(
                text = plan.price,
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun SubscriptionBenefitItem(
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(GoldPrimary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(12.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
