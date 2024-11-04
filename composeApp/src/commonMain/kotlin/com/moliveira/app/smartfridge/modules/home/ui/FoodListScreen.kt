package com.moliveira.app.smartfridge.modules.home.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.molive.sdk.architecture.DisposableEffectWithLifecycle
import com.moliveira.app.smartfridge.Res
import com.moliveira.app.smartfridge.by_date_added
import com.moliveira.app.smartfridge.by_date_expired
import com.moliveira.app.smartfridge.fridge_title_1
import com.moliveira.app.smartfridge.fridge_title_2
import com.moliveira.app.smartfridge.modules.sdk.CrossfadeKey
import com.moliveira.app.smartfridge.modules.theme.SFColors
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

data class FoodsDetailsScreenState(
    val foodItems: List<FoodsDetailsItem> = emptyList(),
    val filterType: FilterType = FilterType.Expired,
)

sealed class FoodsDetailsItem {
    abstract val uiId: String

    data class Food(
        val state: FoodItemState = FoodItemState(),
    ) : FoodsDetailsItem() {
        override val uiId = state.id
    }

    data class Divider(
        val title: String,
    ) : FoodsDetailsItem() {
        override val uiId = title
    }
}

class FoodsDetailsScreenDestination : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<FoodsDetailsViewModel>()

        DisposableEffectWithLifecycle(
            onResume = viewModel::onResume,
        )

        val navigator = LocalNavigator.currentOrThrow
        val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
        FoodsDetailsScreen(
            state = state,
            onBack = { navigator.pop() },
            onNotificationClick = viewModel::onNotificationClick,
            onDeleteClick = viewModel::onDeleteClick,
            onFilterClick = viewModel::onFilterClick,
        )
    }
}

@Composable
fun FoodsDetailsScreen(
    state: FoodsDetailsScreenState,
    onBack: () -> Unit = {},
    onNotificationClick: (String) -> Unit = {},
    onDeleteClick: (String) -> Unit = {},
    onFilterClick: (FilterType) -> Unit = {},
) {
    var dialogDelete by remember { mutableStateOf<Pair<String, String>?>(null) }

    dialogDelete?.let { info ->
        DialogDelete(
            product = info.second,
            onValidateClick = {
                onDeleteClick(info.first)
                dialogDelete = null
            },
            onDismissRequest = { dialogDelete = null },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = SFColors.primary._300,
            ),
    ) {
        Header(
            onBack = onBack,
            filterType = state.filterType,
            onFilterClick = onFilterClick,
        )
        CrossfadeKey(
            modifier = Modifier.fillMaxWidth().weight(1f),
            targetState = state.foodItems,
            animationSpec = tween(500),
            contentKey = { it.map { it.uiId } },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(0.dp, 12.dp, 0.dp, 0.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(it) { item ->
                    when (item) {
                        is FoodsDetailsItem.Divider ->
                            DividerItem(title = item.title)

                        is FoodsDetailsItem.Food -> {
                            FoodItem(
                                state = item.state,
                                onNotificationClick = onNotificationClick,
                                onDeleteClick = {
                                    dialogDelete = item.state.id to item.state.name
                                },
                            )

                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DividerItem(
    title: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(
                    color = Color.White,
                ),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .background(
                    color = Color.White,
                ),
        )
    }
}

@Composable
private fun Header(
    modifier: Modifier = Modifier,
    filterType: FilterType,
    onBack: () -> Unit = {},
    onFilterClick: (FilterType) -> Unit = {},
) {
    val topInsetPadding = WindowInsets.statusBars
        .asPaddingValues()
        .calculateTopPadding()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = SFColors.primary._200,
                shape = RoundedCornerShape(
                    bottomStart = 24.dp,
                    bottomEnd = 24.dp,
                )
            )
            .padding(top = topInsetPadding),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
                .height(40.dp + topInsetPadding),
        ) {
            Button(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = 8.dp),
                onClick = onBack,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                ),
                contentPadding = PaddingValues(0.dp),
            ) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                    contentDescription = null,
                )
            }

            Row(
                modifier = Modifier.align(Alignment.Center),
            ) {
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    text = stringResource(Res.string.fridge_title_1),
                    color = Color.White,
                )
                Text(
                    style = MaterialTheme.typography.titleMedium
                        .copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    text = stringResource(Res.string.fridge_title_2),
                    color = Color.White,
                )
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.Start),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Spacer(modifier = Modifier.width(24.dp))
            FilterItem(
                filterType = FilterType.Expired,
                isSelected = filterType == FilterType.Expired,
                onClick = { onFilterClick(FilterType.Expired) },
            )
            Spacer(modifier = Modifier.width(12.dp))
            FilterItem(
                filterType = FilterType.Added,
                isSelected = filterType == FilterType.Added,
                onClick = { onFilterClick(FilterType.Added) },
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FilterItem(
    filterType: FilterType,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) SFColors.secondary._500 else Color.White
    )
    val textColor by animateColorAsState(
        if (isSelected) Color.White else SFColors.secondary._500
    )

    Button(
        modifier = Modifier,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = Color.Transparent,
        ),
        contentPadding = PaddingValues(
            horizontal = 12.dp,
            vertical = 2.dp,
        ),
    ) {
        Text(
            text = filterType.displayedName(),
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
        )
    }
}

@Composable
fun FilterType.displayedName() = when (this) {
    FilterType.Added -> stringResource(Res.string.by_date_added)
    FilterType.Expired -> stringResource(Res.string.by_date_expired)
}