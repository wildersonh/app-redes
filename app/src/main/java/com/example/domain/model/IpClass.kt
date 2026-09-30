package com.example.domain.model

/**
 * Represents the standard IPv4 Classful Network hierarchy: Class A, B, C, D, E.
 */
enum class IpClass(
  val displayName: String,
  val defaultPrefix: Int,
  val defaultMask: String,
  val firstOctetRange: IntRange,
  val description: String
) {
  CLASS_A(
    displayName = "Clase A",
    defaultPrefix = 8,
    defaultMask = "255.0.0.0",
    firstOctetRange = 1..126,
    description = "Redes muy grandes (Grandes corporaciones y backbones)"
  ),
  CLASS_B(
    displayName = "Clase B",
    defaultPrefix = 16,
    defaultMask = "255.255.0.0",
    firstOctetRange = 128..191,
    description = "Redes de tamaño mediano (Empresas y universidades)"
  ),
  CLASS_C(
    displayName = "Clase C",
    defaultPrefix = 24,
    defaultMask = "255.255.255.0",
    firstOctetRange = 192..223,
    description = "Redes pequeñas y locales (LANs domésticas y PyMEs)"
  ),
  CLASS_D(
    displayName = "Clase D (Multicast)",
    defaultPrefix = 24,
    defaultMask = "255.255.255.0",
    firstOctetRange = 224..239,
    description = "Direcciones de Multicast (no asignables a hosts individuales)"
  ),
  CLASS_E(
    displayName = "Clase E (Experimental)",
    defaultPrefix = 24,
    defaultMask = "255.255.255.0",
    firstOctetRange = 240..255,
    description = "Reservadas para investigación y uso futuro"
  ),
  LOOPBACK(
    displayName = "Loopback / Localhost",
    defaultPrefix = 8,
    defaultMask = "255.0.0.0",
    firstOctetRange = 127..127,
    description = "Rango de Loopback (127.0.0.0/8)"
  ),
  UNKNOWN(
    displayName = "Especial / No clasificada",
    defaultPrefix = 24,
    defaultMask = "255.255.255.0",
    firstOctetRange = 0..0,
    description = "Dirección 0.0.0.0 o fuera de rango estándar"
  );

  companion object {
    fun fromIp(ip: IpAddress): IpClass {
      val first = ip.octet1
      return when (first) {
        in 1..126 -> CLASS_A
        127 -> LOOPBACK
        in 128..191 -> CLASS_B
        in 192..223 -> CLASS_C
        in 224..239 -> CLASS_D
        in 240..255 -> CLASS_E
        else -> UNKNOWN
      }
    }
  }
}
