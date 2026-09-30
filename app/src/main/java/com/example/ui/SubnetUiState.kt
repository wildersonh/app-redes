package com.example.ui

import com.example.domain.model.CalculationMode
import com.example.domain.model.IpClass
import com.example.domain.model.SubnetCalculationResult
import com.example.domain.model.SubnetItem

data class HistoryItem(
  val id: String,
  val ip: String,
  val mode: CalculationMode,
  val hostOrMaskDisplay: String,
  val resultMask: String,
  val totalSubnets: Long,
  val hostsPerSubnet: Long,
  val timestamp: Long = System.currentTimeMillis()
)

data class SubnetUiState(
  val ipInput: String = "192.168.1.0",
  val calculationMode: CalculationMode = CalculationMode.BY_HOSTS,
  val hostsInput: String = "30",
  val maskCidrInput: Int = 26,
  val isAcademicMode: Boolean = false,
  val customBasePrefix: Int? = null,
  val detectedClass: IpClass? = IpClass.CLASS_C,
  val inputError: String? = null,
  val calculationResult: SubnetCalculationResult? = null,
  val currentPage: Int = 1,
  val pageSize: Int = 50,
  val ipSearchQuery: String = "",
  val searchResultSubnet: SubnetItem? = null,
  val searchResultFeedback: String? = null,
  val selectedSubnetDetails: SubnetItem? = null,
  val history: List<HistoryItem> = emptyList(),
  val showInfoDialog: Boolean = false,
  val copyFeedbackMessage: String? = null
) {
  val totalPages: Int
    get() {
      val res = calculationResult ?: return 1
      val total = res.activeSubnetCount
      if (total <= 0) return 1
      return (((total - 1) / pageSize) + 1).toInt().coerceAtLeast(1)
    }

  /**
   * Returns items for current page safely without allocating memory for the whole range.
   */
  fun getCurrentPageSubnets(): List<SubnetItem> {
    val res = calculationResult ?: return emptyList()
    val total = res.totalSubnets
    if (total <= 0) return emptyList()

    val startIndex = ((currentPage - 1L) * pageSize).coerceIn(0L, total)
    val endIndex = (startIndex + pageSize).coerceAtMost(total)

    val list = ArrayList<SubnetItem>((endIndex - startIndex).toInt())
    for (i in startIndex until endIndex) {
      val item = res.getSubnetAtIndex(i)
      // In academic mode, user might want to see non-usable marked or filtered
      list.add(item)
    }
    return list
  }
}
