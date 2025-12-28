package com.rozetka.presentation.ui.pay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rozetka.model.Dormitory
import com.rozetka.presentation.R
import com.rozetka.presentation.util.shareImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DormitoryCard(dormitory: Dormitory) {
    var showBottomSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(
                topStart = 28.dp,
                topEnd = 28.dp,
                bottomStart = 8.dp,
                bottomEnd = 8.dp
            ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apartment,
                            contentDescription = stringResource(R.string.dormitory_card_content_description),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(40.dp)
                                .align(Alignment.Center)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = dormitory.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp), shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.dormitory_contract_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                InfoRow(
                    icon = painterResource(R.drawable.user_outline),
                    label = stringResource(R.string.student_label),
                    value = dormitory.student
                )
                InfoRow(
                    icon = painterResource(R.drawable.mail_outline),
                    label = stringResource(R.string.email_label),
                    value = dormitory.userEmail
                )
                InfoRow(
                    icon = painterResource(R.drawable.home_outline),
                    label = stringResource(R.string.dormitory_label),
                    value = stringResource(R.string.dormitory_room_info, dormitory.dormNum, dormitory.dormRoom)
                )
                InfoRow(
                    icon = painterResource(R.drawable.calendar_outline_24),
                    label = stringResource(R.string.residence_period_label),
                    value = stringResource(R.string.residence_period_value, dormitory.startDate, dormitory.endDatePlan)
                )
                InfoRow(
                    icon = painterResource(R.drawable.money_circle_outline),
                    label = stringResource(R.string.balance_label),
                    value = stringResource(R.string.balance_value, dormitory.balance)
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(
                bottomEnd = 28.dp,
                bottomStart = 28.dp,
                topStart = 8.dp,
                topEnd = 8.dp
            ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                Button(onClick = {



                }) {
                    Text(stringResource(R.string.download_contract_button))
                }
                Button(onClick = { showBottomSheet = true }) {
                    Text(stringResource(R.string.pay_button))
                }
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.qr_payment_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                AsyncImage(
                    model = dormitory.qrCurrent,
                    contentDescription = stringResource(R.string.qr_code_content_description),
                    modifier = Modifier
                        .size(250.dp)
                        .padding(16.dp)
                )
                Text(
                    text = stringResource(R.string.qr_code_instruction),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                val context = LocalContext.current
                OutlinedButton(
                    onClick = { shareImage(context, dormitory.qrCurrent) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = stringResource(R.string.share_button))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.share_button))
                }
            }
        }
    }
}