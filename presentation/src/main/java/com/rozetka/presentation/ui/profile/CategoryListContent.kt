package com.rozetka.presentation.ui.profile

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rozetka.presentation.R

@Composable
fun CategoryListContent(
    toPay: () -> Unit,
    toSessionResult: () -> Unit,
    toMaps: () -> Unit,
    toDigitalService: () -> Unit,
    toPhysEdJournal: () -> Unit,
    toProjectActivity: () -> Unit

) {
    val context = LocalContext.current

    CategoryItem(
        iconResId = R.drawable.education_outline_28,
        title = stringResource(R.string.session_results),
        subtitle = stringResource(R.string.check_progress),
        shape = RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomEnd = 8.dp,
            bottomStart = 8.dp
        ),
        onClick = toSessionResult,
        Color(0xFF89D1F3)
    )
    Spacer(Modifier.size(2.dp))

    CategoryItem(
        iconResId = R.drawable.lightbulb_star_outline,
        title = stringResource(R.string.project_activity),
        subtitle = stringResource(R.string.project_activity_subtitel),
        shape = RoundedCornerShape(8.dp),
        onClick = toProjectActivity,
        Color(0xFFD5E3FF)
    )
    Spacer(Modifier.size(2.dp))
    CategoryItem(
        iconResId = R.drawable.document_outline_28,
        title = stringResource(R.string.digital_services),
        subtitle = stringResource(R.string.application_submission_service),
        shape = RoundedCornerShape(
            topStart = 8.dp,
            topEnd = 8.dp,
            bottomEnd = 20.dp,
            bottomStart = 20.dp
        ),
        onClick = toDigitalService,
        Color(0xFFD5E3FF)
    )
    Spacer(Modifier.size(28.dp))

    CategoryItem(
        iconResId = R.drawable.school_outline_28,
        title = stringResource(R.string.physical_education),
        subtitle = stringResource(R.string.attendance) + " " + stringResource(R.string.and) + " " + stringResource(
            R.string.score
        ),
        shape = RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomEnd = 8.dp,
            bottomStart = 8.dp
        ),
        onClick = toPhysEdJournal,
        Color(0xFFFAD1E2)
    )
    Spacer(Modifier.size(2.dp))
    CategoryItem(
        iconResId = R.drawable.payment_card_outline_28,
        title = stringResource(R.string.services),
        subtitle = stringResource(R.string.services_action_pay),
        shape = RoundedCornerShape(8.dp),
        onClick = toPay,
        Color(0xFFFAD1E2)
    )
    Spacer(Modifier.size(2.dp))

    CategoryItem(
        iconResId = R.drawable.location_map_outline,
        title = stringResource(R.string.campuse_map),
        subtitle = stringResource(R.string.campuse_map_subtitel),
        shape =
            RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomEnd = 20.dp,
                bottomStart = 20.dp
            ),
        onClick = toMaps,
        Color(0xFFEADDFF)
    )
    Spacer(Modifier.size(28.dp))
}