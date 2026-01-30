package com.cookstemma.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StarRating(
    rating: Int,
    maxRating: Int = 5,
    modifier: Modifier = Modifier,
    onRatingChange: ((Int) -> Unit)? = null,
    size: Dp = 24.dp,
    interactive: Boolean = false,
    activeColor: Color = Color(0xFFFFC107),
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
) {
    Row(modifier = modifier) {
        repeat(maxRating) { index ->
            val starRating = index + 1
            Icon(
                imageVector = if (index < rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = if (interactive) "Rate $starRating stars" else null,
                tint = if (index < rating) activeColor else inactiveColor,
                modifier = Modifier
                    .size(size)
                    .then(
                        if (interactive && onRatingChange != null) {
                            Modifier.clickable { onRatingChange(starRating) }
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

@Composable
fun StarRatingDecimal(
    rating: Double,
    maxRating: Int = 5,
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFFFFC107),
    inactiveColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
) {
    Row(modifier = modifier) {
        repeat(maxRating) { index ->
            val icon = when {
                index < rating.toInt() -> Icons.Filled.Star
                index < rating -> Icons.Filled.StarHalf
                else -> Icons.Filled.StarBorder
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (index < rating) activeColor else inactiveColor
            )
        }
    }
}
