# DESIGN SPECIFICATION: Native Virtual Machine Manager (Android aarch64)

## Architecture Overview

The VM Manager is built to manage virtual machine instances on Android using native JNI interfaces and Material 3 design patterns.

### 1. Two-File Boot Model (Kernel + RootFS)
Virtual machines targeting aarch64 virt architecture require two decoupled file references for direct kernel execution (`-kernel` + `-initrd`):
- **RootFS / Initrd Image (`osImageUri`)**: Contains the guest root filesystem, userland binaries, `/init`, package manager, and shell tools (e.g. Alpine Linux minirootfs archive `.tar.gz`, `.cpio`, or disk image).
- **Kernel Image (`kernelUri`)**: Contains the compiled Linux uncompressed/compressed kernel image (`Image` / `vmlinuz`) supporting `virt` machine type, `cortex-a57` or host vCPU, and `pl011` serial console drivers.

### 2. Custom OS vs. Quick-Start Default
- **Custom OS Mode**: When an instance is configured with both a user-selected `osImageUri` and `kernelUri`, the Boot sequence copies both artifacts to sandboxed cache and invokes the native JNI engine with `-kernel <custom_kernel>` and `-initrd <custom_rootfs>`. If only one is provided, the UI explicitly warns that both files are required for custom OS boots.
- **Quick-Start Default (Alpine Linux)**: When neither `kernelUri` nor `osImageUri` are provided, the app uses a bundled default configuration tailored specifically for Alpine Linux aarch64. The UI clearly states that the default matching kernel is provided exclusively for Alpine Linux and does not generalize to arbitrary user OS images.

### 3. Native JNI & Serial Pipeline Policy
- Native code in `native.cpp` directly invokes the real QEMU aarch64 binary (`libqemu-system-aarch64.so`) via `fork()`, `dup2()`, and `execv()`.
- **Honesty Rule**: If no executable QEMU binary is present on the filesystem, the native code immediately returns `JNI_FALSE` without fabricating kernel messages, boot logs, or simulated login prompts. The Boot screen displays an honest status message:
  > *"QEMU engine not available — native VM launch not yet implemented in this build"*
