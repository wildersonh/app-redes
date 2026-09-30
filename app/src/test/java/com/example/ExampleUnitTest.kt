package com.example

import com.example.domain.model.CalculationMode
import com.example.domain.model.IpAddress
import com.example.domain.model.IpClass
import com.example.domain.usecase.SubnetCalculatorUseCases
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  private val useCases = SubnetCalculatorUseCases()

  @Test
  fun testClassC_ByHosts() {
    // 192.168.1.0/24 with 30 hosts
    // Required: 2^h - 2 >= 30 -> h = 5 (30 hosts) or h = 5 gives 30? Wait: 2^5 - 2 = 30!
    // Prefix: 32 - 5 = 27
    val result = useCases.calculateSubnet(
      rawIp = "192.168.1.0",
      mode = CalculationMode.BY_HOSTS,
      hostsNeeded = 30,
      targetCidrPrefix = null,
      isAcademicMode = false
    )

    assertEquals(IpClass.CLASS_C, result.ipClass)
    assertEquals(24, result.basePrefix)
    assertEquals(27, result.newPrefix)
    assertEquals("255.255.255.224", result.newMask.toDottedDecimal())
    assertEquals(3, result.borrowedBits)
    assertEquals(8L, result.totalSubnets) // 2^3 = 8
    assertEquals(30L, result.usableHostsPerSubnet) // 2^5 - 2 = 30
    assertEquals(32, result.blockSize) // 256 - 224 = 32
    assertEquals(4, result.modifiedOctetIndex)

    // Verify Subnet 0
    val sub0 = result.getSubnetAtIndex(0)
    assertEquals("192.168.1.0", sub0.networkIp.toDottedDecimal())
    assertEquals("192.168.1.1", sub0.firstHostIp?.toDottedDecimal())
    assertEquals("192.168.1.30", sub0.lastHostIp?.toDottedDecimal())
    assertEquals("192.168.1.31", sub0.broadcastIp.toDottedDecimal())

    // Verify Subnet 1
    val sub1 = result.getSubnetAtIndex(1)
    assertEquals("192.168.1.32", sub1.networkIp.toDottedDecimal())
    assertEquals("192.168.1.33", sub1.firstHostIp?.toDottedDecimal())
    assertEquals("192.168.1.62", sub1.lastHostIp?.toDottedDecimal())
    assertEquals("192.168.1.63", sub1.broadcastIp.toDottedDecimal())
  }

  @Test
  fun testClassB_ByMask_Octet3Jump() {
    // 172.16.0.0/16 with /22 mask
    val result = useCases.calculateSubnet(
      rawIp = "172.16.0.0",
      mode = CalculationMode.BY_MASK,
      hostsNeeded = null,
      targetCidrPrefix = 22,
      isAcademicMode = false
    )

    assertEquals(IpClass.CLASS_B, result.ipClass)
    assertEquals(16, result.basePrefix)
    assertEquals(22, result.newPrefix)
    assertEquals("255.255.252.0", result.newMask.toDottedDecimal())
    assertEquals(6, result.borrowedBits)
    assertEquals(64L, result.totalSubnets) // 2^6 = 64
    assertEquals(1022L, result.usableHostsPerSubnet) // 2^10 - 2 = 1022
    assertEquals(3, result.modifiedOctetIndex) // Jump in 3rd octet
    assertEquals(4, result.blockSize) // 256 - 252 = 4

    val sub0 = result.getSubnetAtIndex(0)
    assertEquals("172.16.0.0", sub0.networkIp.toDottedDecimal())
    assertEquals("172.16.0.1", sub0.firstHostIp?.toDottedDecimal())
    assertEquals("172.16.3.254", sub0.lastHostIp?.toDottedDecimal())
    assertEquals("172.16.3.255", sub0.broadcastIp.toDottedDecimal())

    val sub1 = result.getSubnetAtIndex(1)
    assertEquals("172.16.4.0", sub1.networkIp.toDottedDecimal())
    assertEquals("172.16.4.1", sub1.firstHostIp?.toDottedDecimal())
    assertEquals("172.16.7.254", sub1.lastHostIp?.toDottedDecimal())
    assertEquals("172.16.7.255", sub1.broadcastIp.toDottedDecimal())
  }

  @Test
  fun testClassA_ByMask_Octet2Jump() {
    // 10.0.0.0/8 with /12 mask
    val result = useCases.calculateSubnet(
      rawIp = "10.0.0.0",
      mode = CalculationMode.BY_MASK,
      hostsNeeded = null,
      targetCidrPrefix = 12,
      isAcademicMode = false
    )

    assertEquals(IpClass.CLASS_A, result.ipClass)
    assertEquals(8, result.basePrefix)
    assertEquals(12, result.newPrefix)
    assertEquals("255.240.0.0", result.newMask.toDottedDecimal())
    assertEquals(4, result.borrowedBits)
    assertEquals(16L, result.totalSubnets) // 2^4 = 16
    assertEquals(2, result.modifiedOctetIndex) // Jump in 2nd octet
    assertEquals(16, result.blockSize) // 256 - 240 = 16

    val sub1 = result.getSubnetAtIndex(1)
    assertEquals("10.16.0.0", sub1.networkIp.toDottedDecimal())
    assertEquals("10.16.0.1", sub1.firstHostIp?.toDottedDecimal())
    assertEquals("10.31.255.254", sub1.lastHostIp?.toDottedDecimal())
    assertEquals("10.31.255.255", sub1.broadcastIp.toDottedDecimal())
  }

  @Test
  fun testAcademicMode_SubtractsTwoSubnets() {
    val result = useCases.calculateSubnet(
      rawIp = "192.168.1.0",
      mode = CalculationMode.BY_MASK,
      hostsNeeded = null,
      targetCidrPrefix = 26,
      isAcademicMode = true
    )

    assertEquals(4L, result.totalSubnets)
    assertEquals(2L, result.academicUsableSubnets) // 4 - 2 = 2
    assertTrue(result.isAcademicMode)

    val sub0 = result.getSubnetAtIndex(0)
    assertTrue(sub0.isSubnetZero)
    assertFalse(sub0.isUsableInAcademicMode)

    val sub1 = result.getSubnetAtIndex(1)
    assertFalse(sub1.isSubnetZero)
    assertFalse(sub1.isBroadcastSubnet)
    assertTrue(sub1.isUsableInAcademicMode)

    val sub3 = result.getSubnetAtIndex(3)
    assertTrue(sub3.isBroadcastSubnet)
    assertFalse(sub3.isUsableInAcademicMode)
  }

  @Test
  fun testIpAddressParsingAndBinary() {
    val ip = IpAddress.fromString("192.168.1.1")
    assertNotNull(ip)
    assertEquals(listOf(192, 168, 1, 1), ip?.octets)
    assertEquals("192.168.1.1", ip?.toDottedDecimal())
    assertEquals("11000000.10101000.00000001.00000001", ip?.toBinaryString())

    val mask = IpAddress.fromCidr(24)
    assertEquals("255.255.255.0", mask.toDottedDecimal())
  }
}
