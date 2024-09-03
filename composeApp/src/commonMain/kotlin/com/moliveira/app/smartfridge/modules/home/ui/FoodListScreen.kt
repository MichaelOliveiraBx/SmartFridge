package com.moliveira.app.smartfridge.modules.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.moliveira.app.smartfridge.modules.design.ColorsTheme
import com.moliveira.app.smartfridge.modules.food.database.FoodDatabase
import com.moliveira.app.smartfridge.modules.sdk.BaseScreenModel
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.format

data class FoodsDetailsScreenState(
    val foodItems: List<FoodItemState> = emptyList(),
)

class FoodsDetailsScreenDestination : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinScreenModel<FoodsDetailsViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
        FoodsDetailsScreen(
            state = state,
            onBack = { navigator.pop() }
        )
    }
}

class FoodsDetailsViewModel(
    private val database: FoodDatabase,
) : BaseScreenModel<FoodsDetailsScreenState, Unit>(
    FoodsDetailsScreenState()
) {

    override val uiStateProvider: Flow<FoodsDetailsScreenState>
        get() = flow {
            val items = database.getAllUserFood()
                .map {
                    FoodItemState(
                        name = it.name,
                        thumbnail = it.thumbnail,
                        expirationDate = it.expirationDate.format(SimpleDateFormatter),
                    )
                }
            emit(
                FoodsDetailsScreenState(
                    foodItems = items,
                )
            )
        }
}

@Composable
fun FoodsDetailsScreen(
    state: FoodsDetailsScreenState,
    onBack: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF7FCABB),
                        Color(0xFF588B81),
                    )
                )
            ),
    ) {
        val topInsetPadding = WindowInsets.statusBars
            .asPaddingValues()
            .calculateTopPadding()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp + topInsetPadding)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp,
                    )
                )
                .padding(top = topInsetPadding),
        ) {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = 16.dp)
                    .clickable(onClick = onBack),
                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                contentDescription = null,
                tint = ColorsTheme.colors.primaryColor,
            )
            Text(
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleMedium,
                text = "Votre frigo",
            )
        }
        LaunchedEffect(state.foodItems){
            Napier.d("FoodsDetailsScreen: ${state.foodItems}")
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(0.dp, 8.dp, 0.dp, 0.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.foodItems) {
                FoodItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    state = it,
                )
            }
        }
    }
}

data class FoodItemState(
    val name: String,
    val thumbnail: String,
    val expirationDate: String,
)

@Composable
fun FoodItem(
    modifier: Modifier = Modifier,
    state: FoodItemState,
) {
    Row(
        modifier = modifier
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
    ) {
        AsyncImage(
            modifier = Modifier.size(48.dp).clip(MaterialTheme.shapes.small),
            model = state.thumbnail,
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            modifier = Modifier,
        ) {
            Text(
                text = state.name,
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = state.expirationDate,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}