package com.example.domain.usecase

import com.example.domain.model.CalculationMode
import com.example.domain.model.IpAddress
import com.example.domain.model.IpClass
import com.example.domain.model.SubnetCalculationResult

/**
 * Encapsulates the IPv4 subnetting business logic following SOLID principles.
 * Eliminates all global variables from the legacy C++ code and supports Clases A, B, and C dynamically,
 * with full modern (RFC 1812) and academic (RFC 950) standards.
 */
class SubnetCalculatorUseCases {

  sealed class CalculationException(message: String) : Exception(message) {
    class InvalidIpAddress(val rawIp: String) :
      CalculationException("Dirección IP inválida: '$rawIp'. Debe tener formato A.B.C.D con valores entre 0 y 255.")

    class UnsupportedClass(val ipClass: IpClass) :
      CalculationException("La dirección pertenece a ${ipClass.displayName}. El cálculo de subredes solo aplica a Clases A, B y C.")

    class HostsTooLarge(val requested: Int, val maxPossible: Long, val basePrefix: Int) :
      CalculationException("La cantidad de hosts solicitada ($requested) excede la capacidad de la red base (/$basePrefix puede alojar hasta $maxPossible hosts).")

    class InvalidPrefix(val prefix: Int, val basePrefix: Int) :
      CalculationException("El prefijo /$prefix debe ser mayor o igual a la red base (/$basePrefix) y menor o igual a /32.")

    class InvalidMask(val rawMask: String) :
      CalculationException("Máscara de subred inválida: '$rawMask'. Debe ser una máscara continua (ej. 255.255.255.192).")
  }

  /**
   * Primary entry point for calculating IPv4 subnets.
   *
   * @param rawIp IPv4 address string (e.g. "192.168.1.0" or "10.0.0.0")
   * @param mode Calculation mode (BY_HOSTS or BY_MASK)
   * @param hostsNeeded Number of usable hosts needed (used if mode == BY_HOSTS)
   * @param targetCidrPrefix Target prefix length (used if mode == BY_MASK)
   * @param isAcademicMode If true, applies RFC 950 older formula (2^s - 2 subnets)
   * @param overrideBasePrefix Optional custom base prefix (defaults to Class default: /8, /16, or /24)
   */
  fun calculateSubnet(
    rawIp: String,
    mode: CalculationMode,
    hostsNeeded: Int?,
    targetCidrPrefix: Int?,
    isAcademicMode: Boolean = false,
    overrideBasePrefix: Int? = null
  ): SubnetCalculationResult {
    val ip = IpAddress.fromString(rawIp)
      ?: throw CalculationException.InvalidIpAddress(rawIp)

    val ipClass = IpClass.fromIp(ip)
    if (ipClass != IpClass.CLASS_A && ipClass != IpClass.CLASS_B && ipClass != IpClass.CLASS_C) {
      throw CalculationException.UnsupportedClass(ipClass)
    }

    val basePrefix = overrideBasePrefix ?: ipClass.defaultPrefix
    if (basePrefix !in 1..30) {
      throw CalculationException.InvalidPrefix(basePrefix, 1)
    }

    val baseMask = IpAddress.fromCidr(basePrefix)
    val actualNetworkBaseIp = ip and baseMask

    val newPrefix: Int = when (mode) {
      CalculationMode.BY_HOSTS -> {
        val required = hostsNeeded ?: 1
        if (required <= 0) {
          throw CalculationException.HostsTooLarge(required, 0, basePrefix)
        }
        calculatePrefixForHosts(required, basePrefix)
      }
      CalculationMode.BY_MASK -> {
        val prefix = targetCidrPrefix ?: basePrefix
        if (prefix < basePrefix || prefix > 32) {
          throw CalculationException.InvalidPrefix(prefix, basePrefix)
        }
        prefix
      }
    }

    val borrowedBits = newPrefix - basePrefix
    val hostBits = 32 - newPrefix
    val totalSubnets = 1L shl borrowedBits
    val academicUsableSubnets = if (totalSubnets >= 2) totalSubnets - 2L else 0L

    val totalAddressesPerSubnet = 1L shl hostBits
    val usableHostsPerSubnet = when {
      hostBits <= 0 -> 1L // /32 host route
      hostBits == 1 -> 2L // /31 RFC 3021
      else -> (1L shl hostBits) - 2L
    }

    val newMask = IpAddress.fromCidr(newPrefix)
    val wildcardMaskLong = (newMask.toLong().inv()) and 0xFFFFFFFFL
    val wildcardMask = IpAddress.fromLong(wildcardMaskLong)

    val modifiedOctetIndex = when (newPrefix) {
      in 1..8 -> 1
      in 9..16 -> 2
      in 17..24 -> 3
      else -> 4
    }

    val modifiedOctetMaskValue = newMask.octets[modifiedOctetIndex - 1]
    val blockSize = 256 - modifiedOctetMaskValue

    return SubnetCalculationResult(
      baseIp = ip,
      actualNetworkBaseIp = actualNetworkBaseIp,
      ipClass = ipClass,
      basePrefix = basePrefix,
      baseMask = baseMask,
      newPrefix = newPrefix,
      newMask = newMask,
      wildcardMask = wildcardMask,
      borrowedBits = borrowedBits,
      hostBits = hostBits,
      totalSubnets = totalSubnets,
      academicUsableSubnets = academicUsableSubnets,
      isAcademicMode = isAcademicMode,
      totalAddressesPerSubnet = totalAddressesPerSubnet,
      usableHostsPerSubnet = usableHostsPerSubnet,
      blockSize = if (blockSize == 0) 1 else blockSize,
      modifiedOctetIndex = modifiedOctetIndex,
      modifiedOctetMaskValue = modifiedOctetMaskValue
    )
  }

  /**
   * Determines the required CIDR prefix for a specified number of usable hosts.
   * Uses standard IPv4 host capacity formula: 2^h - 2 >= hostsNeeded
   */
  private fun calculatePrefixForHosts(hostsNeeded: Int, basePrefix: Int): Int {
    val maxAvailableHostBits = 32 - basePrefix
    val maxHostsInBase = if (maxAvailableHostBits >= 2) (1L shl maxAvailableHostBits) - 2L else 2L

    if (hostsNeeded > maxHostsInBase) {
      throw CalculationException.HostsTooLarge(hostsNeeded, maxHostsInBase, basePrefix)
    }

    // Special cases for small point-to-point requirements
    if (hostsNeeded == 1) {
      return 30 // standard /30 allows 2 hosts (enough for 1)
    }

    for (h in 2..maxAvailableHostBits) {
      val usable = (1L shl h) - 2L
      if (usable >= hostsNeeded) {
        return 32 - h
      }
    }

    return basePrefix
  }

  /**
   * Helper to parse a dotted-decimal mask (e.g. "255.255.255.192") into a prefix length.
   */
  fun parseDottedMaskToPrefix(rawMask: String): Int? {
    val maskIp = IpAddress.fromString(rawMask) ?: return null
    return IpAddress.maskToCidr(maskIp)
  }
}
