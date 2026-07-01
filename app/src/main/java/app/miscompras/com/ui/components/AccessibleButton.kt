package app.miscompras.com.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.miscompras.com.ui.theme.AccessibilityDefaults

enum class ButtonStyle {
    Filled,
    Outlined
}

@Composable
fun AccessibleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    style: ButtonStyle = ButtonStyle.Filled
) {
    val shape = RoundedCornerShape(16.dp)
    val contentPadding = PaddingValues(
        horizontal = AccessibilityDefaults.ButtonPadding * 1.5f,
        vertical = AccessibilityDefaults.ButtonPadding
    )
    val semanticsModifier = modifier.semantics {
        contentDescription?.let { this.contentDescription = it }
    }

    Box(
        modifier = semanticsModifier,
        contentAlignment = Alignment.Center
    ) {
        when (style) {
            ButtonStyle.Filled -> {
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = AccessibilityDefaults.MinTouchTarget),
                    enabled = enabled,
                    shape = shape,
                    contentPadding = contentPadding,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 0.dp
                    )
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            ButtonStyle.Outlined -> {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = AccessibilityDefaults.MinTouchTarget),
                    enabled = enabled,
                    shape = shape,
                    contentPadding = contentPadding,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
