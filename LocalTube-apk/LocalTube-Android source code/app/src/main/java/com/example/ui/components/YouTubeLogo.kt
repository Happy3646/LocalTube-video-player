package com.example.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Top App Bar Brand Logo: Custom Folder + YouTube Play Icon with LocalTube text
 */
@Composable
fun YouTubeLogo(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom Folder-YouTube Play Logo
        Icon(
            painter = painterResource(id = R.drawable.ic_localtube_logo),
            contentDescription = "LocalTube Logo",
            tint = Color.Unspecified,
            modifier = Modifier.size(30.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // "LocalTube" Brand Title
        Text(
            text = "LocalTube",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
