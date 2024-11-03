package com.moliveira.app.smartfridge.modules.home.ui

import com.moliveira.app.smartfridge.Res
import com.moliveira.app.smartfridge.common_expired
import com.moliveira.app.smartfridge.modules.food.FoodRepository
import com.moliveira.app.smartfridge.modules.food.database.FoodDatabase
import com.moliveira.app.smartfridge.modules.food.domain.UserFoodModel
import com.moliveira.app.smartfridge.modules.food.domain.UserNotificationModel
import com.moliveira.app.smartfridge.modules.notification.NotificationService
import com.moliveira.app.smartfridge.modules.notification.handleNotificationTime
import com.moliveira.app.smartfridge.modules.sdk.BaseScreenModel
import com.moliveira.app.smartfridge.more_late
import com.moliveira.app.smartfridge.notification_title
import com.moliveira.app.smartfridge.notification_title_description
import com.moliveira.app.smartfridge.this_month
import com.moliveira.app.smartfridge.this_week
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString

enum class FilterType {
    Added,
    Expired,
}

class FoodsDetailsViewModel(
    private val database: FoodDatabase,
    private val repository: FoodRepository,
    private val notificationService: NotificationService,
) : BaseScreenModel<FoodsDetailsScreenState, Unit>(
    FoodsDetailsScreenState(
        foodItems = List(6) {
            FoodsDetailsItem.Food()
        }
    )
) {

    private val notificationFlow = MutableStateFlow<List<UserNotificationModel>?>(null)
    private val filterTypeFlow = MutableStateFlow(FilterType.Expired)

    override val uiStateProvider: Flow<FoodsDetailsScreenState>
        get() = combine(
            repository.userFoodFlow.mapNotNull { it },
            notificationFlow.mapNotNull { it },
            filterTypeFlow,
        ) { userFood, notifications, filterType ->
            convert(
                foods = userFood,
                notifications = notifications,
                filterType = filterType,
            )
        }

    private suspend fun convert(
        foods: List<UserFoodModel>,
        notifications: List<UserNotificationModel>,
        filterType: FilterType,
    ): FoodsDetailsScreenState {
        val listStates = when (filterType) {
            FilterType.Added -> foods.map { it.toState(notifications) }
            FilterType.Expired -> {
                val listSorted = foods.sortedBy { it.expirationDate }.toMutableList()

                val timeZone = TimeZone.currentSystemDefault()
                val now = Clock.System.now().toLocalDateTime(timeZone).date

                val expired = listSorted.takeAllAndRemove { it.expirationDate.isBeforeNow(now) }
                val thisWeek = listSorted.takeAllAndRemove { it.expirationDate.isThisWeek(now) }
                val thisMonth = listSorted.takeAllAndRemove { it.expirationDate.isThisMonth(now) }

                val mutableList = mutableListOf<FoodsDetailsItem>()
                if (thisWeek.isNotEmpty()) {
                    mutableList.add(FoodsDetailsItem.Divider(getString(Res.string.this_week)))
                    mutableList.addAll(thisWeek.map { it.toState(notifications) })
                }
                if (thisMonth.isNotEmpty()) {
                    mutableList.add(FoodsDetailsItem.Divider(getString(Res.string.this_month)))
                    mutableList.addAll(thisMonth.map { it.toState(notifications) })
                }
                if (listSorted.isNotEmpty()) {
                    mutableList.add(FoodsDetailsItem.Divider(getString(Res.string.more_late)))
                    mutableList.addAll(listSorted.map { it.toState(notifications) })
                }
                if (expired.isNotEmpty()) {
                    mutableList.add(FoodsDetailsItem.Divider(getString(Res.string.common_expired)))
                    mutableList.addAll(expired.map { it.toState(notifications) })
                }
                mutableList
            }
        }

        return FoodsDetailsScreenState(
            foodItems = listStates,
            filterType = filterType,
        )
    }

    private fun UserFoodModel.toState(
        notifications: List<UserNotificationModel>,
    ) = FoodsDetailsItem.Food(
        state = FoodItemState(id = id,
            name = name,
            thumbnail = thumbnail,
            expirationDate = expirationDate.format(SimpleDateFormatter),
            notificationEnable = notifications.any { it.id == id }
        )
    )

    fun onResume() {
        refreshNotif()
    }

    private fun refreshNotif() {
        viewModelScope.launch(Dispatchers.Default) {
            notificationFlow.value = database.allNotificationModel()
        }
    }

    fun onNotificationClick(id: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val product = repository.userFood?.find { it.id == id } ?: return@launch
            Napier.d("onNotificationClick: $id")
            if (database.hasNotificationById(id)) {
                Napier.d("hasNotification: true")
                notificationFlow.value = notificationFlow.value?.filter { it.id != id }
                notificationService.cancelNotification(id)
                database.removeNotificationId(id)
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
                            notificationFlow.value = notificationFlow.value.orEmpty() + listOf(
                                UserNotificationModel(
                                    id = id,
                                    uuid = it,
                                )
                            )
                            database.addNotificationId(id, it)
                        }
                    }
            }
        }
    }

    fun onDeleteClick(id: String) {
        viewModelScope.launch(Dispatchers.Default) {
            Napier.d("onDeleteClick: $id")
            repository.userFood?.find { it.id == id } ?: return@launch
            notificationService.cancelNotification(id)
            repository.removeUserFood(id)
        }
    }

    fun onFilterClick(
        filterType: FilterType,
    ) {
        filterTypeFlow.value = filterType
    }
}

private fun LocalDate.isBeforeNow(now: LocalDate): Boolean {
    return this < now
}

private fun LocalDate.isThisWeek(now: LocalDate): Boolean {
    return this < now.plus(DatePeriod(days = 7))
}

private fun LocalDate.isThisMonth(now: LocalDate): Boolean {
    return this < now.plus(DatePeriod(months = 1))
}

private fun <T> MutableList<T>.takeAllAndRemove(predicate: (T) -> Boolean): List<T> {
    val list = mutableListOf<T>()
    val iterator = iterator()
    while (iterator.hasNext()) {
        val item = iterator.next()
        if (predicate(item)) {
            list.add(item)
            iterator.remove()
        }
    }
    return list
}