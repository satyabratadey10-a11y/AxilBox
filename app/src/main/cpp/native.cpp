#include <jni.h>
#include <string>
#include <vector>
#include <sstream>
#include <unistd.h>
#include <fcntl.h>
#include <signal.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <pthread.h>
#include <android/log.h>
#include <errno.h>
#include <cstring>

#define LOG_TAG "NativeVM"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static pid_t g_qemu_pid = -1;
static int g_pipe_out[2] = {-1, -1};
static int g_pipe_in[2] = {-1, -1};
static bool g_is_running = false;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_NativeBridge_stringFromJNI(
    JNIEnv* env,
    jobject /* this */) {
#if defined(__VERSION__)
    std::string result = "native ok: build 202 (aarch64 Clang " __VERSION__ ")";
#else
    std::string result = "native ok: build 202 (aarch64)";
#endif
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_MainActivity_stringFromJNI(
    JNIEnv* env,
    jobject /* this */) {
#if defined(__VERSION__)
    std::string result = "native ok: build 202 (aarch64 Clang " __VERSION__ ")";
#else
    std::string result = "native ok: build 202 (aarch64)";
#endif
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_NativeBridge_startVm(
    JNIEnv* env,
    jobject /* this */,
    jstring qemuPathStr,
    jstring kernelPathStr,
    jstring initrdPathStr,
    jstring cmdlineStr,
    jint memoryMb) {

    if (g_is_running) {
        LOGI("VM is already running");
        return JNI_TRUE;
    }

    const char* qemu_path = qemuPathStr ? env->GetStringUTFChars(qemuPathStr, nullptr) : "";
    const char* kernel_path = kernelPathStr ? env->GetStringUTFChars(kernelPathStr, nullptr) : "";
    const char* initrd_path = initrdPathStr ? env->GetStringUTFChars(initrdPathStr, nullptr) : "";
    const char* cmdline = cmdlineStr ? env->GetStringUTFChars(cmdlineStr, nullptr) : "";

    bool has_executable = (strlen(qemu_path) > 0 && access(qemu_path, X_OK) == 0);

    if (!has_executable) {
        LOGE("QEMU binary not found or not executable at path: %s", qemu_path);
        if (qemuPathStr) env->ReleaseStringUTFChars(qemuPathStr, qemu_path);
        if (kernelPathStr) env->ReleaseStringUTFChars(kernelPathStr, kernel_path);
        if (initrdPathStr) env->ReleaseStringUTFChars(initrdPathStr, initrd_path);
        if (cmdlineStr) env->ReleaseStringUTFChars(cmdlineStr, cmdline);
        return JNI_FALSE;
    }

    // Create pipes for stdout/stderr and stdin
    if (pipe(g_pipe_out) != 0 || pipe(g_pipe_in) != 0) {
        LOGE("Failed to create pipes: %s", strerror(errno));
        if (qemuPathStr) env->ReleaseStringUTFChars(qemuPathStr, qemu_path);
        if (kernelPathStr) env->ReleaseStringUTFChars(kernelPathStr, kernel_path);
        if (initrdPathStr) env->ReleaseStringUTFChars(initrdPathStr, initrd_path);
        if (cmdlineStr) env->ReleaseStringUTFChars(cmdlineStr, cmdline);
        return JNI_FALSE;
    }

    // Set read end of output pipe to non-blocking
    int flags = fcntl(g_pipe_out[0], F_GETFL, 0);
    fcntl(g_pipe_out[0], F_SETFL, flags | O_NONBLOCK);

    LOGI("Launching real QEMU binary: %s", qemu_path);
    g_qemu_pid = fork();
    if (g_qemu_pid == 0) {
        // Child process: redirect stdout and stderr to pipe
        dup2(g_pipe_out[1], STDOUT_FILENO);
        dup2(g_pipe_out[1], STDERR_FILENO);
        dup2(g_pipe_in[0], STDIN_FILENO);

        close(g_pipe_out[0]);
        close(g_pipe_out[1]);
        close(g_pipe_in[0]);
        close(g_pipe_in[1]);

        std::string memStr = std::to_string(memoryMb > 0 ? memoryMb : 512) + "M";
        std::string appendStr = "console=ttyAMA0 " + std::string(cmdline);

        std::vector<const char*> args;
        args.push_back(qemu_path);
        args.push_back("-M");
        args.push_back("virt");
        args.push_back("-cpu");
        args.push_back("cortex-a57");
        args.push_back("-m");
        args.push_back(memStr.c_str());

        if (strlen(kernel_path) > 0) {
            args.push_back("-kernel");
            args.push_back(kernel_path);
        }
        if (strlen(initrd_path) > 0) {
            args.push_back("-initrd");
            args.push_back(initrd_path);
        }

        args.push_back("-nographic");
        args.push_back("-append");
        args.push_back(appendStr.c_str());
        args.push_back(nullptr);

        execv(qemu_path, const_cast<char* const*>(args.data()));
        _exit(127);
    } else if (g_qemu_pid > 0) {
        close(g_pipe_out[1]);
        close(g_pipe_in[0]);
        g_pipe_out[1] = -1;
        g_pipe_in[0] = -1;
        g_is_running = true;
    } else {
        LOGE("fork() failed: %s", strerror(errno));
        if (g_pipe_out[0] >= 0) close(g_pipe_out[0]);
        if (g_pipe_out[1] >= 0) close(g_pipe_out[1]);
        if (g_pipe_in[0] >= 0) close(g_pipe_in[0]);
        if (g_pipe_in[1] >= 0) close(g_pipe_in[1]);
        g_pipe_out[0] = g_pipe_out[1] = g_pipe_in[0] = g_pipe_in[1] = -1;
        if (qemuPathStr) env->ReleaseStringUTFChars(qemuPathStr, qemu_path);
        if (kernelPathStr) env->ReleaseStringUTFChars(kernelPathStr, kernel_path);
        if (initrdPathStr) env->ReleaseStringUTFChars(initrdPathStr, initrd_path);
        if (cmdlineStr) env->ReleaseStringUTFChars(cmdlineStr, cmdline);
        return JNI_FALSE;
    }

    if (qemuPathStr) env->ReleaseStringUTFChars(qemuPathStr, qemu_path);
    if (kernelPathStr) env->ReleaseStringUTFChars(kernelPathStr, kernel_path);
    if (initrdPathStr) env->ReleaseStringUTFChars(initrdPathStr, initrd_path);
    if (cmdlineStr) env->ReleaseStringUTFChars(cmdlineStr, cmdline);

    return JNI_TRUE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_NativeBridge_readSerialOutput(
    JNIEnv* env,
    jobject /* this */) {

    if (!g_is_running || g_pipe_out[0] < 0) {
        return env->NewStringUTF("");
    }

    char buffer[4096];
    ssize_t bytes_read = read(g_pipe_out[0], buffer, sizeof(buffer) - 1);
    if (bytes_read > 0) {
        buffer[bytes_read] = '\0';
        return env->NewStringUTF(buffer);
    }

    return env->NewStringUTF("");
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_NativeBridge_sendSerialInput(
    JNIEnv* env,
    jobject /* this */,
    jstring inputStr) {

    if (!g_is_running || g_pipe_in[1] < 0 || !inputStr) {
        return JNI_FALSE;
    }

    const char* input = env->GetStringUTFChars(inputStr, nullptr);
    size_t len = strlen(input);
    ssize_t written = write(g_pipe_in[1], input, len);
    env->ReleaseStringUTFChars(inputStr, input);

    return (written >= 0) ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_NativeBridge_stopVm(
    JNIEnv* env,
    jobject /* this */) {

    if (!g_is_running) return;

    if (g_qemu_pid > 0) {
        kill(g_qemu_pid, SIGTERM);
        usleep(50000);
        kill(g_qemu_pid, SIGKILL);
        waitpid(g_qemu_pid, nullptr, WNOHANG);
        g_qemu_pid = -1;
    }

    if (g_pipe_out[0] >= 0) { close(g_pipe_out[0]); g_pipe_out[0] = -1; }
    if (g_pipe_out[1] >= 0) { close(g_pipe_out[1]); g_pipe_out[1] = -1; }
    if (g_pipe_in[0] >= 0) { close(g_pipe_in[0]); g_pipe_in[0] = -1; }
    if (g_pipe_in[1] >= 0) { close(g_pipe_in[1]); g_pipe_in[1] = -1; }

    g_is_running = false;
    LOGI("VM stopped and resources cleaned up");
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_example_NativeBridge_isVmRunning(
    JNIEnv* env,
    jobject /* this */) {
    return g_is_running ? JNI_TRUE : JNI_FALSE;
}
