package com.example.domain.model

/**
 * Represents a single calculated subnet in the subnet table.
 */
data class SubnetItem(
  val index: Long,
  val displayNumber: Long,
  val networkIp: IpAddress,
  val firstHostIp: IpAddress?,
  val lastHostIp: IpAddress?,
  val broadcastIp: IpAddress,
  val usableHostsCount: Long,
  val isSubnetZero: Boolean,
  val isBroadcastSubnet: Boolean,
  val isUsableInAcademicMode: Boolean
) {
  val formattedRange: String
    get() {
      return if (firstHostIp != null && lastHostIp != null) {
        "${firstHostIp.toDottedDecimal()} - ${lastHostIp.toDottedDecimal()}"
      } else {
        "N/A (Sin hosts asignables)"
      }
    }
}
