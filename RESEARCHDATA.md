# RESEARCH & SYSTEM DATA: Android Native VM Integration

## 1. Boot Subsystem Requirements

| Component | Identifier | Role | Requirement for Custom OS |
|-----------|-----------|------|---------------------------|
| Kernel Image | `kernelUri` | aarch64 Linux Kernel (`vmlinuz` / `Image`) | Mandatory for Custom OS |
| Root Filesystem | `osImageUri` | Userspace initrd/rootfs (`.tar.gz`, `.img`, `.iso`) | Mandatory for Custom OS |
| Hypervisor / Emulator | `libqemu-system-aarch64.so` | CPU emulation (`-M virt -cpu cortex-a57`) | Required for execution |
| Serial Terminal | `ttyAMA0` / `pl011` | Non-blocking stdout/stdin pipe (115200 8N1) | Streamed to UI |

## 2. Boot Mode Matrix

- **Dual-File User Selection**: User selects both `kernelUri` and `osImageUri` -> BootScreen resolves local cached paths -> passes custom `-kernel` and `-initrd` to native JNI engine.
- **Partial User Selection**: User selects only one file -> BootScreen alerts: *"Custom OS configuration incomplete: missing Kernel/RootFS. Both kernel and rootfs are required for custom OS."*
- **Default Quick Start**: User leaves both blank -> BootScreen downloads verified official Alpine Linux aarch64 minirootfs and pairs with bundled Alpine default kernel.

## 3. Strict Absence of Simulated Telemetry
In compliance with project integrity standards:
- Simulated boot loops, hardcoded `dmesg` replay arrays, and synthetic shell prompts (`simulate_alpine_boot`) have been deleted.
- If QEMU binary is absent, native startup fails gracefully and reports *"QEMU engine not available — native VM launch not yet implemented in this build"*.
