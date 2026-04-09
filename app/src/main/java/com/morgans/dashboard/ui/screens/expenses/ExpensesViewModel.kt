package com.morgans.dashboard.ui.screens.expenses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.morgans.dashboard.data.model.*
import com.morgans.dashboard.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ExpensesState(
    val expenses: List<Expense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val users: List<ExpenseUser> = emptyList(),
    val creditCards: List<CreditCard> = emptyList(),
    val income: List<IncomeItem> = emptyList(),
    val totals: ExpenseTotals? = null,
    val totalIncome: Double = 0.0,
    val month: Int = LocalDate.now().monthValue,
    val year: Int = LocalDate.now().year,
    val selectedUserId: Int? = null,
    val loading: Boolean = true,
)

@HiltViewModel
class ExpensesViewModel @Inject constructor(
    private val repository: DashboardRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ExpensesState())
    val state = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            try {
                val s = _state.value
                val expenses = repository.getExpenses(s.month, s.year, s.selectedUserId)
                val income = repository.getIncome(s.month, s.year)
                _state.update {
                    it.copy(
                        expenses = expenses.expenses,
                        categories = expenses.categories,
                        users = expenses.users,
                        creditCards = expenses.creditCards,
                        totals = expenses.totals,
                        income = income.income,
                        totalIncome = income.total,
                        loading = false,
                    )
                }
            } catch (_: Exception) {
                _state.update { it.copy(loading = false) }
            }
        }
    }

    fun setMonth(month: Int, year: Int) {
        _state.update { it.copy(month = month, year = year) }
        load()
    }

    fun prevMonth() {
        val s = _state.value
        val newMonth = if (s.month == 1) 12 else s.month - 1
        val newYear = if (s.month == 1) s.year - 1 else s.year
        setMonth(newMonth, newYear)
    }

    fun nextMonth() {
        val s = _state.value
        val newMonth = if (s.month == 12) 1 else s.month + 1
        val newYear = if (s.month == 12) s.year + 1 else s.year
        setMonth(newMonth, newYear)
    }

    fun filterByUser(userId: Int?) {
        _state.update { it.copy(selectedUserId = userId) }
        load()
    }
}
