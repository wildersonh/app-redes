package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.SubnetCalculationResult
import com.example.domain.model.SubnetItem

@Composable
fun SubnetDetailsDialog(
  subnet: SubnetItem,
  result: SubnetCalculationResult,
  onDismiss: () -> Unit,
  onCopied: (String) -> Unit
) {
  val clipboardManager = LocalClipboardManager.current

  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(20.dp),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Detalle Subred #${subnet.displayNumber}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer
        ) {
          Text(
            text = "/${result.newPrefix}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        DetailSection(
          title = "Dirección de Red (Network ID)",
          decimal = subnet.networkIp.toDottedDecimal(),
          binary = subnet.networkIp.toBinaryString(),
          color = MaterialTheme.colorScheme.primary
        )

        DetailSection(
          title = "Primera IP Válida",
          decimal = subnet.firstHostIp?.toDottedDecimal() ?: "N/A",
          binary = subnet.firstHostIp?.toBinaryString() ?: "N/A",
          color = Color(0xFF059669)
        )

        DetailSection(
          title = "Última IP Válida",
          decimal = subnet.lastHostIp?.toDottedDecimal() ?: "N/A",
          binary = subnet.lastHostIp?.toBinaryString() ?: "N/A",
          color = Color(0xFF059669)
        )

        DetailSection(
          title = "Dirección de Broadcast",
          decimal = subnet.broadcastIp.toDottedDecimal(),
          binary = subnet.broadcastIp.toBinaryString(),
          color = Color(0xFFE11D48)
        )

        Divider()

        // Technical specs summary
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = "Máscara de subred: ${result.newMask} (/${result.newPrefix})",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "Wildcard (máscara inversa): ${result.wildcardMask}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Capacidad útil: ${subnet.usableHostsCount} hosts asignables",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Tamaño del bloque (Salto): ${result.blockSize} en ${result.modifiedOctetName}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    confirmButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
          onClick = {
            val text = "Subred #${subnet.displayNumber}\nRed: ${subnet.networkIp}/${result.newPrefix}\nPrimer Host: ${subnet.firstHostIp}\nÚltimo Host: ${subnet.lastHostIp}\nBroadcast: ${subnet.broadcastIp}\nMáscara: ${result.newMask}"
            clipboardManager.setText(AnnotatedString(text))
            onCopied("Detalles de Subred #${subnet.displayNumber} copiados")
          },
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Copiar")
        }

        Button(
          onClick = onDismiss,
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Cerrar")
        }
      }
    }
  )
}

@Composable
private fun DetailSection(
  title: String,
  decimal: String,
  binary: String,
  color: Color
) {
  Surface(
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = decimal,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        ),
        color = color
      )
      Text(
        text = binary,
        style = MaterialTheme.typography.labelSmall.copy(
          fontFamily = FontFamily.Monospace,
          fontSize = 10.sp
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
