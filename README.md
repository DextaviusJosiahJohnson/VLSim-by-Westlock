# VLSim - Network Topology Engine

Special thanks to Nico for the original concept and logic inspiration. You can view the original web-based implementation here: https://nicoooo-23.github.io/VLSM-Calculator/

## Overview
A native Android Variable Length Subnet Masking (VLSM) calculator and network topology engine. This engineering tool calculates optimal IP allocations for complex network topologies to prevent IP overlap. 

## Key Features
* **Dynamic Topology Calculation:** Automatically sorts and allocates subnets based on required host sizes using the VLSM method.
* **Comprehensive Network Modeling:** Supports configuring and linking complex relationships between Routers, Switches, distinct WAN links, and individual Devices.
* **Persistent Storage:** Users can save, load, and manage network configurations locally on the device.
* **Modern UI:** Built with a custom, high-contrast dark theme utilizing zero-rounded corners for a technical, hardware-focused aesthetic.

## Tech Stack & Architecture
* **Language:** 100% Kotlin
* **UI Framework:** Jetpack Compose
* **Local Database:** Room (SQLite)
* **Asynchronous Data:** Kotlin Coroutines and Flow
* **Architecture:** MVVM (Model-View-ViewModel) pattern

## Screenshots
<img width="395" height="799" alt="image" src="https://github.com/user-attachments/assets/f103d2dc-fd2c-415a-8053-6aea89984108" />
<img width="390" height="798" alt="image" src="https://github.com/user-attachments/assets/2715d92d-232e-4fe3-aab8-df023d9df1ac" />
<img width="391" height="795" alt="image" src="https://github.com/user-attachments/assets/cf79f4f3-2c97-4a8c-9f73-8e6d14fd180a" />
<img width="394" height="798" alt="image" src="https://github.com/user-attachments/assets/dc686bcc-381f-469e-9998-5707bbdbeaa4" />
<img width="391" height="797" alt="image" src="https://github.com/user-attachments/assets/ae183b4e-535b-4261-b0bc-cccaa6124421" />


## Built on TEARS, By Students For Students.
