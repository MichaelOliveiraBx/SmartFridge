package com.moliveira.app.smartfridge.modules.home.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.molive.sdk.extensions.BiasAlignmentExt
import com.molive.sdk.loading.placeholder
import com.molive.sdk.text.OutlinedText
import com.molive.sdk.text.OutlinedTextStyle
import com.moliveira.app.smartfridge.modules.camera.CameraView
import com.moliveira.app.smartfridge.modules.design.Button
import com.moliveira.app.smartfridge.modules.design.ButtonType
import com.moliveira.app.smartfridge.modules.design.ColorsTheme
import com.moliveira.app.smartfridge.modules.food.domain.FoodModel
import com.moliveira.app.smartfridge.modules.sdk.ObserveUiEffect
import kotlinx.coroutines.delay
import kotlinx.datetime.LocalDate

class HomeScreenDestination : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<HomeViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        HomeScreen(
            viewModel = viewModel,
            goToDetails = {
                navigator.push(FoodsDetailsScreenDestination())
            }
        )
    }
}

data class HomeProductBannerState(
    val name: String?,
    val thumbnail: String?,
    val expirationDate: String?,
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
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    goToDetails: () -> Unit = {},
) {
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
    var bottomMessage by remember { mutableStateOf<String?>(null) }
    viewModel.ObserveUiEffect {
        when (it) {
            is HomeUiEffect.DisplayMessage -> bottomMessage = it.message
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        HomeScreenContent(
            state = state,
            onTextRecognized = viewModel::onTextRecognized,
            onBarcodeRecognized = viewModel::onBarcodeRecognized,
            onAddProduct = viewModel::onAddProduct,
            onDeleteProduct = viewModel::onDeleteProduct,
        )

        Crossfade(
            modifier = Modifier.align(BiasAlignmentExt.horizontalCenter(0.7f)),
            targetState = bottomMessage,
        ) {
            if (it != null) {
                LaunchedEffect(key1 = it) {
                    delay(1500)
                    bottomMessage = null
                }
                OutlinedText(
                    text = it,
                    style = baseOutlinedTextStyle,
                )
            }
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .align(
                    alignment = BiasAlignment(
                        horizontalBias = 0.9f,
                        verticalBias = 0.9f,
                    ),
                )
                .clip(CircleShape)
                .background(
                    color = ColorsTheme.colors.primaryColor,
                )
                .clickable { goToDetails() }
                .padding(8.dp),
        ) {
            Icon(
                modifier = Modifier.fillMaxSize(),
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                tint = ColorsTheme.colors.textOnPrimary,
            )
        }
    }
}

val baseOutlinedTextStyle = OutlinedTextStyle(
    style = TextStyle(
        fontSize = 18.sp,
        color = Color.White,
    ),
    outlinedColor = Color(0xFF2B7E6E),
    outlinedWidth = 3.dp,
)

@Composable
fun HomeScreenContent(
    state: HomeState,
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
                brush = Brush.verticalGradient(
                    0.0f to Color(0xFF8ADACA),
                    0.4f to Color(0xFF588B81),
                ),
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
                    text = "Qu'est ce qu'on ajoute",
                    style = TextStyle(
                        fontSize = 22.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                )
                Text(
                    text = "aujourd'hui ?",
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
    onAddProduct: () -> Unit = {},
    onDeleteProduct: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .background(
                color = ColorsTheme.colors.backgroundColor,
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
                    ),
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
                        color = ColorsTheme.colors.textOnBackground,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier.placeholder(
                        enable = state.expirationDate == null,
                        color = Color.LightGray,
                        shimmerColor = Color.White,
                    ),
                    text = state.expirationDate.minForPlaceholder(size = 10),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = ColorsTheme.colors.textOnBackground,
                    ),
                )
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
                text = "Annuler",
                type = ButtonType.SECONDARY,
                onClick = onDeleteProduct,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                text = "Ajouter",
                type = ButtonType.PRIMARY,
                onClick = onAddProduct,
                enable = state.expirationDate != null,
            )

        }
    }
}

fun String?.minForPlaceholder(
    size: Int,
) = this ?: "a".repeat(size)