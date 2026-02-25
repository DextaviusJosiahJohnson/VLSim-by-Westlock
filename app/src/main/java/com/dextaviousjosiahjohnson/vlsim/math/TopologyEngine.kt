package com.dextaviousjosiahjohnson.vlsim.math

import androidx.annotation.Keep

// --- 1. THE DATA MODELS ---

@Keep
data class SwitchData(val id: String, val name: String, val routerName: String, val hostCount: Int = 0)
@Keep
data class DeviceData(val id: String, val name: String, val parentName: String, val hostCount: Int = 1)
@Keep
data class WanData(val id: String, val routerA: String, val routerB: String)

// --- OUTPUT CONFIGURATION MODELS ---
data class RouterInterface(val networkName: String, val ipAddress: String, val subnetMask: String)
data class RouterConfig(val name: String, val interfaces: MutableList<RouterInterface> = mutableListOf())
data class SwitchConfig(val name: String, val ipAddress: String, val defaultGateway: String, val subnetMask: String, val attachedDevices: Int)
data class DeviceConfig(val name: String, val ipAddress: String, val defaultGateway: String, val subnetMask: String)
data class WanResultConfig(val name: String, val routerA: String, val ipA: String, val routerB: String, val ipB: String, val subnetMask: String, val networkAddress: String)

data class FullTopologyResult(
    val routers: List<RouterConfig>,
    val switches: List<SwitchConfig>,
    val devices: List<DeviceConfig>,
    val wans: List<WanResultConfig>,
    val errors: List<String>
)

// Internal requirement tracker for the VLSM sorting phase
private data class SubnetReq(
    val name: String,
    val size: Int,
    val type: ReqType,
    val parentRouter: String,
    val switchData: SwitchData? = null,
    val devices: List<DeviceData> = emptyList(),
    val wanData: WanData? = null
)
private enum class ReqType { SWITCH_LAN, DIRECT_DEVICE, WAN_LINK }

// --- 2. THE UTILS ---

object IPUtils {
    fun validateCIDR(cidr: String): Boolean {
        val regex = Regex("^([0-9\\.]+)/(\\d{1,2})$")
        val match = regex.find(cidr) ?: return false
        val parts = match.groupValues[1].split(".")
        if (parts.size != 4 || parts.any { (it.toIntOrNull() ?: -1) !in 0..255 }) return false
        return match.groupValues[2].toIntOrNull() in 0..32
    }

    fun ipToLong(ip: String): Long = ip.split(".").fold(0L) { acc, s -> (acc shl 8) + s.toLong() }
    fun longToIp(ipLong: Long): String = "${(ipLong shr 24) and 255}.${(ipLong shr 16) and 255}.${(ipLong shr 8) and 255}.${ipLong and 255}"

    fun cidrToMask(prefix: Int): String {
        val mask = if (prefix == 0) 0L else (0xFFFFFFFFL shl (32 - prefix)) and 0xFFFFFFFFL
        return longToIp(mask)
    }

    fun networkBoundary(network: String, prefix: Int): Long {
        val mask = if (prefix == 0) 0L else (0xFFFFFFFFL shl (32 - prefix)) and 0xFFFFFFFFL
        return ipToLong(network) and mask
    }

    fun broadcastAddress(networkLong: Long, prefix: Int): Long {
        val hostBits = 32 - prefix
        val invertedMask = if (hostBits == 32) 0xFFFFFFFFL else (1L shl hostBits) - 1L
        return networkLong or invertedMask
    }
}

// --- 3. THE CALCULATOR ENGINE ---

class TopologyCalculator(cidr: String) {
    private val basePrefix: Int
    private val baseNetworkLong: Long
    private val baseBroadcast: Long
    private var currentPointer: Long

    init {
        val parts = cidr.split("/")
        basePrefix = parts[1].toInt()
        baseNetworkLong = IPUtils.networkBoundary(parts[0], basePrefix)
        baseBroadcast = IPUtils.broadcastAddress(baseNetworkLong, basePrefix)
        currentPointer = baseNetworkLong
    }

    private fun requiredHostBits(hosts: Int): Int {
        var h = 0
        while ((1L shl h) - 2L < hosts.toLong()) h++
        return h
    }

    fun calculate(
        routers: List<String>,
        switches: List<SwitchData>,
        devices: List<DeviceData>,
        wans: List<WanData>
    ): FullTopologyResult {

        val reqs = mutableListOf<SubnetReq>()
        val routerConfigs = routers.associateWith { RouterConfig(it) }.toMutableMap()
        val switchConfigs = mutableListOf<SwitchConfig>()
        val deviceConfigs = mutableListOf<DeviceConfig>()
        val wanConfigs = mutableListOf<WanResultConfig>()
        val errors = mutableListOf<String>()

        // 1. Group Switches and their Devices
        for (switch in switches) {
            val attachedDevices = devices.filter { it.parentName == switch.name }
            val neededHosts = maxOf(switch.hostCount, attachedDevices.size)
            reqs.add(SubnetReq(
                name = "LAN_${switch.name}",
                size = 2 + neededHosts,
                type = ReqType.SWITCH_LAN,
                parentRouter = switch.routerName,
                switchData = switch,
                devices = attachedDevices
            ))
        }

        // 2. Group Direct-to-Router Devices
        val directDevices = devices.filter { dev -> routers.contains(dev.parentName) }
        for (dev in directDevices) {
            reqs.add(SubnetReq(
                name = "LINK_${dev.name}",
                size = 1 + dev.hostCount,
                type = ReqType.DIRECT_DEVICE,
                parentRouter = dev.parentName,
                devices = listOf(dev)
            ))
        }

        // 3. Group WANs
        for (wan in wans) {
            reqs.add(SubnetReq(
                name = "WAN_${wan.routerA}_${wan.routerB}",
                size = 2,
                type = ReqType.WAN_LINK,
                parentRouter = wan.routerA,
                wanData = wan
            ))
        }

        // VLSM GOLDEN RULE: Sort by size descending
        reqs.sortByDescending { it.size }

        // Allocate
        for (req in reqs) {
            val h = requiredHostBits(req.size)
            val prefix = 32 - h
            val blockSize = 1L shl h

            if (prefix < basePrefix) {
                errors.add("Subnet '${req.name}' is too large for the base CIDR.")
                continue
            }

            val networkLong = currentPointer
            val broadcastLong = networkLong + blockSize - 1L

            if (broadcastLong > baseBroadcast) {
                errors.add("Ran out of IP space allocating '${req.name}'.")
                break
            }

            val subnetMask = IPUtils.cidrToMask(prefix)
            val networkIp = IPUtils.longToIp(networkLong)

            when (req.type) {
                ReqType.SWITCH_LAN -> {
                    val routerIp = IPUtils.longToIp(networkLong + 1L)
                    val switchIp = IPUtils.longToIp(networkLong + 2L)

                    routerConfigs[req.parentRouter]?.interfaces?.add(RouterInterface(req.switchData!!.name, routerIp, subnetMask))
                    switchConfigs.add(SwitchConfig(req.switchData!!.name, switchIp, routerIp, subnetMask, req.devices.size))

                    var ipOffset = 3L
                    for (dev in req.devices) {
                        deviceConfigs.add(DeviceConfig(dev.name, IPUtils.longToIp(networkLong + ipOffset), routerIp, subnetMask))
                        ipOffset++
                    }
                }
                ReqType.DIRECT_DEVICE -> {
                    val routerIp = IPUtils.longToIp(networkLong + 1L)
                    val devIp = IPUtils.longToIp(networkLong + 2L)

                    routerConfigs[req.parentRouter]?.interfaces?.add(RouterInterface("Direct to ${req.devices[0].name}", routerIp, subnetMask))
                    deviceConfigs.add(DeviceConfig(req.devices[0].name, devIp, routerIp, subnetMask))
                }
                ReqType.WAN_LINK -> {
                    val routerAIp = IPUtils.longToIp(networkLong + 1L)
                    val routerBIp = IPUtils.longToIp(networkLong + 2L)
                    val wan = req.wanData!!

                    routerConfigs[wan.routerA]?.interfaces?.add(RouterInterface("WAN to ${wan.routerB}", routerAIp, subnetMask))
                    routerConfigs[wan.routerB]?.interfaces?.add(RouterInterface("WAN to ${wan.routerA}", routerBIp, subnetMask))

                    wanConfigs.add(WanResultConfig(req.name, wan.routerA, routerAIp, wan.routerB, routerBIp, subnetMask, networkIp))
                }
            }

            currentPointer = broadcastLong + 1L
        }

        return FullTopologyResult(
            routers = routerConfigs.values.toList(),
            switches = switchConfigs,
            devices = deviceConfigs,
            wans = wanConfigs,
            errors = errors
        )
    }
}