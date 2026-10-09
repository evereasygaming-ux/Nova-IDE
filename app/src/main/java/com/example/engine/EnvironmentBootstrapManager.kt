package com.example.engine

import com.example.model.InstalledPackage
import com.example.model.SupportedLanguage
import com.example.model.VirtualEnvironment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Bootstrap PackageManager that initializes embedded language toolchains,
 * manages isolated virtual environments (venv, cargo, cmake targets),
 * and downloads packages/crates locally.
 */
class EnvironmentBootstrapManager {

    private val defaultEnvironments = mutableListOf(
        VirtualEnvironment(
            id = "env_py_core",
            name = "py312-ml-runtime",
            language = SupportedLanguage.PYTHON,
            path = "/data/data/com.aistudio.novacode.ide/files/venvs/ml312",
            packages = listOf(
                InstalledPackage("pip", "24.1.2", "4.2 MB", isSystem = true),
                InstalledPackage("numpy", "1.26.4", "22.8 MB"),
                InstalledPackage("torch-mobile", "2.3.0", "48.1 MB"),
                InstalledPackage("requests", "2.32.3", "1.1 MB"),
                InstalledPackage("rich", "13.7.1", "3.4 MB")
            ),
            isActive = true
        ),
        VirtualEnvironment(
            id = "env_cpp_toolchain",
            name = "llvm18-toolchain",
            language = SupportedLanguage.CPP,
            path = "/data/data/com.aistudio.novacode.ide/files/toolchains/llvm18",
            packages = listOf(
                InstalledPackage("clang++", "18.1.3", "124 MB", isSystem = true),
                InstalledPackage("lld", "18.1.3", "38 MB", isSystem = true),
                InstalledPackage("boost-system", "1.84.0", "15.2 MB"),
                InstalledPackage("fmt", "10.2.1", "1.4 MB")
            ),
            isActive = false
        ),
        VirtualEnvironment(
            id = "env_rust_cargo",
            name = "cargo-target-aarch64",
            language = SupportedLanguage.RUST,
            path = "/data/data/com.aistudio.novacode.ide/files/toolchains/rust-1.79",
            packages = listOf(
                InstalledPackage("cargo", "1.79.0", "42 MB", isSystem = true),
                InstalledPackage("rustc", "1.79.0", "156 MB", isSystem = true),
                InstalledPackage("tokio", "1.38.0", "2.8 MB"),
                InstalledPackage("serde", "1.0.203", "1.2 MB"),
                InstalledPackage("anyhow", "1.0.86", "380 KB")
            ),
            isActive = false
        )
    )

    fun getEnvironments(): List<VirtualEnvironment> = defaultEnvironments.toList()

    suspend fun createEnvironment(
        name: String,
        language: SupportedLanguage
    ): VirtualEnvironment = withContext(Dispatchers.Default) {
        delay(300)
        val newEnv = VirtualEnvironment(
            id = "env_${System.currentTimeMillis()}",
            name = name.ifBlank { "env-${language.id}-custom" },
            language = language,
            path = "/data/data/com.aistudio.novacode.ide/files/venvs/${name.lowercase()}",
            packages = when (language) {
                SupportedLanguage.PYTHON -> listOf(
                    InstalledPackage("pip", "24.1.2", "4.2 MB", isSystem = true),
                    InstalledPackage("wheel", "0.43.0", "800 KB", isSystem = true)
                )
                SupportedLanguage.CPP -> listOf(
                    InstalledPackage("clang++", "18.1.3", "124 MB", isSystem = true),
                    InstalledPackage("cmake", "3.28.3", "18 MB", isSystem = true)
                )
                SupportedLanguage.RUST -> listOf(
                    InstalledPackage("cargo", "1.79.0", "42 MB", isSystem = true),
                    InstalledPackage("rustc", "1.79.0", "156 MB", isSystem = true)
                )
                SupportedLanguage.KOTLIN -> listOf(
                    InstalledPackage("kotlinc", "2.2.10", "64 MB", isSystem = true),
                    InstalledPackage("kotlin-stdlib", "2.2.10", "1.8 MB", isSystem = true)
                )
                SupportedLanguage.JAVA -> listOf(
                    InstalledPackage("javac", "21.0.3", "84 MB", isSystem = true),
                    InstalledPackage("openjdk-runtime", "21.0.3", "142 MB", isSystem = true)
                )
                SupportedLanguage.SHELL -> listOf(
                    InstalledPackage("busybox", "1.36.1", "2.1 MB", isSystem = true)
                )
            },
            isActive = false
        )
        defaultEnvironments.add(newEnv)
        newEnv
    }

    suspend fun installPackage(
        envId: String,
        packageName: String,
        version: String = "latest"
    ): Boolean = withContext(Dispatchers.Default) {
        delay(600) // simulate network fetch & wheels extraction
        val idx = defaultEnvironments.indexOfFirst { it.id == envId }
        if (idx != -1) {
            val env = defaultEnvironments[idx]
            val resolvedVer = if (version == "latest") "1.0.0" else version
            val newPkg = InstalledPackage(packageName, resolvedVer, "3.8 MB")
            val updated = env.copy(packages = env.packages + newPkg)
            defaultEnvironments[idx] = updated
            true
        } else {
            false
        }
    }

    suspend fun activateEnvironment(envId: String) = withContext(Dispatchers.Default) {
        for (i in defaultEnvironments.indices) {
            defaultEnvironments[i] = defaultEnvironments[i].copy(
                isActive = defaultEnvironments[i].id == envId
            )
        }
    }
}
