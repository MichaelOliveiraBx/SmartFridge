package com.moliveira.app.smartfridge.modules.home.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import coil3.compose.AsyncImage
import com.molive.sdk.loading.minForPlaceholder
import com.molive.sdk.loading.placeholder
import com.moliveira.app.smartfridge.Res
import com.moliveira.app.smartfridge.common_delete
import com.moliveira.app.smartfridge.common_expired_at
import com.moliveira.app.smartfridge.modules.theme.SFColors
import org.jetbrains.compose.resources.stringResource


data class FoodItemState(
    val id: String = "",
    val name: String = "",
    val thumbnail: String = "",
    val expirationDate: String = "",
    val notificationEnable: Boolean = false,
)

@Composable
fun FoodItem(
    modifier: Modifier = Modifier,
    state: FoodItemState,
    onNotificationClick: (String) -> Unit,
    onDeleteClick: (String) -> Unit,
) {
    val deleteComponentWidthSize = 120.dp
    SwipeableBox(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(80.dp)
            .clip(shape = RoundedCornerShape(16.dp)),
        isEnable = state.id.isNotEmpty(),
        onTriggered = {
            onDeleteClick(state.id)
        },
        firstContent = {
            FoodItemContent(
                modifier = it.fillMaxSize(),
                state = state,
                onNotificationClick = onNotificationClick,
            )
        },
        secondContent = {
            Box(
                modifier = it
                    .width(deleteComponentWidthSize)
                    .background(
                        color = Color.Red,
                    ),
            ) {
                Text(
                    text = stringResource(Res.string.common_delete),
                    style = MaterialTheme.typography.bodyMedium
                        .copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        ),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        },
        offsetSize = deleteComponentWidthSize,
    )
}

@Composable
fun FoodItemContent(
    modifier: Modifier = Modifier,
    state: FoodItemState,
    onNotificationClick: (String) -> Unit,
) {
    Row(
        modifier = modifier
            .background(
                color = SFColors.primary._100,
            )
            .padding(
                horizontal = 16.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            modifier = Modifier
                .size(48.dp)
                .clip(MaterialTheme.shapes.small)
                .placeholder(
                    enable = state.id.isEmpty(),
                )
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
                modifier = Modifier.placeholder(enable = state.id.isEmpty()),
                text = state.name.minForPlaceholder(size = 18, enable = state.id.isEmpty()),
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
                    modifier = Modifier.placeholder(enable = state.id.isEmpty()),
                    text = state.expirationDate.minForPlaceholder(
                        size = 10,
                        enable = state.id.isEmpty()
                    ),
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