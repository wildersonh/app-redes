package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SubnetCalculationResult
import com.example.domain.model.SubnetItem
import com.example.ui.SubnetUiState
import com.example.ui.theme.*

@Composable
fun SubnetTableSection(
  state: SubnetUiState,
  onPrevPage: () -> Unit,
  onNextPage: () -> Unit,
  onSelectSubnet: (SubnetItem) -> Unit,
  onSearchQueryChanged: (String) -> Unit,
  onCopyNotice: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val clipboardManager = LocalClipboardManager.current
  val result = state.calculationResult ?: return
  val currentSubnets = state.getCurrentPageSubnets()

  ElevatedCard(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Table Title & Count
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Tabla de Subredes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = if (state.isAcademicMode) {
              "${result.academicUsableSubnets} subredes útiles (${result.totalSubnets} totales generadas)"
            } else {
              "${result.totalSubnets} subredes generadas"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Quick Export All to Clipboard
        OutlinedButton(
          onClick = {
            val sb = StringBuilder()
            sb.appendLine("Cálculo de Subredes IPv4 para ${result.actualNetworkBaseIp}/${result.newPrefix}")
            sb.appendLine("Máscara: ${result.newMask} | Hosts por subred: ${result.usableHostsPerSubnet} | Salto: ${result.blockSize}")
            sb.appendLine("--------------------------------------------------------------------------------")
            val limit = minOf(result.totalSubnets, 100L)
            for (i in 0 until limit) {
              val item = result.getSubnetAtIndex(i)
              sb.appendLine("#${item.displayNumber} | Red: ${item.networkIp} | Primer Host: ${item.firstHostIp ?: "N/A"} | Último Host: ${item.lastHostIp ?: "N/A"} | Broadcast: ${item.broadcastIp}")
            }
            if (result.totalSubnets > 100) {
              sb.appendLine("... y ${result.totalSubnets - 100} subredes más")
            }
            clipboardManager.setText(AnnotatedString(sb.toString()))
            onCopyNotice("Tabla copiada al portapapeles")
          },
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier.testTag("export_button")
        ) {
          Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Copiar Todo", style = MaterialTheme.typography.labelSmall)
        }
      }

      // Fast Subnet Finder / IP Lookup
      OutlinedTextField(
        value = state.ipSearchQuery,
        onValueChange = onSearchQueryChanged,
        label = { Text("Buscar en qué subred cae una IP") },
        placeholder = { Text("Ej. 192.168.1.75") },
        leadingIcon = {
          Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingIcon = {
          if (state.ipSearchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchQueryChanged("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
            }
          }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("subnet_search_field")
      )

      // Search feedback banner
      state.searchResultFeedback?.let { feedback ->
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = if (state.searchResultSubnet != null) {
            MaterialTheme.colorScheme.primaryContainer
          } else {
            MaterialTheme.colorScheme.errorContainer
          },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = feedback,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
          )
        }
      }

      // Pagination Controls (Top)
      if (state.totalPages > 1) {
        PaginationBar(
          currentPage = state.currentPage,
          totalPages = state.totalPages,
          onPrev = onPrevPage,
          onNext = onNextPage
        )
      }

      // Subnet Rows (Lazy Column)
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        currentSubnets.forEach { subnet ->
          val isHighlighted = state.searchResultSubnet?.index == subnet.index
          SubnetRowCard(
            subnet = subnet,
            isAcademicMode = state.isAcademicMode,
            isHighlighted = isHighlighted,
            onClick = { onSelectSubnet(subnet) },
            onCopy = {
              val text = "Subred #${subnet.displayNumber}: Red ${subnet.networkIp}/${result.newPrefix}, Rango: ${subnet.formattedRange}, Broadcast: ${subnet.broadcastIp}"
              clipboardManager.setText(AnnotatedString(text))
              onCopyNotice("Copiada Subred #${subnet.displayNumber}")
            }
          )
        }
      }

      // Pagination Controls (Bottom)
      if (state.totalPages > 1) {
        PaginationBar(
          currentPage = state.currentPage,
          totalPages = state.totalPages,
          onPrev = onPrevPage,
          onNext = onNextPage
        )
      }
    }
  }
}

@Composable
private fun PaginationBar(
  currentPage: Int,
  totalPages: Int,
  onPrev: () -> Unit,
  onNext: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      .padding(horizontal = 12.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    FilledTonalIconButton(
      onClick = onPrev,
      enabled = currentPage > 1,
      modifier = Modifier
        .size(36.dp)
        .testTag("prev_page_button")
    ) {
      Icon(Icons.Default.ChevronLeft, contentDescription = "Página anterior")
    }

    Text(
      text = "Página $currentPage de $totalPages",
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )

    FilledTonalIconButton(
      onClick = onNext,
      enabled = currentPage < totalPages,
      modifier = Modifier
        .size(36.dp)
        .testTag("next_page_button")
    ) {
      Icon(Icons.Default.ChevronRight, contentDescription = "Página siguiente")
    }
  }
}

@Composable
private fun SubnetRowCard(
  subnet: SubnetItem,
  isAcademicMode: Boolean,
  isHighlighted: Boolean,
  onClick: () -> Unit,
  onCopy: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isDiscarded = isAcademicMode && !subnet.isUsableInAcademicMode

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("subnet_card_${subnet.index}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = when {
        isHighlighted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        isDiscarded -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
      }
    ),
    border = when {
      isHighlighted -> androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
      else -> null
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Top row: Badge, Status, and Copy
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.primary
          ) {
            Text(
              text = "Subred #${subnet.displayNumber}",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }

          if (isAcademicMode) {
            when {
              subnet.isSubnetZero -> {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                ) {
                  Text(
                    text = "Subnet Zero (RFC 950 descarta)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
              subnet.isBroadcastSubnet -> {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                ) {
                  Text(
                    text = "Subnet Broadcast (RFC 950 descarta)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
              else -> {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = Color(0xFF10B981).copy(alpha = 0.15f)
                ) {
                  Text(
                    text = "Válida",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF047857),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }
        }

        IconButton(
          onClick = onCopy,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Outlined.ContentCopy,
            contentDescription = "Copiar subred",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      // The 4 core required values: ID Red, Primer Host, Último Host, Broadcast
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        SubnetField(
          label = "ID de Red",
          value = subnet.networkIp.toDottedDecimal(),
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.weight(1f)
        )

        SubnetField(
          label = "Broadcast",
          value = subnet.broadcastIp.toDottedDecimal(),
          color = Color(0xFFE11D48),
          modifier = Modifier.weight(1f)
        )
      }

      Divider(
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        thickness = 1.dp
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        SubnetField(
          label = "Primera IP Válida",
          value = subnet.firstHostIp?.toDottedDecimal() ?: "N/A",
          color = Color(0xFF059669),
          modifier = Modifier.weight(1f)
        )

        SubnetField(
          label = "Última IP Válida",
          value = subnet.lastHostIp?.toDottedDecimal() ?: "N/A",
          color = Color(0xFF059669),
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
private fun SubnetField(
  label: String,
  value: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
      ),
      color = color
    )
  }
}
