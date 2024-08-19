package com.moliveira.app.smartfridge.modules.home.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import coil3.compose.AsyncImage
import com.molive.sdk.extensions.BiasAlignmentExt
import com.molive.sdk.loading.placeholder
import com.molive.sdk.text.OutlinedText
import com.molive.sdk.text.OutlinedTextStyle
import com.moliveira.app.smartfridge.modules.camera.CameraView
import com.moliveira.app.smartfridge.modules.food.FoodRepository
import com.moliveira.app.smartfridge.modules.food.domain.FoodModel
import com.moliveira.app.smartfridge.modules.sdk.BaseScreenModel
import com.moliveira.app.smartfridge.modules.sdk.ObserveUiEffect
import com.moliveira.app.smartfridge.modules.sdk.localizedString
import io.github.aakira.napier.Napier
import io.ktor.util.logging.KtorSimpleLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char

class HomeScreenDestination : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<HomeViewModel>()
        HomeScreen(viewModel)
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

class HomeViewStateConverter {
    private val dateFormatter = LocalDate.Format {
        dayOfMonth()
        char('/')
        monthNumber()
        char('/')
        yearTwoDigits(2000)
    }

    operator fun invoke(
        internalState: HomeInternalState,
    ): HomeState = when (internalState) {
        is HomeInternalState.Idle -> {
            HomeState()
        }

        is HomeInternalState.ProductInSearch -> {
            HomeState(
                productBanner = HomeProductBannerState(
                    name = null,
                    thumbnail = null,
                    expirationDate = null,
                ),
            )
        }

        is HomeInternalState.ProductFound -> {
            HomeState(
                bottomBannerText = "Scanner la date de péremption",
                productBanner = HomeProductBannerState(
                    name = internalState.foodModel.name.localizedString(),
                    thumbnail = internalState.foodModel.thumbnail,
                    expirationDate = null,
                ),
            )
        }

        is HomeInternalState.DateSettled -> {
            HomeState(
                productBanner = HomeProductBannerState(
                    name = internalState.foodModel.name.localizedString(),
                    thumbnail = internalState.foodModel.thumbnail,
                    expirationDate = dateFormatter.format(internalState.date),
                ),
            )
        }
    }
}

sealed class HomeUiEffect {
    data class DisplayMessage(
        val message: String,
    ) : HomeUiEffect()
}

class HomeViewModel(
    private val foodRepository: FoodRepository,
    private val converter: HomeViewStateConverter,
) : BaseScreenModel<HomeState, HomeUiEffect>(
    HomeState()
) {

    private val internalStateFlow = MutableStateFlow<HomeInternalState>(HomeInternalState.Idle)
    override val uiStateProvider: Flow<HomeState>
        get() = internalStateFlow.map(converter::invoke)

    fun onBarcodeRecognized(text: String) {
        Napier.w("onBarcodeRecognized: $text")
        viewModelScope.launch(Dispatchers.Default) {
            if (internalStateFlow.value !is HomeInternalState.Idle) return@launch

            internalStateFlow.value = HomeInternalState.ProductInSearch
            Napier.w("onBarcodeRecognized: getFoodById $text")
            foodRepository.getFoodById(text)
                .onSuccess {
                    Napier.w("onBarcodeRecognized: getFoodById success $it")
                    internalStateFlow.value = HomeInternalState.ProductFound(it)
                }
                .onFailure {
                    Napier.w("onBarcodeRecognized: getFoodById failure $it")
                    HomeInternalState.Idle
                    sendUiEffect(HomeUiEffect.DisplayMessage("Product not found"))
                }
        }
    }


    private val dateRegex = Regex("""\d{1,2}/\d{1,2}/\d{2,4}""")
    private val pointDateRegex = Regex("""\d{1,2}.\d{1,2}.\d{2,4}""")
    fun onTextRecognized(text: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val productFoundState =
                (internalStateFlow.value as? HomeInternalState.ProductFound) ?: return@launch

            val textMatch = pointDateRegex.find(text)
                ?.value?.replace(".", "/")
                ?.also { Napier.w("onTextRecognized: date found pointDateRegex $it") }
                ?: dateRegex.find(text)?.value
                    ?.also { Napier.w("onTextRecognized: date found slash $it") }
                ?: return@launch

            val yearSize = textMatch.split("/").last().length
            Napier.w("onTextRecognized: date found $textMatch, yearSize: $yearSize")
            runCatching {
                val dateFormat = LocalDate.Format {
                    dayOfMonth()
                    char('/')
                    monthNumber()
                    char('/')
                    if (yearSize == 2) yearTwoDigits(2000) else year()
                }
                LocalDate.parse(text, dateFormat)
            }.onSuccess {
                Napier.i("onTextRecognized: date parsed $it")
                internalStateFlow.value = HomeInternalState.DateSettled(
                    foodModel = productFoundState.foodModel,
                    date = it,
                )
            }
                .onFailure {
                    Napier.w("onTextRecognized: date parse failure $it")
                }
        }
    }

    fun onAddProduct() {
        viewModelScope.launch(Dispatchers.Default) {
            val productFoundState =
                (internalStateFlow.value as? HomeInternalState.DateSettled) ?: return@launch
            Napier.w("onAddProduct: $productFoundState")
            // TODO
            internalStateFlow.value = HomeInternalState.Idle
        }
    }

    fun onDeleteProduct() {
        Napier.w("onDeleteProduct")
        internalStateFlow.value = HomeInternalState.Idle
    }
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
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
    }
}

val baseOutlinedTextStyle = OutlinedTextStyle(
    style = TextStyle(
        fontSize = 16.sp,
        color = Color.White,
    ),
    outlinedColor = Color.Black,
    outlinedWidth = 4.dp,
)

@Composable
fun HomeScreenContent(
    state: HomeState,
    onTextRecognized: (String) -> Unit,
    onBarcodeRecognized: (String) -> Unit,
    onAddProduct: () -> Unit,
    onDeleteProduct: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        CameraView(
            modifier = Modifier.fillMaxSize(),
            onTextRecognized = onTextRecognized,
            onBarcodeRecognized = onBarcodeRecognized,
        )

        state.productBanner?.let {
            HomeScreenProductBanner(
                modifier = Modifier
                    .align(BiasAlignmentExt.horizontalCenter(-0.7f))
                    .fillMaxWidth(0.9f),
                state = it,
                onAddProduct = onAddProduct,
                onDeleteProduct = onDeleteProduct,
            )
        }
    }
}

@Composable
fun HomeScreenProductBanner(
    modifier: Modifier = Modifier,
    state: HomeProductBannerState,
    onAddProduct: () -> Unit,
    onDeleteProduct: () -> Unit,
) {
    Row(
        modifier = modifier
            .background(
                color = Color.Gray.copy(alpha = 0.8f),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = CenterVertically,
    ) {
        AsyncImage(
            modifier = Modifier
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
                    color = Color.White,
                ),
                text = state.name.minForPlaceholder(size = 10),
                style = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                modifier = Modifier.placeholder(
                    enable = state.expirationDate == null,
                    color = Color.White,
                ),
                text = state.expirationDate.minForPlaceholder(size = 10),
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color.White,
                ),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        AnimatedVisibility(
            modifier = Modifier.size(32.dp),
            visible = state.expirationDate != null,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(shape = CircleShape)
                    .background(
                        color = Color.Green,
                    )
                    .clickable(onClick = onAddProduct),
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            Modifier
                .size(32.dp)
                .clip(shape = CircleShape)
                .background(
                    color = Color.Red,
                )
                .clickable(onClick = onDeleteProduct),
        )
    }
}

fun String?.minForPlaceholder(
    size: Int,
) = this ?: "a".repeat(size)