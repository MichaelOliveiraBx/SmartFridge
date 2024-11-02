package com.moliveira.app.smartfridge.modules.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import com.moliveira.app.smartfridge.fridge_title_1
import com.moliveira.app.smartfridge.fridge_title_2
import com.moliveira.app.smartfridge.modules.theme.SFColors
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

data class FoodsDetailsScreenState(
    val foodItems: List<FoodItemState> = emptyList(),
)

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
        )
    }
}

@Composable
fun FoodsDetailsScreen(
    state: FoodsDetailsScreenState,
    onBack: () -> Unit = {},
    onNotificationClick: (String) -> Unit = {},
    onDeleteClick: (String) -> Unit = {},
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
        val topInsetPadding = WindowInsets.statusBars
            .asPaddingValues()
            .calculateTopPadding()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp + topInsetPadding)
                .background(
                    color = SFColors.primary._200,
                    shape = RoundedCornerShape(
                        bottomStart = 24.dp,
                        bottomEnd = 24.dp,
                    )
                )
                .padding(top = topInsetPadding),
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

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(0.dp, 8.dp, 0.dp, 0.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.foodItems) { item ->
                FoodItem(
                    state = item,
                    onNotificationClick = onNotificationClick,
                    onDeleteClick = {
                        dialogDelete = item.id to item.name
                    },
                )
            }
        }
    }
}