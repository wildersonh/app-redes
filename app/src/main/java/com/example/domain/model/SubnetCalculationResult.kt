package com.example.domain.model

/**
 * Result of a subnetting computation containing all network parameters and
 * an on-demand generator for any individual subnet item.
 */
data class SubnetCalculationResult(
  val baseIp: IpAddress,
  val actualNetworkBaseIp: IpAddress,
  val ipClass: IpClass,
  val basePrefix: Int,
  val baseMask: IpAddress,
  val newPrefix: Int,
  val newMask: IpAddress,
  val wildcardMask: IpAddress,
  val borrowedBits: Int,
  val hostBits: Int,
  val totalSubnets: Long,
  val academicUsableSubnets: Long,
  val isAcademicMode: Boolean,
  val totalAddressesPerSubnet: Long,
  val usableHostsPerSubnet: Long,
  val blockSize: Int,
  val modifiedOctetIndex: Int,
  val modifiedOctetMaskValue: Int
) {
  val activeSubnetCount: Long
    get() = if (isAcademicMode) academicUsableSubnets else totalSubnets

  val modifiedOctetName: String
    get() = when (modifiedOctetIndex) {
      1 -> "1° Octeto (bits 1-8)"
      2 -> "2° Octeto (bits 9-16)"
      3 -> "3° Octeto (bits 17-24)"
      else -> "4° Octeto (bits 25-32)"
    }

  /**
   * Calculates subnet details for a given 0-based index O(1).
   * This enables instant rendering and pagination without memory overhead.
   */
  fun getSubnetAtIndex(index: Long): SubnetItem {
    require(index in 0 until totalSubnets) { "Index $index out of bounds [0, $totalSubnets)" }

    val networkLong = actualNetworkBaseIp.toLong() + (index * totalAddressesPerSubnet)
    val broadcastLong = networkLong + totalAddressesPerSubnet - 1

    val networkIp = IpAddress.fromLong(networkLong)
    val broadcastIp = IpAddress.fromLong(broadcastLong)

    val (firstHostIp, lastHostIp) = when {
      usableHostsPerSubnet <= 0 -> Pair(null, null)
      newPrefix == 31 -> Pair(networkIp, broadcastIp) // RFC 3021
      newPrefix == 32 -> Pair(networkIp, networkIp)
      else -> Pair(
        IpAddress.fromLong(networkLong + 1),
        IpAddress.fromLong(broadcastLong - 1)
      )
    }

    val isSubnetZero = (index == 0L)
    val isBroadcastSubnet = (index == totalSubnets - 1L)
    val isUsableInAcademic = !isSubnetZero && !isBroadcastSubnet

    return SubnetItem(
      index = index,
      displayNumber = index + 1,
      networkIp = networkIp,
      firstHostIp = firstHostIp,
      lastHostIp = lastHostIp,
      broadcastIp = broadcastIp,
      usableHostsCount = usableHostsPerSubnet,
      isSubnetZero = isSubnetZero,
      isBroadcastSubnet = isBroadcastSubnet,
      isUsableInAcademicMode = isUsableInAcademic
    )
  }

  /**
   * Locates which subnet index contains the specified IP address.
   * Returns null if the target IP is outside this base network block.
   */
  fun findSubnetIndexForIp(target: IpAddress): Long? {
    val baseLong = actualNetworkBaseIp.toLong()
    val targetLong = target.toLong()
    val totalBlockAddresses = totalSubnets * totalAddressesPerSubnet
    val endLong = baseLong + totalBlockAddresses - 1

    if (targetLong < baseLong || targetLong > endLong) {
      return null
    }

    val offset = targetLong - baseLong
    return offset / totalAddressesPerSubnet
  }
}
