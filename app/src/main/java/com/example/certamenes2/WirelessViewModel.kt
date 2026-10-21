package com.example.certamenes2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

/**
 * Rol de la aplicación en la arquitectura de Sockets TCP sobre Wi-Fi.
 */
enum class NetworkRole {
    Host,   // Servidor TCP Socket (ServerSocket)
    Cliente // Cliente TCP Socket (Socket)
}

/**
 * Estados de la conexión del Socket TCP.
 */
enum class ConnectionState {
    Desconectado,
    Conectando,
    Enlazado
}

/**
 * ViewModel que gestiona la comunicación mediante Sockets TCP sobre Wi-Fi local.
 * REQUISITO ESTRICTO: Toda la lógica de conexión, lectura y escritura
 * se ejecuta fuera del hilo UI usando viewModelScope.launch(Dispatchers.IO).
 */
class WirelessViewModel : ViewModel() {

    private val _connectionState = MutableStateFlow(ConnectionState.Desconectado)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _selectedRole = MutableStateFlow(NetworkRole.Host)
    val selectedRole: StateFlow<NetworkRole> = _selectedRole.asStateFlow()

    private val _hostIpInput = MutableStateFlow("192.168.1.100")
    val hostIpInput: StateFlow<String> = _hostIpInput.asStateFlow()

    private val _portInput = MutableStateFlow("8080")
    val portInput: StateFlow<String> = _portInput.asStateFlow()

    private val _receivedData = MutableStateFlow("")
    val receivedData: StateFlow<String> = _receivedData.asStateFlow()

    private val _logMessages = MutableStateFlow<List<String>>(emptyList())
    val logMessages: StateFlow<List<String>> = _logMessages.asStateFlow()

    // Sockets TCP de Java
    private var serverSocket: ServerSocket? = null
    private var socket: Socket? = null

    private var connectionJob: Job? = null
    private var ioJob: Job? = null

    fun setRole(role: NetworkRole) {
        if (_connectionState.value == ConnectionState.Desconectado) {
            _selectedRole.value = role
        }
    }

    fun setHostIpInput(ip: String) {
        if (_connectionState.value == ConnectionState.Desconectado) {
            _hostIpInput.value = ip
        }
    }

    fun setPortInput(port: String) {
        if (_connectionState.value == ConnectionState.Desconectado) {
            _portInput.value = port
        }
    }

    /**
     * Inicia la conexión mediante Socket TCP según el rol seleccionado.
     * REQUISITO ESTRICTO: Ejecutado en Dispatchers.IO.
     */
    fun conectar() {
        cerrarConexion()

        val role = _selectedRole.value
        val port = _portInput.value.toIntOrNull() ?: 8080
        val targetIp = _hostIpInput.value.trim()

        _connectionState.value = ConnectionState.Conectando
        addLog("Iniciando Socket TCP en modo ${role.name}...")

        connectionJob = viewModelScope.launch(Dispatchers.IO) {
            if (role == NetworkRole.Host) {
                ejecutarHostTcp(port)
            } else {
                ejecutarClienteTcp(targetIp, port)
            }
        }
    }

    /**
     * Lógica Servidor Host TCP (ServerSocket).
     * Se bloquea en accept() en Dispatchers.IO y lee líneas con BufferedReader.
     */
    private fun ejecutarHostTcp(port: Int) {
        try {
            addLog("Host: Creando ServerSocket en puerto $port...")
            serverSocket = ServerSocket(port)
            addLog("Host: Esperando conexión entrante (accept) en puerto $port...")

            val clientSocket = serverSocket!!.accept() // Bloqueante en Dispatchers.IO
            socket = clientSocket

            _connectionState.value = ConnectionState.Enlazado
            addLog("¡Cliente TCP Conectado desde ${clientSocket.inetAddress.hostAddress}:${clientSocket.port}!")

            iniciarLecturaHostTcp(clientSocket)

        } catch (e: Exception) {
            if (_connectionState.value == ConnectionState.Conectando) {
                _connectionState.value = ConnectionState.Desconectado
                addLog("Error Servidor TCP (${e.javaClass.simpleName}): ${e.localizedMessage}")
            }
        }
    }

    /**
     * Lógica Cliente TCP (Socket).
     * Conecta a ip_host:puerto estrictamente dentro de try-catch.
     * Si el socket falla al conectar (ej. ConnectException por falta de Host),
     * registra el error en la consola, pasa a estado 'Desconectado' y aborta.
     */
    private fun ejecutarClienteTcp(hostIp: String, port: Int) {
        var tcpSocket: Socket? = null
        try {
            addLog("Cliente: Intentando conectar Socket TCP a $hostIp:$port...")

            // Intento de conexión socket real con timeout de 5 segundos
            tcpSocket = Socket()
            tcpSocket.connect(InetSocketAddress(hostIp, port), 5000)

            // Conexión exitosa confirmada
            socket = tcpSocket
            _connectionState.value = ConnectionState.Enlazado
            addLog("¡Conectado exitosamente al Host TCP en $hostIp:$port!")

            // ÚNICAMENTE si la conexión real fue exitosa, iniciamos el bucle de transmisión
            iniciarTransmisionClienteTcp(tcpSocket)

        } catch (e: Exception) {
            // Manejo estricto de fallo de conexión (ConnectException, SocketTimeoutException, etc.)
            try {
                tcpSocket?.close()
            } catch (ignored: Exception) {}
            socket = null
            _connectionState.value = ConnectionState.Desconectado

            val errorMsg = e.localizedMessage ?: e.message ?: "Conexión rechazada o Host no encontrado"
            addLog("Fallo al conectar Cliente TCP (${e.javaClass.simpleName}): $errorMsg")
        }
    }

    /**
     * Bucle de lectura del Host con BufferedReader (Dispatchers.IO).
     */
    private fun iniciarLecturaHostTcp(clientSocket: Socket) {
        ioJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                var line: String?
                while (_connectionState.value == ConnectionState.Enlazado && !clientSocket.isClosed) {
                    line = reader.readLine()
                    if (line == null) break // Fin del stream por cierre de conexión

                    val incomingData = line
                    _receivedData.value = incomingData
                    addLog("Host Recibido <- $incomingData")
                }
            } catch (e: Exception) {
                if (_connectionState.value == ConnectionState.Enlazado) {
                    addLog("Interrupción de lectura Host: ${e.localizedMessage}")
                }
            } finally {
                _connectionState.value = ConnectionState.Desconectado
                addLog("Conexión finalizada con el cliente TCP.")
            }
        }
    }

    /**
     * Bucle de envío continuo del Cliente con PrintWriter (Dispatchers.IO).
     * SOLO se ejecuta si la conexión real del socket fue exitosa y continúa activa.
     */
    private fun iniciarTransmisionClienteTcp(tcpSocket: Socket) {
        ioJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val writer = PrintWriter(
                    BufferedWriter(OutputStreamWriter(tcpSocket.getOutputStream())),
                    true
                )
                var counter = 1
                while (_connectionState.value == ConnectionState.Enlazado && !tcpSocket.isClosed && tcpSocket.isConnected) {
                    delay(2000)
                    val telemetryPayload = "TEMP: ${(20..32).random()}°C | HUM: ${(45..68).random()}% [Sensor Payload #$counter]"

                    writer.println(telemetryPayload) // Envío real mediante PrintWriter sobre el socket TCP

                    if (writer.checkError()) {
                        throw java.io.IOException("Error de I/O en Socket TCP (Conexión cerrada o rechazada por el Host).")
                    }

                    _receivedData.value = "Transmitido -> $telemetryPayload"
                    addLog("Cliente Transmitiendo -> $telemetryPayload")
                    counter++
                }
            } catch (e: Exception) {
                if (_connectionState.value == ConnectionState.Enlazado) {
                    addLog("Error en transmisión Cliente TCP: ${e.localizedMessage}")
                }
            } finally {
                _connectionState.value = ConnectionState.Desconectado
                try {
                    tcpSocket.close()
                } catch (ignored: Exception) {}
                socket = null
                addLog("Conexión del Cliente TCP finalizada.")
            }
        }
    }

    /**
     * Envío de comandos manuales desde la UI usando PrintWriter (Dispatchers.IO).
     */
    fun enviarDatos(mensaje: String) {
        val currentSocket = socket
        if (_connectionState.value != ConnectionState.Enlazado || currentSocket == null || currentSocket.isClosed) {
            addLog("No se puede enviar: Socket no conectado")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                addLog("Enviando comando manual: '$mensaje'...")
                val writer = PrintWriter(
                    BufferedWriter(OutputStreamWriter(currentSocket.getOutputStream())),
                    true
                )
                writer.println(mensaje)
                if (writer.checkError()) {
                    throw java.io.IOException("Error de I/O al enviar el comando.")
                }
                addLog("Comando '$mensaje' enviado con éxito.")
            } catch (e: Exception) {
                addLog("Error al enviar comando: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Cierra de forma segura ServerSocket y Socket liberando recursos (Dispatchers.IO).
     */
    fun cerrarConexion() {
        connectionJob?.cancel()
        ioJob?.cancel()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                socket?.close()
                serverSocket?.close()
            } catch (e: Exception) {
                // Captura silenciosa de errores al cerrar sockets
            } finally {
                socket = null
                serverSocket = null
            }
        }

        if (_connectionState.value != ConnectionState.Desconectado) {
            _connectionState.value = ConnectionState.Desconectado
            addLog("Conexión Socket TCP cerrada y puertos liberados.")
        }
    }

    private fun addLog(message: String) {
        val currentLogs = _logMessages.value.toMutableList()
        currentLogs.add(0, message)
        _logMessages.value = currentLogs.take(20)
    }

    override fun onCleared() {
        super.onCleared()
        cerrarConexion()
    }
}
