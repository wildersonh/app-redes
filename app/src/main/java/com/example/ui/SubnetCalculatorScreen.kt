package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CalculationMode
import com.example.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubnetCalculatorScreen(
  viewModel: SubnetViewModel,
  modifier: Modifier = Modifier
) {
  val state by viewModel.uiState.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }
  var showHistorySheet by remember { mutableStateOf(false) }

  // Observe feedback message to show in snackbar
  LaunchedEffect(state.copyFeedbackMessage) {
    state.copyFeedbackMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.setCopyFeedback(null)
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Hub,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
            Column {
              Text(
                text = "SubnetCalc",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
              )
              Text(
                text = "IPv4 Clases A, B y C • CIDR & RFC 1812",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        actions = {
          if (state.history.isNotEmpty()) {
            IconButton(
              onClick = { showHistorySheet = true },
              modifier = Modifier.testTag("history_button")
            ) {
              Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = "Historial de cálculos"
              )
            }
          }

          IconButton(
            onClick = { viewModel.toggleInfoDialog(true) },
            modifier = Modifier.testTag("top_info_button")
          ) {
            Icon(
              imageVector = Icons.Outlined.Info,
              contentDescription = "Información RFC"
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Input Section
      item {
        InputSection(
          state = state,
          onIpChanged = viewModel::onIpChanged,
          onModeChanged = viewModel::onCalculationModeChanged,
          onHostsChanged = viewModel::onHostsInputChanged,
          onMaskCidrChanged = viewModel::onMaskCidrChanged,
          onAcademicModeToggled = viewModel::onAcademicModeToggled,
          onCalculateClicked = viewModel::calculate,
          onPresetSelected = viewModel::applyPreset,
          onInfoClicked = { viewModel.toggleInfoDialog(true) },
          onClearClicked = viewModel::clearInputs
        )
      }

      // 2. Results Dashboard (Summary Cards)
      state.calculationResult?.let { result ->
        item {
          DashboardSummaryCards(result = result)
        }

        // 3. Subnets Table
        item {
          SubnetTableSection(
            state = state,
            onPrevPage = viewModel::prevPage,
            onNextPage = viewModel::nextPage,
            onSelectSubnet = viewModel::selectSubnetForDetails,
            onSearchQueryChanged = viewModel::onIpSearchQueryChanged,
            onCopyNotice = viewModel::setCopyFeedback
          )
        }
      }
    }
  }

  // Subnet Details Dialog
  state.selectedSubnetDetails?.let { subnet ->
    state.calculationResult?.let { result ->
      SubnetDetailsDialog(
        subnet = subnet,
        result = result,
        onDismiss = { viewModel.selectSubnetForDetails(null) },
        onCopied = viewModel::setCopyFeedback
      )
    }
  }

  // RFC Academic Info Dialog
  if (state.showInfoDialog) {
    AcademicInfoDialog(onDismiss = { viewModel.toggleInfoDialog(false) })
  }

  // History Bottom Sheet
  if (showHistorySheet) {
    ModalBottomSheet(
      onDismissRequest = { showHistorySheet = false },
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Historial Reciente",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          TextButton(onClick = { showHistorySheet = false }) {
            Text("Cerrar")
          }
        }

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 350.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(state.history) { item ->
            ElevatedCard(
              onClick = {
                viewModel.onIpChanged(item.ip)
                if (item.mode == CalculationMode.BY_HOSTS) {
                  val hostsCount = item.hostOrMaskDisplay.filter { it.isDigit() }
                  viewModel.onHostsInputChanged(hostsCount)
                  viewModel.onCalculationModeChanged(CalculationMode.BY_HOSTS)
                } else {
                  val cidr = item.hostOrMaskDisplay.filter { it.isDigit() }.toIntOrNull() ?: 24
                  viewModel.onMaskCidrChanged(cidr)
                  viewModel.onCalculationModeChanged(CalculationMode.BY_MASK)
                }
                viewModel.calculate()
                showHistorySheet = false
              },
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = item.ip,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Modo: ${item.hostOrMaskDisplay} • ${item.resultMask}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = MaterialTheme.colorScheme.primaryContainer
                ) {
                  Text(
                    text = "${item.totalSubnets} subredes",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
