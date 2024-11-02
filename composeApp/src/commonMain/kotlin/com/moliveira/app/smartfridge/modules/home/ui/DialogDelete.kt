package com.moliveira.app.smartfridge.modules.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.moliveira.app.smartfridge.Res
import com.moliveira.app.smartfridge.common_delete
import com.moliveira.app.smartfridge.cta_cancel
import com.moliveira.app.smartfridge.dialog_delete_description
import com.moliveira.app.smartfridge.modules.design.ButtonType
import com.moliveira.app.smartfridge.modules.theme.SFColors
import org.jetbrains.compose.resources.stringResource


@Composable
fun DialogDelete(
    product: String,
    onValidateClick: () -> Unit = {},
    onDismissRequest: () -> Unit = {},
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = SFColors.primary._100,
                    shape = RoundedCornerShape(16.dp),
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = product,
                style = MaterialTheme.typography.titleSmall,
                color = SFColors.secondary._500,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.dialog_delete_description),
                style = MaterialTheme.typography.bodyLarge,
                color = SFColors.secondary._500,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                com.moliveira.app.smartfridge.modules.design.Button(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    text = stringResource(Res.string.cta_cancel),
                    type = ButtonType.SECONDARY,
                    onClick = onDismissRequest,
                )
                Spacer(modifier = Modifier.width(8.dp))
                com.moliveira.app.smartfridge.modules.design.Button(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    text = stringResource(Res.string.common_delete),
                    type = ButtonType.PRIMARY,
                    onClick = { onValidateClick() },
                )
            }
        }
    }
}