package com.morgans.dashboard.ui.screens.expenses

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.morgans.dashboard.ui.theme.StatusError
import com.morgans.dashboard.ui.theme.StatusOnline
import com.morgans.dashboard.util.formatCurrency
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun ExpensesScreen(viewModel: ExpensesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val monthName = Month.of(state.month).getDisplayName(TextStyle.FULL, Locale.getDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header with month navigation
        item {
            Text("Expenses", style = MaterialTheme.typography.headlineLarge)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { viewModel.prevMonth() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous month")
                }
                Text(
                    "$monthName ${state.year}",
                    style = MaterialTheme.typography.titleMedium,
                )
                IconButton(onClick = { viewModel.nextMonth() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next month")
                }
            }
        }

        // Summary cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    label = "Expenses",
                    value = formatCurrency(state.totals?.total ?: 0.0),
                    valueColor = StatusError,
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    label = "Income",
                    value = formatCurrency(state.totalIncome),
                    valueColor = StatusOnline,
                )
                val net = state.totalIncome - (state.totals?.total ?: 0.0)
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    label = "Net",
                    value = formatCurrency(net),
                    valueColor = if (net >= 0) StatusOnline else StatusError,
                )
            }
        }

        // Category breakdown
        val byCategory = state.totals?.byCategory ?: emptyMap()
        if (byCategory.isNotEmpty()) {
            item {
                Text("By Category", style = MaterialTheme.typography.titleMedium)
            }
            val total = state.totals?.total ?: 1.0
            items(byCategory.entries.sortedByDescending { it.value }.toList()) { (cat, amount) ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(cat, style = MaterialTheme.typography.bodySmall)
                        Text(formatCurrency(amount), style = MaterialTheme.typography.bodySmall)
                    }
                    LinearProgressIndicator(
                        progress = { (amount / total).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(4.dp),
                    )
                }
            }
        }

        // Expense list
        if (state.expenses.isNotEmpty()) {
            item {
                Text("Transactions", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            }
            items(state.expenses, key = { it.id }) { expense ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                expense.description,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                buildString {
                                    expense.category_name?.let { append(it) }
                                    expense.credit_card_name?.let {
                                        if (isNotEmpty()) append(" | ")
                                        append(it)
                                    }
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            formatCurrency(expense.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall, color = valueColor)
        }
    }
}
