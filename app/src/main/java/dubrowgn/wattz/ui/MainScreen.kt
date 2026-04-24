package dubrowgn.wattz.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dubrowgn.wattz.BatteryData
import dubrowgn.wattz.BatteryViewModel
import dubrowgn.wattz.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, vm: BatteryViewModel = viewModel()) {
    val data by vm.data.collectAsState()
    val primary = MaterialTheme.colorScheme.primary
    val background = MaterialTheme.colorScheme.background

    val glowTransition = rememberInfiniteTransition(label = "screen-glow")
    val glowPulse by glowTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2_500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow-pulse",
    )

    val heroTransition = rememberInfiniteTransition(label = "hero")
    val rot1 by heroTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20_000, easing = LinearEasing)),
        label = "r1",
    )
    val rot2 by heroTransition.animateFloat(
        initialValue = 0f, targetValue = -360f,
        animationSpec = infiniteRepeatable(tween(13_000, easing = LinearEasing)),
        label = "r2",
    )
    val rot3 by heroTransition.animateFloat(
        initialValue = -60f, targetValue = 60f,
        animationSpec = infiniteRepeatable(tween(3_000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "r3",
    )
    val pulse by heroTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2_500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse",
    )
    var heroCenter by remember { mutableStateOf(Offset.Zero) }
    var heroMinDim by remember { mutableFloatStateOf(0f) }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.drawBehind {
                drawRect(background)
                drawRect(
                    brush = Brush.radialGradient(
                        listOf(primary.copy(alpha = 0.04f + 0.06f * glowPulse), Color.Transparent),
                        center = Offset(size.width / 2f, 0f),
                        radius = size.minDimension * 0.85f,
                    ),
                )
            },
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.battery)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                    actions = {
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(
                                painter = painterResource(R.drawable.ico_settings),
                                contentDescription = stringResource(R.string.settings),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    },
                )
            },
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { Spacer(Modifier.height(4.dp)) }
                item {
                    HeroCard(data) { center, minDim ->
                        heroCenter = center
                        heroMinDim = minDim
                    }
                }
                item {
                    MetricCard {
                        MetricRow(stringResource(R.string.power), data.power)
                        HorizontalDivider(Modifier.padding(vertical = 2.dp))
                        MetricRow(stringResource(R.string.current), data.current)
                        HorizontalDivider(Modifier.padding(vertical = 2.dp))
                        MetricRow(stringResource(R.string.voltage), data.voltage)
                        HorizontalDivider(Modifier.padding(vertical = 2.dp))
                        MetricRow(stringResource(R.string.energy), data.energy)
                        HorizontalDivider(Modifier.padding(vertical = 2.dp))
                        MetricRow(stringResource(R.string.temperature), data.temperature)
                    }
                }
                item {
                    MetricCard {
                        MetricRow(stringResource(R.string.chargeLevel), data.chargeLevel)
                        HorizontalDivider(Modifier.padding(vertical = 2.dp))
                        MetricRow(stringResource(R.string.charging), data.charging)
                        HorizontalDivider(Modifier.padding(vertical = 2.dp))
                        MetricRow(stringResource(R.string.chargingSince), data.chargingSince)
                        HorizontalDivider(Modifier.padding(vertical = 2.dp))
                        MetricRow(stringResource(R.string.timeToFullCharge), data.timeToFullCharge)
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
        if (heroMinDim > 0f) {
            Canvas(Modifier.fillMaxSize()) {
                rotate(rot1, pivot = heroCenter) {
                    val r = heroMinDim * 0.68f
                    drawArc(
                        color = primary.copy(alpha = 0.16f),
                        startAngle = -20f, sweepAngle = 190f, useCenter = false,
                        topLeft = Offset(heroCenter.x - r, heroCenter.y - r), size = Size(r * 2, r * 2),
                        style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
                rotate(rot2, pivot = heroCenter) {
                    val r = heroMinDim * 0.50f
                    drawArc(
                        color = primary.copy(alpha = 0.22f),
                        startAngle = 45f, sweepAngle = 130f, useCenter = false,
                        topLeft = Offset(heroCenter.x - r, heroCenter.y - r), size = Size(r * 2, r * 2),
                        style = Stroke(width = 13.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
                rotate(rot3, pivot = heroCenter) {
                    val r = heroMinDim * 0.32f
                    drawArc(
                        color = primary.copy(alpha = 0.25f + 0.20f * pulse),
                        startAngle = 90f, sweepAngle = 70f, useCenter = false,
                        topLeft = Offset(heroCenter.x - r, heroCenter.y - r), size = Size(r * 2, r * 2),
                        style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroCard(
    data: BatteryData,
    onBoundsChanged: (center: Offset, minDimension: Float) -> Unit,
) {
    val onBackground = MaterialTheme.colorScheme.onBackground

    val animatedProgress by animateFloatAsState(
        targetValue = data.chargeLevelFloat,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "charge-progress",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .onGloballyPositioned { coords ->
                val topLeft = coords.localToRoot(Offset.Zero)
                val sz = coords.size
                onBoundsChanged(
                    Offset(topLeft.x + sz.width / 2f, topLeft.y + sz.height / 2f),
                    minOf(sz.width, sz.height).toFloat(),
                )
            },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedContent(
                targetState = data.power,
                transitionSpec = {
                    (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.94f))
                        .togetherWith(fadeOut(tween(200)))
                },
                label = "power-value",
            ) { value ->
                Text(
                    text = value,
                    style = MaterialTheme.typography.displayLarge,
                    color = onBackground,
                )
            }
            Text(
                text = "W",
                style = MaterialTheme.typography.titleMedium,
                color = onBackground.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(20.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(MaterialTheme.shapes.small),
                strokeCap = StrokeCap.Round,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = data.chargeLevel,
                style = MaterialTheme.typography.labelMedium,
                color = onBackground.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun MetricCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        AnimatedContent(
            targetState = value,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
            label = "metric-$label",
        ) { v ->
            Text(
                text = v,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
