import os
import matplotlib.pyplot as plt
import matplotlib.patches as patches

output_dir = os.path.join(os.path.dirname(__file__), "..", "docs", "graphs")
os.makedirs(output_dir, exist_ok=True)

fig, ax = plt.subplots(figsize=(14, 9), dpi=300)
ax.set_xlim(0, 100)
ax.set_ylim(0, 100)
ax.axis('off')

# Color Palette (Academic / Clean)
bg_color = "#FFFFFF"
fig.patch.set_facecolor(bg_color)
ax.set_facecolor(bg_color)

def draw_layer_box(ax, x, y, w, h, title, subtitle, box_color, border_color):
    rect = patches.FancyBboxPatch(
        (x, y), w, h,
        boxstyle="round,pad=0.8,rounding_size=1.2",
        facecolor=box_color, edgecolor=border_color, linewidth=1.5, alpha=0.95
    )
    ax.add_patch(rect)
    ax.text(x + 2, y + h - 3.2, title, fontsize=11, fontweight='bold', color=border_color)
    ax.text(x + w - 2, y + h - 3.2, subtitle, fontsize=9, fontstyle='italic', color='#64748B', ha='right')

def draw_component_card(ax, x, y, w, h, title, details, icon_text=""):
    rect = patches.FancyBboxPatch(
        (x, y), w, h,
        boxstyle="round,pad=0.5,rounding_size=0.8",
        facecolor="#FFFFFF", edgecolor="#CBD5E1", linewidth=1.2
    )
    ax.add_patch(rect)
    ax.text(x + w/2, y + h - 2.8, title, fontsize=9.5, fontweight='bold', color='#1E293B', ha='center')
    ax.text(x + w/2, y + 2.2, details, fontsize=7.5, color='#64748B', ha='center')

# 1. Edge Layer
draw_layer_box(ax, 3, 76, 94, 20, "LAYER 1: HETEROGENEOUS IoT EDGE FLEET", "Cryptographically Attested Edge Devices", "#F8FAFC", "#3B82F6")
draw_component_card(ax, 5.5, 78, 20, 13, "ESP32-S3 Node", "did:zt:dev:0xE37D\nSensors: Temp / Humid")
draw_component_card(ax, 29.5, 78, 20, 13, "RPi CM4 Gateway", "did:zt:dev:0xFBC0\nIndustrial Modbus / CAN")
draw_component_card(ax, 53.5, 78, 20, 13, "STM32F4 Controller", "did:zt:dev:0x8053\nWater Treatment Actuator")
draw_component_card(ax, 77.5, 78, 20, 13, "Jetson Orin Nano", "did:zt:dev:0x5680\nEdge AI Perimeter Node")

# 2. Ingestion Broker Layer
draw_layer_box(ax, 15, 59, 70, 12, "LAYER 2: SECURE TRANSPORT & INGESTION BROKER", "Eclipse Mosquitto (mTLS 1.3 / MQTT 5.0)", "#F0FDF4", "#10B981")
draw_component_card(ax, 18, 61, 30, 7.5, "MQTT Telemetry Ingress", "Topics: iot/+/telemetry | QOS 1")
draw_component_card(ax, 52, 61, 30, 7.5, "Alerts & Quarantine Topics", "Topics: iot/+/alerts | iot/+/control")

# 3. Trust Engine & PDP Layer
draw_layer_box(ax, 3, 31, 94, 23, "LAYER 3: ZERO TRUST CORE PDP & TRUST ENGINE", "Spring Boot 3 + Java 21 Virtual Threads (Sub-15ms)", "#EEF2FF", "#6366F1")
draw_component_card(ax, 5.5, 33, 20, 16, "Telemetry Ingest Service", "Paho Client Worker Pool\nPayload Decoupling & Queue")
draw_component_card(ax, 29.5, 33, 20, 16, "Mathematical Trust Engine", "T(t) Formulation Engine\nCrypto(35%) + Behavior(25%)\n+ Firmware(20%) + Net(20%)")
draw_component_card(ax, 53.5, 33, 20, 16, "Policy Decision Point (PDP)", "Attribute-Based Access Control\nPERMIT_FULL (T>=60)\nPERMIT_RESTRICTED (35<=T<60)")
draw_component_card(ax, 77.5, 33, 20, 16, "Autonomous Quarantine", "Threshold Breach (T < 35)\nRevoke MQTT Broker ACL\nSub-15ms Instant Isolation")

# 4. Storage & Blockchain Layer
draw_layer_box(ax, 3, 6, 45, 20, "LAYER 4A: IMMUTABLE LEDGER & IPFS", "Ethereum Smart Contracts & Storage", "#FDF4FF", "#A855F7")
draw_component_card(ax, 5, 8, 20, 13, "On-Chain Registries", "DIDRegistry.sol\nDeviceRegistry.sol\nAccessControl.sol")
draw_component_card(ax, 26, 8, 20, 13, "Tamper-Proof Audit & IPFS", "AuditLog.sol (SHA-256)\nIPFS Kubo Cluster (VCs)")

# 5. UI Application Layer
draw_layer_box(ax, 52, 6, 45, 20, "LAYER 4B: EXECUTIVE COMMAND CENTER", "React 18 / Vite / Tailwind Web App", "#FFFBEB", "#F59E0B")
draw_component_card(ax, 54, 8, 20, 13, "Web3 Authentication", "MetaMask SIWE (EIP-4361)\nRole-Based JWT Session")
draw_component_card(ax, 75, 8, 20, 13, "Live Security Mesh", "STOMP WebSocket Telemetry\nFleet Trust Gauge & Radar")

# Flow Arrows
arrow_style = dict(arrowstyle="->", lw=2, color="#475569")
accent_red = dict(arrowstyle="->", lw=2.2, color="#EF4444", linestyle="--")
accent_green = dict(arrowstyle="->", lw=2, color="#10B981")
accent_purple = dict(arrowstyle="<->", lw=1.8, color="#8B5CF6")

# Edge to MQTT
ax.annotate("", xy=(35, 71), xytext=(25, 76), arrowprops=arrow_style)
ax.annotate("", xy=(65, 71), xytext=(75, 76), arrowprops=arrow_style)
ax.text(50, 73.5, "Signed mTLS 1.3 Telemetry Packets", fontsize=8, fontweight='bold', color="#475569", ha="center")

# MQTT to Spring Ingestion
ax.annotate("", xy=(15.5, 50), xytext=(30, 59), arrowprops=arrow_style)
ax.text(20, 55, "Packet Queue", fontsize=8, color="#475569")

# Internal PDP Pipeline
ax.annotate("", xy=(29.5, 41), xytext=(25.5, 41), arrowprops=arrow_style)
ax.annotate("", xy=(53.5, 41), xytext=(49.5, 41), arrowprops=arrow_style)
ax.annotate("", xy=(77.5, 41), xytext=(73.5, 41), arrowprops=accent_red)
ax.text(75.5, 43, "T < 35", fontsize=8, fontweight='bold', color="#EF4444", ha="center")

# Quarantine feedback to MQTT
ax.annotate("", xy=(75, 61), xytext=(85, 50), arrowprops=accent_red)
ax.text(86, 56, "Revoke ACL", fontsize=8, fontweight='bold', color="#EF4444")

# Trust Engine to Blockchain & IPFS
ax.annotate("", xy=(25, 27), xytext=(35, 31), arrowprops=accent_purple)
ax.text(32, 28.5, "Verify DID / Log SHA-256", fontsize=7.5, color="#8B5CF6", ha="center")

# PDP to React Command Center
ax.annotate("", xy=(75, 27), xytext=(65, 31), arrowprops=accent_green)
ax.text(73, 28.5, "WebSocket STOMP (Sub-15ms)", fontsize=7.5, color="#10B981", ha="center")

plt.tight_layout()
arch_path = os.path.join(output_dir, "architecture_diagram.png")
plt.savefig(arch_path, dpi=300, bbox_inches='tight')
plt.close()
print(f"Generated: {arch_path}")
