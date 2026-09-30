package com.example.domain.model

/**
 * Representation of an IPv4 address.
 * Encapsulates octet parsing, binary conversion, and mathematical operations.
 */
data class IpAddress(
  val octets: List<Int>
) {
  init {
    require(octets.size == 4) { "IPv4 address must have exactly 4 octets" }
    require(octets.all { it in 0..255 }) { "Each octet must be between 0 and 255" }
  }

  val octet1: Int get() = octets[0]
  val octet2: Int get() = octets[1]
  val octet3: Int get() = octets[2]
  val octet4: Int get() = octets[3]

  /**
   * Converts the IP address to a 32-bit unsigned integer stored in a Long.
   */
  fun toLong(): Long {
    return ((octets[0].toLong() and 0xFF) shl 24) or
        ((octets[1].toLong() and 0xFF) shl 16) or
        ((octets[2].toLong() and 0xFF) shl 8) or
        (octets[3].toLong() and 0xFF)
  }

  /**
   * Returns standard dotted decimal notation: e.g. "192.168.1.1"
   */
  fun toDottedDecimal(): String {
    return octets.joinToString(".")
  }

  /**
   * Returns 32-bit binary string separated by dots:
   * e.g. "11000000.10101000.00000001.00000001"
   */
  fun toBinaryString(): String {
    return octets.joinToString(".") { octet ->
      octet.toString(2).padStart(8, '0')
    }
  }

  /**
   * Returns 32-bit contiguous binary string without dots.
   */
  fun toRawBinaryString(): String {
    return octets.joinToString("") { octet ->
      octet.toString(2).padStart(8, '0')
    }
  }

  /**
   * Adds an offset to the IP address and returns a new IpAddress.
   */
  fun plus(offset: Long): IpAddress {
    val newLong = (toLong() + offset) and 0xFFFFFFFFL
    return fromLong(newLong)
  }

  /**
   * Subtracts an offset from the IP address and returns a new IpAddress.
   */
  fun minus(offset: Long): IpAddress {
    val newLong = (toLong() - offset) and 0xFFFFFFFFL
    return fromLong(newLong)
  }

  /**
   * Bitwise AND with a mask
   */
  infix fun and(mask: IpAddress): IpAddress {
    return fromLong(toLong() and mask.toLong())
  }

  /**
   * Bitwise OR with a mask
   */
  infix fun or(mask: IpAddress): IpAddress {
    return fromLong(toLong() or mask.toLong())
  }

  override fun toString(): String = toDottedDecimal()

  companion object {
    val ZERO = IpAddress(listOf(0, 0, 0, 0))
    val BROADCAST = IpAddress(listOf(255, 255, 255, 255))

    /**
     * Parses a dotted string such as "192.168.1.1".
     * Returns null if invalid.
     */
    fun fromString(ipStr: String): IpAddress? {
      val trimmed = ipStr.trim()
      val parts = trimmed.split(".")
      if (parts.size != 4) return null

      val octets = mutableListOf<Int>()
      for (part in parts) {
        val num = part.toIntOrNull() ?: return null
        if (num !in 0..255) return null
        // Prevent leading zeros unless the number is exactly "0"
        if (part.length > 1 && part.startsWith("0")) return null
        octets.add(num)
      }
      return IpAddress(octets)
    }

    /**
     * Creates an IpAddress from a 32-bit unsigned Long value.
     */
    fun fromLong(value: Long): IpAddress {
      val v = value and 0xFFFFFFFFL
      val o1 = ((v ushr 24) and 0xFF).toInt()
      val o2 = ((v ushr 16) and 0xFF).toInt()
      val o3 = ((v ushr 8) and 0xFF).toInt()
      val o4 = (v and 0xFF).toInt()
      return IpAddress(listOf(o1, o2, o3, o4))
    }

    /**
     * Creates a Subnet Mask IpAddress from CIDR prefix length (0..32).
     */
    fun fromCidr(prefix: Int): IpAddress {
      require(prefix in 0..32) { "Prefix must be in 0..32" }
      if (prefix == 0) return ZERO
      val maskLong = (-1L shl (32 - prefix)) and 0xFFFFFFFFL
      return fromLong(maskLong)
    }

    /**
     * Converts a subnet mask to its CIDR prefix (count of leading 1s).
     * Returns null if the mask is not contiguous.
     */
    fun maskToCidr(mask: IpAddress): Int? {
      val binary = mask.toRawBinaryString()
      val firstZero = binary.indexOf('0')
      if (firstZero == -1) return 32
      // Check that all subsequent characters are '0'
      if (binary.substring(firstZero).contains('1')) {
        return null // Non-contiguous mask
      }
      return firstZero
    }

    /**
     * Validates an IPv4 dotted decimal string.
     */
    fun isValid(ipStr: String): Boolean {
      return fromString(ipStr) != null
    }
  }
}
