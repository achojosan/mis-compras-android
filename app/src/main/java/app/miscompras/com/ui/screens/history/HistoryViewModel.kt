package app.miscompras.com.ui.screens.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.miscompras.com.data.repository.ProductRepository
import app.miscompras.com.data.repository.PurchaseHistoryRepository
import app.miscompras.com.data.repository.SupermarketRepository
import app.miscompras.com.domain.model.Product
import app.miscompras.com.domain.model.PurchaseHistory
import app.miscompras.com.domain.model.Supermarket
import app.miscompras.com.ui.screens.shopping.PurchasedProductData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/** Mes calendario al que pertenece un gasto. */
fun PurchaseHistory.purchaseMonth(): YearMonth =
    YearMonth.from(Instant.ofEpochMilli(purchasedAt).atZone(ZoneId.systemDefault()))

private val monthLabelFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))
private val monthNameFormatter = DateTimeFormatter.ofPattern("MMMM", Locale("es", "ES"))

/** Etiqueta legible del mes, p. ej. "Septiembre 2026". */
fun formatMonthLabel(yearMonth: YearMonth): String {
    val formatted = yearMonth.format(monthLabelFormatter)
    return formatted.replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale("es", "ES")) else char.toString()
    }
}

/** Nombre del mes sin año, p. ej. "Septiembre". */
fun formatMonthName(yearMonth: YearMonth): String {
    val formatted = yearMonth.format(monthNameFormatter)
    return formatted.replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale("es", "ES")) else char.toString()
    }
}

/** Resumen de gasto de un mes concreto. */
data class MonthExpense(
    val yearMonth: YearMonth,
    val total: Double,
    val purchaseCount: Int
)

@HiltViewModel
class HistoryListViewModel @Inject constructor(
    private val purchaseHistoryRepository: PurchaseHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryListUiState>(HistoryListUiState.Loading)
    val uiState: StateFlow<HistoryListUiState> = _uiState.asStateFlow()

    // Año seleccionado por el usuario; null = el más reciente con datos.
    private val selectedYear = MutableStateFlow<Int?>(null)

    init {
        loadHistory()
    }

    fun selectYear(year: Int) {
        selectedYear.value = year
    }

    private fun loadHistory() {
        viewModelScope.launch {
            combine(
                purchaseHistoryRepository.getAllHistory(),
                selectedYear
            ) { historyList, chosenYear ->
                if (historyList.isEmpty()) {
                    HistoryListUiState.Empty
                } else {
                    val allMonths = historyList
                        .groupBy { it.purchaseMonth() }
                        .map { (yearMonth, purchases) ->
                            MonthExpense(
                                yearMonth = yearMonth,
                                total = purchases.sumOf { it.totalAmount },
                                purchaseCount = purchases.size
                            )
                        }
                        .sortedByDescending { it.yearMonth }

                    val years = allMonths
                        .map { it.yearMonth.year }
                        .distinct()
                        .sortedDescending()

                    val activeYear = chosenYear?.takeIf { it in years } ?: years.first()
                    val monthsForYear = allMonths.filter { it.yearMonth.year == activeYear }

                    HistoryListUiState.Success(
                        availableYears = years,
                        selectedYear = activeYear,
                        months = monthsForYear,
                        yearTotal = monthsForYear.sumOf { it.total },
                        yearPurchaseCount = monthsForYear.sumOf { it.purchaseCount },
                        currentMonthTotal = allMonths.find { it.yearMonth == YearMonth.now() }?.total ?: 0.0,
                        totalSpent = historyList.sumOf { it.totalAmount },
                        totalPurchases = historyList.size
                    )
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

sealed class HistoryListUiState {
    data object Loading : HistoryListUiState()
    data object Empty : HistoryListUiState()
    data class Success(
        val availableYears: List<Int>,
        val selectedYear: Int,
        val months: List<MonthExpense>,
        val yearTotal: Double,
        val yearPurchaseCount: Int,
        val currentMonthTotal: Double,
        val totalSpent: Double,
        val totalPurchases: Int
    ) : HistoryListUiState()
}

data class HistoryItem(
    val history: PurchaseHistory,
    val supermarket: Supermarket?
) {
    // Nombre a mostrar: el de la tienda actual o el guardado en el historial.
    val supermarketName: String
        get() = supermarket?.name ?: history.supermarketName ?: "Supermercado"
}

@HiltViewModel
class MonthHistoryViewModel @Inject constructor(
    private val purchaseHistoryRepository: PurchaseHistoryRepository,
    private val supermarketRepository: SupermarketRepository,
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val monthKey: String = savedStateHandle.get<String>("monthKey") ?: ""
    private val yearMonth: YearMonth? = runCatching { YearMonth.parse(monthKey) }.getOrNull()

    private val _uiState = MutableStateFlow<MonthHistoryUiState>(MonthHistoryUiState.Loading)
    val uiState: StateFlow<MonthHistoryUiState> = _uiState.asStateFlow()

    // Eventos de un solo uso para mostrar avisos (Toast) al usuario.
    private val _events = Channel<String>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadMonth()
    }

    private fun loadMonth() {
        val month = yearMonth
        if (month == null) {
            _uiState.value = MonthHistoryUiState.Empty
            return
        }

        viewModelScope.launch {
            combine(
                purchaseHistoryRepository.getAllHistory(),
                supermarketRepository.getAllSupermarkets()
            ) { historyList, supermarkets ->
                val items = historyList
                    .filter { it.purchaseMonth() == month }
                    .map { history ->
                        HistoryItem(
                            history = history,
                            supermarket = supermarkets.find { it.id == history.supermarketId }
                        )
                    }

                if (items.isEmpty()) {
                    MonthHistoryUiState.Empty
                } else {
                    val monthTotal = items.sumOf { it.history.totalAmount }
                    val previousMonthTotal = historyList
                        .filter { it.purchaseMonth() == month.minusMonths(1) }
                        .sumOf { it.totalAmount }
                    MonthHistoryUiState.Success(
                        yearMonth = month,
                        items = items,
                        total = monthTotal,
                        previousMonthTotal = previousMonthTotal,
                        averageTicket = monthTotal / items.size,
                        favouriteSupermarketName = items
                            .groupingBy { it.supermarketName }
                            .eachCount()
                            .maxByOrNull { it.value }
                            ?.key
                    )
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun repeatPurchase(historyItem: HistoryItem) {
        viewModelScope.launch {
            val supermarketId = historyItem.history.supermarketId
            if (supermarketId == null) {
                _events.send("No se puede repetir: la tienda ya no existe")
                return@launch
            }
            val products = try {
                Json.decodeFromString<List<PurchasedProductData>>(historyItem.history.productsJson)
            } catch (e: Exception) {
                emptyList()
            }
            if (products.isEmpty()) {
                _events.send("No hay productos para repetir")
                return@launch
            }

            // Productos que ya existen en el supermercado (por nombre), para no
            // duplicarlos y respetar los datos que el usuario haya actualizado.
            val existingByName = productRepository
                .getProductsBySupermarket(supermarketId)
                .first()
                .associateBy { it.name.trim().lowercase() }

            var added = 0
            var restored = 0

            products.forEach { data ->
                val current = existingByName[data.name.trim().lowercase()]
                when {
                    current == null -> {
                        productRepository.insertProduct(
                            Product(
                                name = data.name,
                                quantity = data.quantity,
                                unit = data.unit,
                                price = data.price,
                                supermarketId = supermarketId
                            )
                        )
                        added++
                    }
                    current.isPurchased -> {
                        // Vuelve a quedar pendiente para la nueva compra,
                        // conservando el nombre/cantidad/precio actuales.
                        productRepository.updatePurchaseStatus(current.id, false)
                        restored++
                    }
                    // Si ya existe y está pendiente se deja tal cual: así no se
                    // sobrescribe con los datos antiguos del histórico.
                }
            }

            val message = when {
                added == 0 && restored == 0 -> "Los productos ya están en tu lista"
                restored == 0 ->
                    "Compra repetida: $added producto${if (added != 1) "s" else ""}"
                added == 0 ->
                    "Compra repetida: $restored reactivado${if (restored != 1) "s" else ""}"
                else -> "Compra repetida: $added añadidos, $restored reactivados"
            }
            _events.send(message)
        }
    }
}

sealed class MonthHistoryUiState {
    data object Loading : MonthHistoryUiState()
    data object Empty : MonthHistoryUiState()
    data class Success(
        val yearMonth: YearMonth,
        val items: List<HistoryItem>,
        val total: Double,
        val previousMonthTotal: Double,
        val averageTicket: Double,
        val favouriteSupermarketName: String?
    ) : MonthHistoryUiState()
}

@HiltViewModel
class HistoryDetailViewModel @Inject constructor(
    private val purchaseHistoryRepository: PurchaseHistoryRepository,
    private val supermarketRepository: SupermarketRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val historyId: Long = savedStateHandle.get<Long>("historyId") ?: -1L

    private val _uiState = MutableStateFlow<HistoryDetailUiState>(HistoryDetailUiState.Loading)
    val uiState: StateFlow<HistoryDetailUiState> = _uiState.asStateFlow()

    init {
        loadHistoryDetail()
    }

    private fun loadHistoryDetail() {
        viewModelScope.launch {
            combine(
                purchaseHistoryRepository.getHistoryById(historyId),
                supermarketRepository.getAllSupermarkets()
            ) { history, supermarkets ->
                if (history != null) {
                    val supermarket = supermarkets.find { it.id == history.supermarketId }
                    val products = try {
                        Json.decodeFromString<List<PurchasedProductData>>(history.productsJson)
                    } catch (e: Exception) {
                        emptyList()
                    }
                    HistoryDetailUiState.Success(
                        history = history,
                        supermarket = supermarket,
                        products = products
                    )
                } else {
                    HistoryDetailUiState.Error("Compra no encontrada")
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

sealed class HistoryDetailUiState {
    data object Loading : HistoryDetailUiState()
    data class Error(val message: String) : HistoryDetailUiState()
    data class Success(
        val history: PurchaseHistory,
        val supermarket: Supermarket?,
        val products: List<PurchasedProductData>
    ) : HistoryDetailUiState()
}
