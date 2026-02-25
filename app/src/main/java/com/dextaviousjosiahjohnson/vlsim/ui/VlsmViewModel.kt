package com.dextaviousjosiahjohnson.vlsim.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dextaviousjosiahjohnson.vlsim.data.CalculationDao
import com.dextaviousjosiahjohnson.vlsim.data.SavedCalculation
import com.dextaviousjosiahjohnson.vlsim.math.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class VlsmViewModel(private val dao: CalculationDao) : ViewModel() {
    var baseNetwork by mutableStateOf("192.168.1.0/24")
    var routers by mutableStateOf(listOf("Router A"))
    var switches by mutableStateOf(listOf<SwitchData>())
    var devices by mutableStateOf(listOf<DeviceData>())
    var wans by mutableStateOf(listOf<WanData>())
    var resultData by mutableStateOf<FullTopologyResult?>(null)

    val savedCalculations = dao.getAllCalculations()

    fun calculateTopology() {
        try {
            val calc = TopologyCalculator(baseNetwork)
            resultData = calc.calculate(routers, switches, devices, wans)
        } catch (e: Exception) {
            resultData = FullTopologyResult(emptyList(), emptyList(), emptyList(), emptyList(), listOf(e.message ?: "Unknown Error"))
        }
    }

    fun saveCalculation(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertCalculation(
                SavedCalculation(
                    saveName = name,
                    baseNetwork = baseNetwork,
                    routers = routers,
                    switches = switches,
                    devices = devices,
                    wans = wans
                )
            )
        }
    }

    fun loadCalculation(save: SavedCalculation) {
        baseNetwork = save.baseNetwork
        routers = save.routers
        switches = save.switches
        devices = save.devices
        wans = save.wans
        resultData = null
    }

    fun removeRouter(r: String) {
        routers = routers - r
        switches = switches.filter { it.routerName != r }
        wans = wans.filter { it.routerA != r && it.routerB != r }
        devices = devices.filter { dev -> dev.parentName != r && !switches.any { s -> s.name == dev.parentName && s.routerName == r } }
    }

    fun removeSwitch(s: SwitchData) {
        switches = switches - s
        devices = devices.filter { it.parentName != s.name }
    }
}

class VlsmViewModelFactory(private val dao: CalculationDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VlsmViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VlsmViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}