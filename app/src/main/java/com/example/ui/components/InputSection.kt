package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.model.CalculationMode
import com.example.domain.model.IpAddress
import com.example.domain.model.IpClass
import com.example.ui.SubnetUiState
import com.example.ui.theme.*

@Composable
fun InputSection(
  state: SubnetUiState,
  onIpChanged: (String) -> Unit,
  onModeChanged: (CalculationMode) -> Unit,
  onHostsChanged: (String) -> Unit,
  onMaskCidrChanged: (Int) -> Unit,
  onAcademicModeToggled: (Boolean) -> Unit,
  onCalculateClicked: () -> Unit,
  onPresetSelected: (String, Int?, Int?, Int?) -> Unit,
  onInfoClicked: () -> Unit,
  onClearClicked: () -> Unit,
  modifier: Modifier = Modifier
) {
  val focusManager = LocalFocusManager.current

  ElevatedCard(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.elevatedCardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Header with Title & Help
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.SettingsEthernet,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
          Text(
            text = "Configuración de Red",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        IconButton(
          onClick = onInfoClicked,
          modifier = Modifier.testTag("help_button")
        ) {
          Icon(
            imageVector = Icons.Outlined.HelpOutline,
            contentDescription = "Guía y Modo Académico",
            tint = MaterialTheme.colorScheme.primary
          )
        }
      }

      // Quick Preset Chips (Class A, B, C)
      Text(
        text = "Plantillas rápidas IPv4:",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = state.ipInput.startsWith("192.168"),
          onClick = { onPresetSelected("192.168.1.0", 30, 26, 24) },
          label = { Text("Clase C (192.168.1.0)") },
          modifier = Modifier.testTag("preset_class_c")
        )
        FilterChip(
          selected = state.ipInput.startsWith("172.16"),
          onClick = { onPresetSelected("172.16.0.0", 500, 22, 16) },
          label = { Text("Clase B (172.16.0.0)") },
          modifier = Modifier.testTag("preset_class_b")
        )
        FilterChip(
          selected = state.ipInput.startsWith("10.0"),
          onClick = { onPresetSelected("10.0.0.0", 2000, 20, 8) },
          label = { Text("Clase A (10.0.0.0)") },
          modifier = Modifier.testTag("preset_class_a")
        )
      }

      // IP Input Field
      OutlinedTextField(
        value = state.ipInput,
        onValueChange = onIpChanged,
        label = { Text("Dirección IP Base") },
        placeholder = { Text("Ej. 192.168.1.0 o 10.0.0.0") },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Dns,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
          )
        },
        trailingIcon = {
          if (state.ipInput.isNotEmpty()) {
            IconButton(onClick = onClearClicked) {
              Icon(Icons.Default.Clear, contentDescription = "Limpiar IP")
            }
          }
        },
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Number,
          imeAction = ImeAction.Next
        ),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.SemiBold
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("ip_input_field")
      )

      // IP Class Badge & Base Mask indicator
      state.detectedClass?.let { ipClass ->
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = when (ipClass) {
            IpClass.CLASS_A -> MaterialTheme.colorScheme.primaryContainer
            IpClass.CLASS_B -> MaterialTheme.colorScheme.secondaryContainer
            IpClass.CLASS_C -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
          },
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "${ipClass.displayName} (Base: /${ipClass.defaultPrefix} • ${ipClass.defaultMask})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // Calculation Mode Selector (Segmented / Radio: Hosts requeridos vs Máscara)
      Text(
        text = "Modo de cálculo:",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
      ) {
        SegmentedButton(
          selected = state.calculationMode == CalculationMode.BY_HOSTS,
          onClick = { onModeChanged(CalculationMode.BY_HOSTS) },
          shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
          icon = { SegmentedButtonDefaults.Icon(active = state.calculationMode == CalculationMode.BY_HOSTS) },
          modifier = Modifier.testTag("mode_hosts_button")
        ) {
          Text("Hosts requeridos")
        }
        SegmentedButton(
          selected = state.calculationMode == CalculationMode.BY_MASK,
          onClick = { onModeChanged(CalculationMode.BY_MASK) },
          shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
          icon = { SegmentedButtonDefaults.Icon(active = state.calculationMode == CalculationMode.BY_MASK) },
          modifier = Modifier.testTag("mode_mask_button")
        ) {
          Text("Máscara de subred")
        }
      }

      // Dynamic Input based on Mode
      AnimatedVisibility(visible = state.calculationMode == CalculationMode.BY_HOSTS) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = state.hostsInput,
            onValueChange = onHostsChanged,
            label = { Text("Cantidad de Hosts útiles necesarios") },
            placeholder = { Text("Ej. 30, 50, 100, 500") },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Computer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
              )
            },
            keyboardOptions = KeyboardOptions(
              keyboardType = KeyboardType.Number,
              imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
              focusManager.clearFocus()
              onCalculateClicked()
            }),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("hosts_input_field")
          )

          // Popular host presets
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("14", "30", "62", "126", "254").forEach { preset ->
              SuggestionChip(
                onClick = { onHostsChanged(preset) },
                label = { Text("$preset hosts") }
              )
            }
          }
        }
      }

      AnimatedVisibility(visible = state.calculationMode == CalculationMode.BY_MASK) {
        val minCidr = state.detectedClass?.defaultPrefix ?: 8
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          val activeMask = IpAddress.fromCidr(state.maskCidrInput)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Máscara seleccionada: /${state.maskCidrInput}",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = activeMask.toDottedDecimal(),
              style = MaterialTheme.typography.bodyMedium,
              fontFamily = FontFamily.Monospace,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Slider(
            value = state.maskCidrInput.toFloat(),
            onValueChange = { onMaskCidrChanged(it.toInt()) },
            valueRange = minCidr.toFloat()..30f,
            steps = (30 - minCidr - 1).coerceAtLeast(0),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("mask_slider")
          )

          // Common CIDR shortcuts
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf(24, 25, 26, 27, 28, 29, 30).filter { it >= minCidr }.forEach { cidr ->
              FilterChip(
                selected = state.maskCidrInput == cidr,
                onClick = { onMaskCidrChanged(cidr) },
                label = { Text("/$cidr") }
              )
            }
          }
        }
      }

      // Academic Mode Switch (RFC 950 vs RFC 1812)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Modo Académico (RFC 950)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = if (state.isAcademicMode) "Fórmula clásica (2ˢ - 2 subredes)" else "Estándar moderno RFC 1812 (Subnet Zero: 2ˢ)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Switch(
          checked = state.isAcademicMode,
          onCheckedChange = onAcademicModeToggled,
          modifier = Modifier.testTag("academic_mode_switch")
        )
      }

      // Error Banner
      state.inputError?.let { error ->
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.errorContainer,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.error
            )
            Text(
              text = error,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onErrorContainer
            )
          }
        }
      }

      // Calculate Button
      Button(
        onClick = {
          focusManager.clearFocus()
          onCalculateClicked()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("calculate_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary
        )
      ) {
        Icon(
          imageVector = Icons.Default.Calculate,
          contentDescription = null,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Calcular Subredes",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
