package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SubnetCalculationResult
import com.example.ui.theme.*

@Composable
fun DashboardSummaryCards(
  result: SubnetCalculationResult,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    Text(
      text = "Resultados del Cálculo",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface
    )

    // 2x2 Grid for the 4 core cards
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      MetricCard(
        title = "Máscara Resultante",
        value = result.newMask.toDottedDecimal(),
        secondary = "CIDR: /${result.newPrefix} • Wildcard: ${result.wildcardMask}",
        icon = Icons.Default.FilterAlt,
        iconTint = MaterialTheme.colorScheme.primary,
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        modifier = Modifier
          .weight(1f)
          .testTag("result_mask_card")
      )

      MetricCard(
        title = "Cantidad de Subredes",
        value = if (result.isAcademicMode) "${result.academicUsableSubnets} válidas" else "${result.totalSubnets}",
        secondary = if (result.isAcademicMode) {
          "2ˢ - 2 = 2^${result.borrowedBits} - 2 (RFC 950)"
        } else {
          "2ˢ = 2^${result.borrowedBits} (RFC 1812 Subnet Zero)"
        },
        icon = Icons.Default.Hub,
        iconTint = MaterialTheme.colorScheme.secondary,
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
        modifier = Modifier
          .weight(1f)
          .testTag("result_subnets_card")
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      MetricCard(
        title = "Hosts por Subred",
        value = "${result.usableHostsPerSubnet} útiles",
        secondary = "2ʰ - 2 = 2^${result.hostBits} - 2 (${result.totalAddressesPerSubnet} totales)",
        icon = Icons.Default.Devices,
        iconTint = MaterialTheme.colorScheme.tertiary,
        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
        modifier = Modifier
          .weight(1f)
          .testTag("result_hosts_card")
      )

      MetricCard(
        title = "Tamaño del Salto",
        value = "Salto de ${result.blockSize}",
        secondary = "En el ${result.modifiedOctetName} (256 - ${result.modifiedOctetMaskValue})",
        icon = Icons.Default.TrendingUp,
        iconTint = Color(0xFFF59E0B),
        containerColor = Color(0xFFFEF3C7).copy(alpha = 0.45f),
        modifier = Modifier
          .weight(1f)
          .testTag("result_block_size_card")
      )
    }

    // Binary Structure Card (Network, Subnet, Host bits)
    BinaryBitBreakdownCard(result = result)
  }
}

@Composable
private fun MetricCard(
  title: String,
  value: String,
  secondary: String,
  icon: ImageVector,
  iconTint: Color,
  containerColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.heightIn(min = 120.dp),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(14.dp),
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(iconTint.copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = secondary,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2
      )
    }
  }
}

@Composable
fun BinaryBitBreakdownCard(
  result: SubnetCalculationResult,
  modifier: Modifier = Modifier
) {
  ElevatedCard(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Desglose de Bits (32 bits IPv4)",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Text(
          text = "Clase ${result.ipClass.displayName.takeLast(1)} (/ ${result.newPrefix})",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold
        )
      }

      // Legend of bits
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        LegendItem(
          color = BitColorNetwork,
          label = "Red base: ${result.basePrefix} bits"
        )
        LegendItem(
          color = BitColorSubnet,
          label = "Subred: ${result.borrowedBits} bits (2^${result.borrowedBits})"
        )
        LegendItem(
          color = BitColorHost,
          label = "Host: ${result.hostBits} bits"
        )
      }

      // 32 Bit Bar visualizer
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(24.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
      ) {
        if (result.basePrefix > 0) {
          Box(
            modifier = Modifier
              .weight(result.basePrefix.toFloat())
              .fillMaxHeight()
              .background(BitColorNetwork),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "RED (${result.basePrefix})",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }
        }

        if (result.borrowedBits > 0) {
          Box(
            modifier = Modifier
              .weight(result.borrowedBits.toFloat())
              .fillMaxHeight()
              .background(BitColorSubnet),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "SUB (${result.borrowedBits})",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
              color = Color.Black,
              fontWeight = FontWeight.Bold
            )
          }
        }

        if (result.hostBits > 0) {
          Box(
            modifier = Modifier
              .weight(result.hostBits.toFloat())
              .fillMaxHeight()
              .background(BitColorHost),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "HOST (${result.hostBits})",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // 4 Octets Binary details
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = "Máscara binaria:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = result.newMask.toBinaryString(),
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = FontFamily.Monospace,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            ),
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }
}

@Composable
private fun LegendItem(
  color: Color,
  label: String
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Box(
      modifier = Modifier
        .size(10.dp)
        .clip(RoundedCornerShape(3.dp))
        .background(color)
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
