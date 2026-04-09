package com.morgans.dashboard.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ExpensesResponse(
    val expenses: List<Expense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val users: List<ExpenseUser> = emptyList(),
    val creditCards: List<CreditCard> = emptyList(),
    val totals: ExpenseTotals? = null,
)

@Serializable
data class Expense(
    val id: Int,
    val description: String,
    val amount: Double,
    val category_id: Int? = null,
    val category_name: String? = null,
    val parent_category_name: String? = null,
    val credit_card_id: Int? = null,
    val credit_card_name: String? = null,
    val user_id: Int? = null,
    val username: String? = null,
    val date: String = "",
    val notes: String? = null,
)

@Serializable
data class Category(
    val id: Int,
    val name: String,
    val parent_id: Int? = null,
    val color: String? = null,
    val icon: String? = null,
    val children: List<Category> = emptyList(),
)

@Serializable
data class ExpenseUser(
    val id: Int,
    val username: String,
)

@Serializable
data class CreditCard(
    val id: Int,
    val name: String,
    val last_four: String? = null,
    val color: String? = null,
)

@Serializable
data class ExpenseTotals(
    val total: Double = 0.0,
    val byCategory: Map<String, Double> = emptyMap(),
    val byUser: Map<String, Double> = emptyMap(),
)

@Serializable
data class IncomeResponse(
    val income: List<IncomeItem> = emptyList(),
    val total: Double = 0.0,
)

@Serializable
data class IncomeItem(
    val id: Int,
    val description: String,
    val amount: Double,
    val date: String = "",
    val source: String? = null,
)
