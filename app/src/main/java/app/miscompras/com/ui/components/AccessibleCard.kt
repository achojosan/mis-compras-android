package app.miscompras.com.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.miscompras.com.ui.theme.AccessibilityDefaults

@Composable
fun AccessibleCard(
    title: String? = null,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingAction: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(AccessibilityDefaults.ItemSpacing / 2),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(AccessibilityDefaults.CardPadding)
        ) {
            if (title != null || leadingIcon != null || trailingAction != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = AccessibilityDefaults.ItemSpacing),
                    horizontalArrangement = Arrangement.spacedBy(AccessibilityDefaults.ItemSpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            modifier = Modifier.size(AccessibilityDefaults.IconSize),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (trailingAction != null) {
                        trailingAction()
                    }
                }
            }
            content()
        }
    }
}
