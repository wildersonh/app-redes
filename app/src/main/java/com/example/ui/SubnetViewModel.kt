package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CalculationMode
import com.example.domain.model.IpAddress
import com.example.domain.model.IpClass
import com.example.domain.model.SubnetItem
import com.example.domain.usecase.SubnetCalculatorUseCases
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class SubnetViewModel(
  private val useCases: SubnetCalculatorUseCases = SubnetCalculatorUseCases()
) : ViewModel() {

  private val _uiState = MutableStateFlow(SubnetUiState())
  val uiState: StateFlow<SubnetUiState> = _uiState.asStateFlow()

  init {
    // Perform initial calculation so the app opens with rich data
    calculate()
  }

  fun onIpChanged(newIp: String) {
    // Filter to allow numbers and dots only
    val filtered = newIp.filter { it.isDigit() || it == '.' }
    val detected = IpAddress.fromString(filtered)?.let { IpClass.fromIp(it) }

    _uiState.update { state ->
      state.copy(
        ipInput = filtered,
        detectedClass = detected,
        inputError = null
      )
    }
  }

  fun onCalculationModeChanged(mode: CalculationMode) {
    _uiState.update { state ->
      state.copy(
        calculationMode = mode,
        inputError = null
      )
    }
  }

  fun onHostsInputChanged(newHosts: String) {
    val filtered = newHosts.filter { it.isDigit() }
    _uiState.update { state ->
      state.copy(
        hostsInput = filtered,
        inputError = null
      )
    }
  }

  fun onMaskCidrChanged(prefix: Int) {
    _uiState.update { state ->
      state.copy(
        maskCidrInput = prefix.coerceIn(1, 32),
        inputError = null
      )
    }
  }

  fun onAcademicModeToggled(enabled: Boolean) {
    _uiState.update { state ->
      state.copy(isAcademicMode = enabled)
    }
    // Re-calculate with updated academic flag if there's already an active result
    calculate()
  }

  fun onBasePrefixOverrideChanged(prefix: Int?) {
    _uiState.update { state ->
      state.copy(customBasePrefix = prefix)
    }
    calculate()
  }

  fun calculate() {
    val state = _uiState.value
    val ipStr = state.ipInput.trim()

    val parsedIp = IpAddress.fromString(ipStr)
    if (parsedIp == null) {
      _uiState.update {
        it.copy(
          inputError = "Por favor ingresa una dirección IPv4 válida (ej. 192.168.1.0, 172.16.0.0, 10.0.0.0)",
          calculationResult = null
        )
      }
      return
    }

    val ipClass = IpClass.fromIp(parsedIp)
    if (ipClass != IpClass.CLASS_A && ipClass != IpClass.CLASS_B && ipClass != IpClass.CLASS_C) {
      _uiState.update {
        it.copy(
          inputError = "La IP ingresada pertenece a ${ipClass.displayName}. Solo las Clases A, B y C son admitidas para subredes.",
          calculationResult = null
        )
      }
      return
    }

    try {
      val hostsInt = if (state.calculationMode == CalculationMode.BY_HOSTS) {
        val h = state.hostsInput.toIntOrNull()
        if (h == null || h <= 0) {
          _uiState.update { it.copy(inputError = "Ingresa un número válido de hosts mayor a 0.") }
          return
        }
        h
      } else null

      val result = useCases.calculateSubnet(
        rawIp = ipStr,
        mode = state.calculationMode,
        hostsNeeded = hostsInt,
        targetCidrPrefix = if (state.calculationMode == CalculationMode.BY_MASK) state.maskCidrInput else null,
        isAcademicMode = state.isAcademicMode,
        overrideBasePrefix = state.customBasePrefix
      )

      val historyEntry = HistoryItem(
        id = UUID.randomUUID().toString(),
        ip = ipStr,
        mode = state.calculationMode,
        hostOrMaskDisplay = if (state.calculationMode == CalculationMode.BY_HOSTS) "${state.hostsInput} hosts" else "/${state.maskCidrInput}",
        resultMask = "${result.newMask} (/${result.newPrefix})",
        totalSubnets = result.activeSubnetCount,
        hostsPerSubnet = result.usableHostsPerSubnet
      )

      val updatedHistory = listOf(historyEntry) + state.history.take(9)

      _uiState.update {
        it.copy(
          calculationResult = result,
          currentPage = 1,
          inputError = null,
          history = updatedHistory,
          ipSearchQuery = "",
          searchResultSubnet = null,
          searchResultFeedback = null
        )
      }
    } catch (e: Exception) {
      _uiState.update {
        it.copy(
          inputError = e.message ?: "Ocurrió un error al calcular la subred.",
          calculationResult = null
        )
      }
    }
  }

  fun setPage(page: Int) {
    val total = _uiState.value.totalPages
    val validPage = page.coerceIn(1, total)
    _uiState.update { it.copy(currentPage = validPage) }
  }

  fun nextPage() {
    setPage(_uiState.value.currentPage + 1)
  }

  fun prevPage() {
    setPage(_uiState.value.currentPage - 1)
  }

  fun onIpSearchQueryChanged(query: String) {
    val filtered = query.filter { it.isDigit() || it == '.' }
    _uiState.update { it.copy(ipSearchQuery = filtered) }

    val result = _uiState.value.calculationResult ?: return
    if (filtered.isEmpty()) {
      _uiState.update {
        it.copy(searchResultSubnet = null, searchResultFeedback = null)
      }
      return
    }

    val targetIp = IpAddress.fromString(filtered)
    if (targetIp != null) {
      val foundIndex = result.findSubnetIndexForIp(targetIp)
      if (foundIndex != null) {
        val subnet = result.getSubnetAtIndex(foundIndex)
        val targetPage = ((foundIndex / _uiState.value.pageSize) + 1).toInt()
        _uiState.update {
          it.copy(
            searchResultSubnet = subnet,
            searchResultFeedback = "IP encontrada en la Subred #${subnet.displayNumber} (Página $targetPage)",
            currentPage = targetPage
          )
        }
      } else {
        _uiState.update {
          it.copy(
            searchResultSubnet = null,
            searchResultFeedback = "La IP $filtered no pertenece a esta red base (${result.actualNetworkBaseIp}/${result.basePrefix})"
          )
        }
      }
    }
  }

  fun selectSubnetForDetails(subnet: SubnetItem?) {
    _uiState.update { it.copy(selectedSubnetDetails = subnet) }
  }

  fun toggleInfoDialog(show: Boolean) {
    _uiState.update { it.copy(showInfoDialog = show) }
  }

  fun setCopyFeedback(message: String?) {
    _uiState.update { it.copy(copyFeedbackMessage = message) }
  }

  fun applyPreset(ip: String, hosts: Int?, cidr: Int?, basePrefix: Int?) {
    _uiState.update { state ->
      val detected = IpAddress.fromString(ip)?.let { IpClass.fromIp(it) }
      state.copy(
        ipInput = ip,
        detectedClass = detected,
        calculationMode = if (hosts != null) CalculationMode.BY_HOSTS else CalculationMode.BY_MASK,
        hostsInput = hosts?.toString() ?: state.hostsInput,
        maskCidrInput = cidr ?: state.maskCidrInput,
        customBasePrefix = basePrefix,
        inputError = null
      )
    }
    calculate()
  }

  fun clearInputs() {
    _uiState.update {
      it.copy(
        ipInput = "",
        detectedClass = null,
        hostsInput = "",
        inputError = null,
        calculationResult = null,
        ipSearchQuery = "",
        searchResultSubnet = null,
        searchResultFeedback = null
      )
    }
  }
}
