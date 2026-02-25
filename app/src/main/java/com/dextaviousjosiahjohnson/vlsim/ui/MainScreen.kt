package com.dextaviousjosiahjohnson.vlsim.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dextaviousjosiahjohnson.vlsim.math.*
import com.dextaviousjosiahjohnson.vlsim.ui.theme.ThemeState
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: VlsmViewModel, themeState: MutableState<ThemeState>) {
    // Inputs
    var routerInput by remember { mutableStateOf("") }
    var switchInput by remember { mutableStateOf("") }
    var switchHostInput by remember { mutableStateOf("") }
    var selectedRouterForSwitch by remember { mutableStateOf("") }
    var deviceInput by remember { mutableStateOf("") }
    var deviceHostInput by remember { mutableStateOf("") }
    var selectedParentForDevice by remember { mutableStateOf("") }
    var wanRouterA by remember { mutableStateOf("") }
    var wanRouterB by remember { mutableStateOf("") }

    // Dialogs
    var showSaveDialog by remember { mutableStateOf(false) }
    var showLoadDialog by remember { mutableStateOf(false) }
    var saveNameInput by remember { mutableStateOf("") }

    val savedCalculations by viewModel.savedCalculations.collectAsState(initial = emptyList())
    val hardBorder = BorderStroke(1.dp, themeState.value.primaryColor)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- TOP BAR ---
            Row(
                modifier = Modifier.fillMaxWidth().background(themeState.value.surfaceColor).border(hardBorder).padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("VLSM ENGINE", style = MaterialTheme.typography.headlineMedium, color = themeState.value.primaryColor)
                Row {
                    Button(onClick = { showSaveDialog = true }, shape = RectangleShape, modifier = Modifier.padding(end = 4.dp)) { Text("SAVE") }
                    Button(onClick = { showLoadDialog = true }, shape = RectangleShape) { Text("LOAD") }
                }
            }

            LazyColumn(modifier = Modifier.padding(16.dp).fillMaxSize()) {

                // --- NETWORK CONFIG ---
                item {
                    OutlinedTextField(
                        value = viewModel.baseNetwork, onValueChange = { viewModel.baseNetwork = it },
                        label = { Text("Base Network (CIDR)") }, modifier = Modifier.fillMaxWidth(), shape = RectangleShape
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- ROUTERS ---
                item { SectionHeader("ROUTERS") }
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = routerInput, onValueChange = { routerInput = it }, label = { Text("Router Name") }, modifier = Modifier.weight(1f), shape = RectangleShape)
                        Button(onClick = { if (routerInput.isNotBlank() && !viewModel.routers.contains(routerInput)) { viewModel.routers = viewModel.routers + routerInput; routerInput = "" } }, shape = RectangleShape, modifier = Modifier.height(56.dp).padding(start = 8.dp)) { Text("ADD") }
                    }
                }
                items(viewModel.routers) { r ->
                    ListItemRow(text = r, onDelete = { viewModel.removeRouter(r) })
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }

                // --- WAN LINKS ---
                item { SectionHeader("WAN LINKS") }
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        SimpleDropdown(viewModel.routers, wanRouterA, "Router A", Modifier.weight(1f)) { wanRouterA = it }
                        Spacer(modifier = Modifier.width(8.dp))
                        SimpleDropdown(viewModel.routers, wanRouterB, "Router B", Modifier.weight(1f)) { wanRouterB = it }
                    }
                    Button(onClick = {
                        if (wanRouterA.isNotBlank() && wanRouterB.isNotBlank() && wanRouterA != wanRouterB) {
                            viewModel.wans = viewModel.wans + WanData(UUID.randomUUID().toString(), wanRouterA, wanRouterB)
                            wanRouterA = ""; wanRouterB = ""
                        }
                    }, shape = RectangleShape, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("LINK ROUTERS") }
                }
                items(viewModel.wans) { w ->
                    ListItemRow(text = "${w.routerA} <---> ${w.routerB}", onDelete = { viewModel.wans = viewModel.wans - w })
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }

                // --- SWITCHES ---
                item { SectionHeader("SWITCHES") }
                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = switchInput, onValueChange = { switchInput = it }, label = { Text("Switch Name") }, modifier = Modifier.weight(1f), shape = RectangleShape)
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(value = switchHostInput, onValueChange = { switchHostInput = it }, label = { Text("Host") }, modifier = Modifier.weight(0.5f), shape = RectangleShape, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        Spacer(modifier = Modifier.width(8.dp))
                        SimpleDropdown(viewModel.routers, selectedRouterForSwitch, "End", Modifier.weight(1f)) { selectedRouterForSwitch = it }
                    }
                    Button(onClick = {
                        if (switchInput.isNotBlank() && selectedRouterForSwitch.isNotBlank()) {
                            val hosts = switchHostInput.toIntOrNull() ?: 0
                            viewModel.switches = viewModel.switches + SwitchData(UUID.randomUUID().toString(), switchInput, selectedRouterForSwitch, hosts)
                            switchInput = ""
                            switchHostInput = ""
                        }
                    }, shape = RectangleShape, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("ADD SWITCH") }
                }
                items(viewModel.switches) { s ->
                    val hostString = if (s.hostCount > 0) " (${s.hostCount} hosts)" else ""
                    ListItemRow(text = "${s.name} (on ${s.routerName})$hostString", onDelete = { viewModel.removeSwitch(s) })
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }

                // --- DEVICES ---
                item { SectionHeader("DEVICES") }
                item {
                    val possibleParents = viewModel.routers + viewModel.switches.map { it.name }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = deviceInput, onValueChange = { deviceInput = it }, label = { Text("Device Name") }, modifier = Modifier.weight(1f), shape = RectangleShape)
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(value = deviceHostInput, onValueChange = { deviceHostInput = it }, label = { Text("Host") }, modifier = Modifier.weight(0.5f), shape = RectangleShape, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                        Spacer(modifier = Modifier.width(8.dp))
                        SimpleDropdown(possibleParents, selectedParentForDevice, "End", Modifier.weight(1f)) { selectedParentForDevice = it }
                    }
                    Button(onClick = {
                        if (deviceInput.isNotBlank() && selectedParentForDevice.isNotBlank()) {
                            val hosts = deviceHostInput.toIntOrNull() ?: 1
                            viewModel.devices = viewModel.devices + DeviceData(UUID.randomUUID().toString(), deviceInput, selectedParentForDevice, hosts)
                            deviceInput = ""
                            deviceHostInput = ""
                        }
                    }, shape = RectangleShape, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("ADD DEVICE") }
                }
                items(viewModel.devices) { d ->
                    val hostString = if (d.hostCount > 1) " (${d.hostCount} hosts)" else ""
                    ListItemRow(text = "${d.name} (on ${d.parentName})$hostString", onDelete = { viewModel.devices = viewModel.devices - d })
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }

                // --- EXECUTE ---
                item {
                    Button(onClick = { viewModel.calculateTopology() }, shape = RectangleShape, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = themeState.value.primaryColor, contentColor = Color.Black)) {
                        Text("CALCULATE TOPOLOGY", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- RESULTS ---
                viewModel.resultData?.let { res ->
                    if (res.errors.isNotEmpty()) {
                        item {
                            res.errors.forEach { err -> Text("ERROR: $err", color = Color.Red, fontWeight = FontWeight.Bold) }
                        }
                    } else {
                        item { SectionHeader("ROUTER CONFIGURATIONS") }
                        items(res.routers) { r -> ConfigCard("Router: ${r.name}") {
                            r.interfaces.forEach { iface -> Text("INT [${iface.networkName}] - IP: ${iface.ipAddress} | Mask: ${iface.subnetMask}") }
                        }}

                        item { SectionHeader("SWITCH CONFIGURATIONS") }
                        items(res.switches) { s -> ConfigCard("Switch: ${s.name}") {
                            Text("IP Address: ${s.ipAddress}")
                            Text("Subnet Mask: ${s.subnetMask}")
                            Text("Gateway: ${s.defaultGateway}")
                            Text("Attached Devices: ${s.attachedDevices}")
                        }}

                        item { SectionHeader("DEVICE CONFIGURATIONS") }
                        items(res.devices) { d -> ConfigCard("Device: ${d.name}") {
                            Text("IP Address: ${d.ipAddress}")
                            Text("Subnet Mask: ${d.subnetMask}")
                            Text("Gateway: ${d.defaultGateway}")
                        }}

                        item { SectionHeader("WAN CONFIGURATIONS") }
                        items(res.wans) { w -> ConfigCard("WAN: ${w.name}") {
                            Text("Network: ${w.networkAddress} | Mask: ${w.subnetMask}")
                            Text("${w.routerA} IP: ${w.ipA}")
                            Text("${w.routerB} IP: ${w.ipB}")
                        }}
                    }
                }
                item { Spacer(modifier = Modifier.height(64.dp)) }
            }
        }
    }

    // Dialogs...
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false }, shape = RectangleShape,
            title = { Text("SAVE TOPOLOGY") },
            text = { OutlinedTextField(value = saveNameInput, onValueChange = { saveNameInput = it }, label = { Text("Save Name") }, shape = RectangleShape) },
            confirmButton = { Button(onClick = {
                viewModel.saveCalculation(saveNameInput)
                showSaveDialog = false; saveNameInput = ""
            }, shape = RectangleShape) { Text("SAVE") } },
            dismissButton = { Button(onClick = { showSaveDialog = false }, shape = RectangleShape) { Text("CANCEL") } }
        )
    }

    if (showLoadDialog) {
        AlertDialog(
            onDismissRequest = { showLoadDialog = false }, shape = RectangleShape,
            title = { Text("LOAD TOPOLOGY") },
            text = {
                LazyColumn {
                    items(savedCalculations) { save ->
                        Card(
                            onClick = {
                                viewModel.loadCalculation(save)
                                showLoadDialog = false
                            },
                            shape = RectangleShape, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, Color.Gray)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(save.saveName, fontWeight = FontWeight.Bold)
                                Text("Network: ${save.baseNetwork}")
                            }
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { showLoadDialog = false }, shape = RectangleShape) { Text("CLOSE") } }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
    Divider(color = MaterialTheme.colorScheme.primary, thickness = 2.dp, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
fun ListItemRow(text: String, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).background(MaterialTheme.colorScheme.surface).border(1.dp, Color.DarkGray).padding(8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text)
        Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), shape = RectangleShape, contentPadding = PaddingValues(0.dp), modifier = Modifier.size(36.dp)) { Text("X") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDropdown(options: List<String>, selected: String, label: String, modifier: Modifier = Modifier, onSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = modifier) {
        OutlinedTextField(value = selected, onValueChange = {}, readOnly = true, label = { Text(label) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RectangleShape)
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { opt ->
                DropdownMenuItem(text = { Text(opt) }, onClick = { onSelected(opt); expanded = false })
            }
        }
    }
}

@Composable
fun ConfigCard(title: String, content: @Composable () -> Unit) {
    Card(shape = RectangleShape, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, MaterialTheme.colorScheme.primary), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
            content()
        }
    }
}