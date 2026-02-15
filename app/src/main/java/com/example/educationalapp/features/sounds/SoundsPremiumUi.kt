package com.example.educationalapp.features.sounds

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min


// ================================================================
// UiText helpers (works with the upgraded models)
// ================================================================
@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Raw -> value
    is UiText.Res -> stringResource(id)
}

// ================================================================
// PERFECT FIT GRID (no vertical scrolling). Falls back to paging
// when items cannot fit above a minimum comfortable size.
// ================================================================

data class GridPlan(
    val columns: Int,
    val rows: Int,
    val cellWidth: Dp,
    val cellHeight: Dp,
    val hGap: Dp,
    val vGap: Dp
) {
    val capacity: Int get() = columns * rows
}

private fun computeFitPlanOrNull(
    count: Int,
    maxW: Dp,
    maxH: Dp,
    minCellW: Dp,
    minCellH: Dp,
    aspect: Float,
    hGap: Dp,
    vGap: Dp,
    maxColumnsLimit: Int = 6
): GridPlan? {
    if (count <= 0) return null

    val maxColsByWidth = max(
        1,
        ((maxW.value + hGap.value) / (minCellW.value + hGap.value)).toInt()
    )

    val maxCols = min(count, min(maxColsByWidth, maxColumnsLimit))

    var best: GridPlan? = null
    var bestArea = -1f

    for (cols in 1..maxCols) {
        val rows = ceil(count / cols.toFloat()).toInt().coerceAtLeast(1)

        val w = ((maxW.value - hGap.value * (cols - 1)) / cols).dp
        val rawH = ((maxH.value - vGap.value * (rows - 1)) / rows).dp

        // Keep aspect ratio (don’t force taller than available)
        val h = min(rawH.value, (w.value / aspect)).dp

        if (w.value >= minCellW.value && h.value >= minCellH.value) {
            val area = w.value * h.value
            if (area > bestArea) {
                bestArea = area
                best = GridPlan(cols, rows, w, h, hGap, vGap)
            }
        }
    }

    return best
}

private fun computePagedPlan(
    maxW: Dp,
    maxH: Dp,
    minCellW: Dp,
    minCellH: Dp,
    aspect: Float,
    hGap: Dp,
    vGap: Dp,
    maxColumnsLimit: Int = 6
): GridPlan {
    val cols = max(
        1,
        min(
            maxColumnsLimit,
            ((maxW.value + hGap.value) / (minCellW.value + hGap.value)).toInt()
        )
    )

    val rows = max(
        1,
        ((maxH.value + vGap.value) / (minCellH.value + vGap.value)).toInt()
    )

    val w = ((maxW.value - hGap.value * (cols - 1)) / cols).dp
    val rawH = ((maxH.value - vGap.value * (rows - 1)) / rows).dp
    val h = min(rawH.value, (w.value / aspect)).dp

    return GridPlan(cols, rows, w, h, hGap, vGap)
}

@Composable
fun <T> PerfectFitGridOrPager(
    items: List<T>,
    modifier: Modifier = Modifier,
    minCellW: Dp,
    minCellH: Dp,
    aspect: Float,
    hGap: Dp = 22.dp,
    vGap: Dp = 22.dp,
    pageContentPadding: Dp = 28.dp,
    key: ((T) -> Any)? = null,
    content: @Composable (item: T, index: Int, cellSize: DpSize) -> Unit
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier) {
        val maxW = maxWidth
        val maxH = maxHeight

        val fitPlan = remember(items.size, maxW, maxH, minCellW, minCellH, aspect, hGap, vGap) {
            computeFitPlanOrNull(
                count = items.size,
                maxW = maxW,
                maxH = maxH,
                minCellW = minCellW,
                minCellH = minCellH,
                aspect = aspect,
                hGap = hGap,
                vGap = vGap
            )
        }

        if (fitPlan != null) {
            PerfectFitGrid(
                items = items,
                plan = fitPlan,
                key = key,
                modifier = Modifier.fillMaxSize(),
                content = content
            )
        } else {
            val pagePlan = remember(maxW, maxH, minCellW, minCellH, aspect, hGap, vGap) {
                computePagedPlan(
                    maxW = maxW,
                    maxH = maxH,
                    minCellW = minCellW,
                    minCellH = minCellH,
                    aspect = aspect,
                    hGap = hGap,
                    vGap = vGap
                )
            }

            val capacity = max(1, pagePlan.capacity)
            val pageCount = ceil(items.size / capacity.toFloat()).toInt().coerceAtLeast(1)
            val pagerState = rememberPagerState(pageCount = { pageCount })

            Column(Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = pageContentPadding)
                ) { pageIndex ->
                    val pageOffset = (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
                    val scaleFactor = lerp(1f, 0.92f, pageOffset.absoluteValue.coerceIn(0f, 1f))
                    val alphaFactor = lerp(1f, 0.55f, pageOffset.absoluteValue.coerceIn(0f, 1f))

                    val start = pageIndex * capacity
                    val end = min(start + capacity, items.size)
                    val pageItems = items.subList(start, end)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scaleFactor
                                scaleY = scaleFactor
                                alpha = alphaFactor
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        PerfectFitGrid(
                            items = pageItems,
                            plan = pagePlan,
                            key = key,
                            modifier = Modifier.fillMaxSize(),
                            content = content
                        )
                    }
                }

                if (pageCount > 1) {
                    PremiumPagerDots(
                        count = pageCount,
                        current = pagerState.currentPage,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 14.dp, bottom = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> PerfectFitGrid(
    items: List<T>,
    plan: GridPlan,
    modifier: Modifier = Modifier,
    key: ((T) -> Any)? = null,
    content: @Composable (item: T, index: Int, cellSize: DpSize) -> Unit
) {
    val cellSize = remember(plan) { DpSize(plan.cellWidth, plan.cellHeight) }

    // Center the grid vertically when there is leftover height.
    val density = LocalDensity.current
    val totalRows = ceil(items.size / plan.columns.toFloat()).toInt().coerceAtLeast(1)
    val usedH = plan.cellHeight.value * totalRows + plan.vGap.value * (totalRows - 1)

    val topPad = with(density) {
        (((plan.rows * plan.cellHeight.value + (plan.rows - 1) * plan.vGap.value) - usedH) / 2f).dp
    }

    // Lazy grid is fine here because we pre-compute a plan that fits.
    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(plan.columns),
        userScrollEnabled = false,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(plan.hGap, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(plan.vGap, Alignment.CenterVertically),
        contentPadding = PaddingValues(top = topPad.coerceAtLeast(0.dp))
    ) grid@{
        // We intentionally avoid the name clash between the 'items' parameter and LazyGridScope.items by calling via receiver.
        this@grid.items(items.size) { idx ->
            val item = items[idx]
            Box(
                modifier = Modifier
                    .height(cellSize.height)
                    .fillMaxWidth()
            ) {
                content(item, idx, cellSize)
            }
        }
    }
}

// ================================================================
// PREMIUM VISUALS + ANIMATIONS (glass + glow + shine + bounce)
// ================================================================

@Composable
fun PremiumGlassCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    corner: Dp = 36.dp,
    baseColor: Color = Color.White,
    borderAlpha: Float = 0.55f,
    contentPadding: Dp = 18.dp,
    enterDelayMs: Int = 0,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        if (enterDelayMs > 0) kotlinx.coroutines.delay(enterDelayMs.toLong())
        appear.animateTo(1f, tween(durationMillis = 520, easing = FastOutSlowInEasing))
    }

    val infinite = rememberInfiniteTransition(label = "inf")
    val floatY by infinite.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    val shimmerX by infinite.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerX"
    )

    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.965f else 1f,
        animationSpec = spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessHigh),
        label = "pressScale"
    )

    val shape = remember(corner) { RoundedCornerShape(corner) }

    Card(
        modifier = modifier
            .semantics { role = Role.Button }
            .graphicsLayer {
                alpha = appear.value
                val base = lerp(0.94f, 1f, appear.value)
                scaleX = base * pressScale
                scaleY = base * pressScale
                translationY = 0f
            }
            .drawWithContent { drawContent() },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = 18.dp),
        colors = CardDefaults.cardColors(containerColor = baseColor.copy(alpha = 0.94f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .clickable(interactionSource = interaction, indication = null) { onClick() }
        ) {
            content()
        }
    }
}

@Composable
fun PremiumCategoryCard(
    category: SoundCategory,
    index: Int,
    onClick: (SoundCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumGlassCard(
        modifier = modifier,
        onClick = { onClick(category) },
        baseColor = Color.White,
        enterDelayMs = index * 70,
        contentPadding = 0.dp
    ) {
        // Full-bleed image
        Image(
            painter = painterResource(id = category.coverRes),
            contentDescription = category.title.asString(),
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(0f, category.coverYBias.coerceIn(-1f, 1f)),
            modifier = Modifier.fillMaxSize()
        )

        // cinematic bottom gradient
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(110.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            category.themeColor.copy(alpha = 0.92f)
                        )
                    )
                )
        )

        Text(
            text = category.title.asString(),
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp, vertical = 16.dp)
        )
    }
}

@Composable
fun PremiumAnimalCard(
    item: SoundItem,
    index: Int,
    onClick: (Offset) -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    var centerPos by remember { mutableStateOf(Offset.Zero) }

    PremiumGlassCard(
        modifier = modifier
            .onGloballyPositioned { coords ->
                val rootPos = coords.positionInRoot()
                centerPos = Offset(
                    x = rootPos.x + coords.size.width / 2f,
                    y = rootPos.y + coords.size.height / 2f
                )
            },
        onClick = { onClick(centerPos) },
        baseColor = tint,
        enterDelayMs = index * 55,
        contentPadding = 0.dp,
        borderAlpha = 0f
    ) {
        // Fill the whole card (no inner frame, no extra label layer).
        Image(
            painter = painterResource(id = item.imageRes),
            contentDescription = item.name.asString(),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Make the animal "as big as the card" without cutting faces.
                    scaleX = 1.22f
                    scaleY = 1.22f
                }
        )
    }
}



@Composable
fun PremiumPagerDots(
    count: Int,
    current: Int,
    modifier: Modifier = Modifier
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(count) { i ->
            val selected = i == current
            val size by animateFloatAsState(
                targetValue = if (selected) 14f else 10f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
                label = "dot"
            )
            val alpha = if (selected) 1f else 0.45f
            Box(
                Modifier
                    .size(size.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = alpha))
            )
        }
    }
}