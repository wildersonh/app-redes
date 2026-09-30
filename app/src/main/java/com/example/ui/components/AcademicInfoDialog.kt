package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AcademicInfoDialog(
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    shape = RoundedCornerShape(20.dp),
    icon = {
      Icon(
        imageVector = Icons.Default.Info,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(32.dp)
      )
    },
    title = {
      Text(
        text = "Estándar Moderno vs Modo Académico",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "¿Por qué el código heredado restaba 2 a las subredes?",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        Text(
          text = "En 1985, el RFC 950 estipuló que la primera subred (Subnet Zero, todos los bits de subred en 0) y la última subred (Broadcast Subnet, todos los bits de subred en 1) debían ser descartadas para evitar confusiones en protocolos de enrutamiento antiguos como RIPv1.\n\nPor eso se usaba la fórmula antigua: Subredes = 2ˢ - 2.",
          style = MaterialTheme.typography.bodyMedium
        )

        Divider()

        Text(
          text = "Estándar Moderno (RFC 1812 y CIDR):",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.secondary
        )

        Text(
          text = "Desde 1995 (RFC 1812) y la adopción universal de CIDR, todas las 2ˢ subredes son 100% válidas y utilizables (función 'ip subnet-zero' habilitada por defecto en Cisco, Linux y equipos modernos).\n\nEn esta app puedes activar el 'Modo Académico' si tu profesor o examen aún exige la regla de 1985 (2ˢ - 2), pero por defecto la app opera según la ingeniería de redes actual.",
          style = MaterialTheme.typography.bodyMedium
        )

        Divider()

        Text(
          text = "Fórmula de Hosts:",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )

        Text(
          text = "Para hosts siempre se restan 2 (2ʰ - 2): una dirección para el Identificador de Red (todos los bits de host en 0) y otra para el Broadcast de la subred (todos los bits de host en 1).",
          style = MaterialTheme.typography.bodyMedium
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Entendido")
      }
    }
  )
}
