package com.moliveira.app.smartfridge.modules.home.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.molive.sdk.extensions.BiasAlignmentExt
import com.molive.sdk.loading.minForPlaceholder
import com.molive.sdk.loading.placeholder
import com.molive.sdk.text.OutlinedText
import com.molive.sdk.text.OutlinedTextStyle
import com.moliveira.app.smartfridge.Res
import com.moliveira.app.smartfridge.common_expired_at
import com.moliveira.app.smartfridge.cta_add
import com.moliveira.app.smartfridge.cta_cancel
import com.moliveira.app.smartfridge.home_title_1
import com.moliveira.app.smartfridge.home_title_2
import com.moliveira.app.smartfridge.modules.camera.CameraView
import com.moliveira.app.smartfridge.modules.design.Button
import com.moliveira.app.smartfridge.modules.design.ButtonType
import com.moliveira.app.smartfridge.modules.design.ColorsTheme
import com.moliveira.app.smartfridge.modules.food.domain.FoodModel
import com.moliveira.app.smartfridge.modules.sdk.ObserveUiEffect
import com.moliveira.app.smartfridge.modules.theme.SFColors
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

class HomeScreenDestination : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<HomeViewModel>()
        val navigator = LocalNavigator.currentOrThrow

        HomeScreen(
            viewModel = viewModel,
            goToDetails = {
                navigator.push(FoodsDetailsScreenDestination())
            },
        )
    }
}

data class HomeProductBannerState(
    val name: String?,
    val thumbnail: String?,
    val expirationDate: String?,
    val buttonIsLoading: Boolean = false,
)

data class HomeState(
    val bottomBannerText: String? = null,
    val productBanner: HomeProductBannerState? = null,
)

sealed class HomeInternalState {
    data object Idle : HomeInternalState()

    data object ProductInSearch : HomeInternalState()
    data class ProductFound(
        val foodModel: FoodModel,
    ) : HomeInternalState()

    data class DateSettled(
        val foodModel: FoodModel,
        val date: LocalDate,
    ) : HomeInternalState()
}

sealed class HomeUiEffect {
    data class DisplayMessage(
        val message: String,
    ) : HomeUiEffect()

    data class StartAddAnimation(
        val icon: String,
    ) : HomeUiEffect()
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    goToDetails: () -> Unit = {},
) {

    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    val bottomMessage = remember { mutableStateOf<String?>(null) }
    var addedAnimationModel by remember { mutableStateOf<String?>(null) }
    viewModel.ObserveUiEffect {
        when (it) {
            is HomeUiEffect.DisplayMessage -> bottomMessage.value = it.message
            is HomeUiEffect.StartAddAnimation -> addedAnimationModel = it.icon
        }
    }

    var bannerIconPosition by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val animatableXOffset = remember { Animatable(0f) }
    val animatableYOffset = remember { Animatable(0f) }
    val density = LocalDensity.current
    var parentPosition by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var detailButtonPosition by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val scaleButtonAnimatable = remember { Animatable(1f) }
    val alphaIconAnimatable = remember { Animatable(0f) }

    var displayPaywall by remember { mutableStateOf(false) }
    val options = remember {
        PaywallOptions(dismissRequest = { displayPaywall = false }) {
            shouldDisplayDismissButton = true
        }
    }

    LaunchedEffect(Unit) {
        delay(3000)
    }

    if (displayPaywall) {
        Paywall(options)
    }

    LaunchedEffect(key1 = addedAnimationModel) {
        if (addedAnimationModel != null) {
            val iconPosition = bannerIconPosition ?: return@LaunchedEffect
            val pposition = parentPosition ?: return@LaunchedEffect
            val buttonPosition = detailButtonPosition ?: return@LaunchedEffect
            val offset = pposition.localPositionOf(
                iconPosition,
                Offset.Zero,
            )

            val buttonOffset = pposition.localPositionOf(
                buttonPosition,

                Offset(
                    x = -with(density) { 24.dp.toPx() },
                    y = -with(density) { 24.dp.toPx() },
                ),
            )

            animatableXOffset.snapTo(offset.x)
            animatableYOffset.snapTo(offset.y)
            alphaIconAnimatable.snapTo(1f)

            listOf(
                async {
                    animatableXOffset.animateTo(
                        buttonOffset.x,
                        tween(
                            durationMillis = 1000,
                            easing = CubicBezierEasing(
                                0f, .67f, .37f, 1.03f
                            )
                        ),
                    )
                },
                async {
                    animatableYOffset.animateTo(
                        buttonOffset.y,
                        tween(
                            durationMillis = 1000,
                            easing = FastOutSlowInEasing,
                        ),
                    )
                },
                async {
                    alphaIconAnimatable.animateTo(
                        0f,
                        tween(
                            delayMillis = 600,
                            durationMillis = 200,
                            easing = LinearEasing,
                        ),
                    )
                },
            ).awaitAll()

            scaleButtonAnimatable.animateTo(1.4f, tween(200))
            scaleButtonAnimatable.animateTo(1f, tween(200))
            addedAnimationModel = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { parentPosition = it },
    ) {
        HomeScreenContent(
            state = state,
            onTextRecognized = viewModel::onTextRecognized,
            onBarcodeRecognized = viewModel::onBarcodeRecognized,
            onAddProduct = viewModel::onAddProduct,
            onDeleteProduct = viewModel::onDeleteProduct,
            onBannerIconPositioned = { bannerIconPosition = it },
        )

        addedAnimationModel?.let {
            AsyncImage(
                modifier = Modifier
                    .align(TopStart)
                    .offset(
                        x = with(density) { animatableXOffset.value.toDp() },
                        y = with(density) { animatableYOffset.value.toDp() },
                    )
                    .alpha(alphaIconAnimatable.value)
                    .clip(RoundedCornerShape(8.dp))
                    .size(64.dp),
                model = it,
                contentDescription = null,
            )
        }

        Crossfade(
            modifier = Modifier.align(BiasAlignmentExt.horizontalCenter(0.2f)),
            targetState = bottomMessage.value,
        ) {
            if (it != null) {
                val offsetYAnimate = remember(it) { Animatable(0f) }
                val alphaAnimate = remember(it) { Animatable(1f) }

                LaunchedEffect(key1 = it) {
                    launch {
                        offsetYAnimate.animateTo(
                            with(density) { -100.dp.toPx() },
                            tween(
                                durationMillis = 1500,
                                easing = FastOutSlowInEasing,
                            ),
                        )
                        bottomMessage.value = null
                    }
                    launch {
                        alphaAnimate.animateTo(
                            0f,
                            tween(
                                delayMillis = 1000,
                                durationMillis = 500,
                                easing = FastOutSlowInEasing,
                            ),
                        )
                    }
                }
                OutlinedText(
                    modifier = Modifier
                        .offset(y = with(density) { offsetYAnimate.value.toDp() })
                        .alpha(alphaAnimate.value),
                    text = it,
                    style = OutlinedTextStyle(
                        style = TextStyle(
                            fontSize = 22.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        ),
                        outlinedColor = SFColors.primary._300,
                        outlinedWidth = 3.dp,
                    ),
                )
            }
        }

        androidx.compose.material3.Button(
            modifier = Modifier
                .size(48.dp)
                .align(
                    alignment = BiasAlignment(
                        horizontalBias = 0.9f,
                        verticalBias = 0.9f,
                    ),
                )
                .scale(scaleButtonAnimatable.value)
                .onGloballyPositioned { detailButtonPosition = it },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = SFColors.primary._300,
                contentColor = Color.White,
            ),
            onClick = goToDetails,
            contentPadding = PaddingValues(8.dp),
        ) {
            Icon(
                modifier = Modifier.fillMaxSize(),
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = null,
            )
        }
    }
}

val baseOutlinedTextStyle = OutlinedTextStyle(
    style = TextStyle(
        fontSize = 18.sp,
        color = Color.White,
    ),
    outlinedColor = SFColors.primary._300,
    outlinedWidth = 3.dp,
)

@Composable
fun HomeScreenContent(
    state: HomeState,
    onBannerIconPositioned: (LayoutCoordinates) -> Unit = {},
    onTextRecognized: (String) -> Unit = {},
    onBarcodeRecognized: (String) -> Unit = {},
    onAddProduct: () -> Unit = {},
    onDeleteProduct: () -> Unit = {},
) {
    val topInsetPadding = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                SFColors.primary._300,
            )
    ) {
        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .padding(top = topInsetPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.home_title_1),
                    style = TextStyle(
                        fontSize = 22.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                )
                Text(
                    text = stringResource(Res.string.home_title_2),
                    style = TextStyle(
                        fontSize = 22.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(
                        color = ColorsTheme.colors.backgroundColor,
                    ),
                contentAlignment = Center,
            ) {
                CameraView(
                    modifier = Modifier.fillMaxSize(),
                    onTextRecognized = onTextRecognized,
                    onBarcodeRecognized = onBarcodeRecognized,
                )

                this@Column.FadeAnimatable(
                    modifier = Modifier
                        .align(BiasAlignmentExt.horizontalCenter(-1f))
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp),
                    value = state.productBanner,
                ) {
                    HomeScreenProductBanner(
                        state = it,
                        onBannerIconPositioned = onBannerIconPositioned,
                        onAddProduct = onAddProduct,
                        onDeleteProduct = onDeleteProduct,
                    )
                }
            }
        }

        Crossfade(
            modifier = Modifier.align(BiasAlignmentExt.horizontalCenter(0.7f)),
            targetState = state.bottomBannerText,
        ) {
            if (it != null) {
                OutlinedText(
                    text = it,
                    style = baseOutlinedTextStyle,
                )
            }
        }
    }
}

@Composable
fun <T> ColumnScope.FadeAnimatable(
    modifier: Modifier,
    value: T?,
    content: @Composable (T) -> Unit,
) {
    var lastValidValue by remember { mutableStateOf<T?>(null) }
    LaunchedEffect(value) {
        if (value != null) {
            lastValidValue = value
        }
    }

    AnimatedVisibility(
        modifier = modifier,
        visible = value != null,
        enter = fadeIn(tween(400)),
        exit = fadeOut(tween(400)),
    ) {
        lastValidValue?.let { content(it) }
    }
}

@Composable
fun HomeScreenProductBanner(
    modifier: Modifier = Modifier,
    state: HomeProductBannerState,
    onBannerIconPositioned: (LayoutCoordinates) -> Unit = {},
    onAddProduct: () -> Unit = {},
    onDeleteProduct: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .background(
                color = SFColors.primary._100,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(vertical = 8.dp, horizontal = 8.dp),
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = CenterVertically,
        ) {
            AsyncImage(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .size(64.dp)
                    .placeholder(
                        enable = state.thumbnail == null,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .onGloballyPositioned(onBannerIconPositioned),
                model = state.thumbnail,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    modifier = Modifier.placeholder(
                        enable = state.name == null,
                        color = Color.LightGray,
                        shimmerColor = Color.White,
                    ),
                    text = state.name.minForPlaceholder(size = 10),
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = SFColors.secondary._500,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Text(
                        text = stringResource(Res.string.common_expired_at),
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = SFColors.secondary._500,
                        ),
                    )
                    Text(
                        modifier = Modifier.placeholder(
                            enable = state.expirationDate == null,
                            color = Color.LightGray,
                            shimmerColor = Color.White,
                        ),
                        text = state.expirationDate.minForPlaceholder(size = 10),
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = SFColors.secondary._500,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.height(36.dp).fillMaxWidth(),
        ) {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = stringResource(Res.string.cta_cancel),
                type = ButtonType.SECONDARY,
                onClick = onDeleteProduct,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = stringResource(Res.string.cta_add),
                type = ButtonType.PRIMARY,
                onClick = onAddProduct,
                enable = state.expirationDate != null,
                isLoading = state.buttonIsLoading,
            )

        }
    }
}

fun String?.minForPlaceholder(
    size: Int,
) = this ?: "a".repeat(size)


class DpOffsetAnimationDataToVector(
    private val density: Density,
) : TwoWayConverter<DpOffset, AnimationVector2D> {
    override val convertFromVector: (AnimationVector2D) -> DpOffset = {
        with(density) {
            DpOffset(
                x = it.v1.toDp(),
                y = it.v2.toDp(),
            )
        }
    }

    override val convertToVector: (DpOffset) -> AnimationVector2D = {
        with(density) {
            AnimationVector2D(
                v1 = it.x.toPx(),
                v2 = it.y.toPx(),
            )
        }
    }
}

data class DpYOffsetAlpha(
    val offset: Dp,
    val alpha: Float,
)

class DpYOffsetAlphaAnimationDataToVector(
    private val density: Density,
) : TwoWayConverter<DpYOffsetAlpha, AnimationVector2D> {
    override val convertFromVector: (AnimationVector2D) -> DpYOffsetAlpha = {
        with(density) {
            DpYOffsetAlpha(
                offset = it.v1.toDp(),
                alpha = it.v2,
            )
        }
    }

    override val convertToVector: (DpYOffsetAlpha) -> AnimationVector2D = {
        with(density) {
            AnimationVector2D(
                v1 = it.offset.toPx(),
                v2 = it.alpha,
            )
        }
    }
}

object OffsetAnimationDataToVector : TwoWayConverter<Offset, AnimationVector2D> {
    override val convertFromVector: (AnimationVector2D) -> Offset = {
        Offset(
            x = it.v1,
            y = it.v2,
        )
    }

    override val convertToVector: (Offset) -> AnimationVector2D = {
        AnimationVector2D(
            v1 = it.x,
            v2 = it.y,
        )
    }
}

@Composable
internal inline fun animatableDpOffset(
    initialValue: DpYOffsetAlpha = DpYOffsetAlpha(0.dp, 1f),
    label: String = "DpYOffsetAlpha",
): Animatable<DpYOffsetAlpha, AnimationVector2D> {
    val density = LocalDensity.current
    return remember {
        Animatable(
            initialValue = initialValue,
            typeConverter = DpYOffsetAlphaAnimationDataToVector(density),
            label = label,
        )
    }
}


@Composable
internal inline fun animatableDpOffsetAlpha(
    initialValue: DpOffset = DpOffset.Zero,
    label: String = "DpOffsetAnimation",
): Animatable<DpOffset, AnimationVector2D> {
    val density = LocalDensity.current
    return remember {
        Animatable(
            initialValue = initialValue,
            typeConverter = DpOffsetAnimationDataToVector(density),
            label = label,
        )
    }
}

@Composable
internal inline fun animatableOffset(
    initialValue: Offset = Offset.Zero,
    label: String = "OffsetAnimation",
): Animatable<Offset, AnimationVector2D> = remember {
    Animatable(
        initialValue = initialValue,
        typeConverter = OffsetAnimationDataToVector,
        label = label,
    )
}