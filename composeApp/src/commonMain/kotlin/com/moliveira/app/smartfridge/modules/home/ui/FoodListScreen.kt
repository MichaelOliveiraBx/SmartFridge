package com.moliveira.app.smartfridge.modules.home.ui

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import coil3.compose.AsyncImage
import com.moliveira.app.smartfridge.Res
import com.moliveira.app.smartfridge.common_expired_at
import com.moliveira.app.smartfridge.fridge_title_1
import com.moliveira.app.smartfridge.fridge_title_2
import com.moliveira.app.smartfridge.modules.food.database.FoodDatabase
import com.moliveira.app.smartfridge.modules.food.domain.UserFoodModel
import com.moliveira.app.smartfridge.modules.food.domain.UserNotificationModel
import com.moliveira.app.smartfridge.modules.notification.NotificationService
import com.moliveira.app.smartfridge.modules.notification.handleNotificationTime
import com.moliveira.app.smartfridge.modules.sdk.BaseScreenModel
import com.moliveira.app.smartfridge.modules.theme.SFColors
import com.moliveira.app.smartfridge.notification_title
import com.moliveira.app.smartfridge.notification_title_description
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.datetime.format
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

data class FoodsDetailsScreenState(
    val foodItems: List<FoodItemState> = emptyList(),
)

class FoodsDetailsScreenDestination : Screen {
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<FoodsDetailsViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()
        FoodsDetailsScreen(
            state = state,
            onBack = { navigator.pop() },
            onNotificationClick = viewModel::onNotificationClick,
        )
    }
}

class FoodsDetailsViewModel(
    private val database: FoodDatabase,
    private val notificationService: NotificationService,
) : BaseScreenModel<FoodsDetailsScreenState, Unit>(
    FoodsDetailsScreenState()
) {

    private val userFoodFlow = MutableStateFlow<List<UserFoodModel>?>(null)
    private val notificationFlow = MutableStateFlow<List<UserNotificationModel>?>(null)

    override val uiStateProvider: Flow<FoodsDetailsScreenState>
        get() = combine(
            userFoodFlow.mapNotNull { it },
            notificationFlow.mapNotNull { it },
        ) { userFood, notifications ->
            Napier.d("uiStateProvider: FOOD $userFood")
            Napier.d("uiStateProvider: NOTIFS $notifications")
            FoodsDetailsScreenState(
                foodItems = userFood.map { food ->
                    FoodItemState(
                        id = food.id,
                        name = food.name,
                        thumbnail = food.thumbnail,
                        expirationDate = food.expirationDate.format(SimpleDateFormatter),
                        notificationEnable = notifications.any { it.id == food.id }
                    )
                }
            )
        }

    init {
        viewModelScope.launch(Dispatchers.Default) {
            userFoodFlow.value = database.getAllUserFood()
        }
        refreshNotif()
    }

    private fun refreshNotif() {
        viewModelScope.launch(Dispatchers.Default) {
            notificationFlow.value = database.allNotificationModel()
        }
    }

    fun onNotificationClick(id: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val product = userFoodFlow.value?.find { it.id == id } ?: return@launch
            Napier.d("onNotificationClick: $id")
            if (database.hasNotificationById(id)) {
                Napier.d("hasNotification: true")
                notificationService.cancelNotification(id)
                database.removeNotificationId(id)
                notificationFlow.value = notificationFlow.value?.filter { it.id != id }
            } else {
                Napier.d("hasNotification: false")
                product.expirationDate.handleNotificationTime()
                    ?.let {
                        notificationService.scheduleNotification(
                            title = getString(Res.string.notification_title),
                            body = getString(
                                Res.string.notification_title_description,
                                product.name
                            ),
                            icon = null,
                            localDateTime = it,
                        ).onSuccess {
                            Napier.d("scheduleNotification: $it")
                            database.addNotificationId(id, it)
                            notificationFlow.value = notificationFlow.value.orEmpty() + listOf(
                                UserNotificationModel(
                                    id = id,
                                    uuid = it,
                                )
                            )
                        }
                    }
            }
        }
    }
}

@Composable
fun FoodsDetailsScreen(
    state: FoodsDetailsScreenState,
    onBack: () -> Unit = {},
    onNotificationClick: (String) -> Unit = {},
) {
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
            items(state.foodItems) {
                FoodItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    state = it,
                    onNotificationClick = onNotificationClick,
                )
            }
        }
    }
}

data class FoodItemState(
    val id: String = "",
    val name: String,
    val thumbnail: String,
    val expirationDate: String,
    val notificationEnable: Boolean = false,
)

@Composable
fun FoodItem(
    modifier: Modifier = Modifier,
    state: FoodItemState,
    onNotificationClick: (String) -> Unit,
) {
    Row(
        modifier = modifier
            .background(
                color = SFColors.primary._100,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.small)
                .background(
                    color = SFColors.primary._300,
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
                text = state.name,
                style = MaterialTheme.typography.bodyLarge,
                color = SFColors.secondary._500,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Row {
                Text(
                    text = stringResource(Res.string.common_expired_at),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SFColors.secondary._500,
                )
                Text(
                    text = state.expirationDate,
                    style = MaterialTheme.typography.bodyMedium
                        .copy(
                            fontWeight = FontWeight.Bold,
                        ),
                    color = SFColors.secondary._500,
                )
            }
        }
        val containerColor by animateColorAsState(
            targetValue = if (state.notificationEnable) SFColors.secondary._500 else SFColors.secondary._100,
        )
        Button(
            modifier = Modifier
                .size(32.dp),
            onClick = { onNotificationClick(state.id) },
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = Color.White,
            ),
            contentPadding = PaddingValues(4.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
            )
        }
    }
}