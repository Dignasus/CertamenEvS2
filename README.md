\# Centro de Control IoT — Certamen 2



Aplicación móvil Android desarrollada con Kotlin y Jetpack Compose para establecer comunicaciones entre dispositivos mediante sockets TCP a través de una red Wi-Fi local.



El proyecto permite configurar los modos Host y Cliente, establecer conexiones e intercambiar datos entre dispositivos. También incorpora un sistema de inicio de sesión y mecanismos de almacenamiento protegido de credenciales, considerando principios de ciberseguridad aplicables al Internet de las Cosas (IoT).



\## Funcionalidades



\- \*\*Inicio de sesión:\*\* acceso a las funcionalidades de la aplicación mediante credenciales.

\- \*\*Modo Host:\*\* permite iniciar un servidor TCP mediante `ServerSocket` para recibir conexiones entrantes.

\- \*\*Modo Cliente:\*\* permite conectarse a un Host utilizando su dirección IP y puerto.

\- \*\*Comunicación TCP:\*\* intercambio de mensajes y datos entre dispositivos conectados a una red que permita la comunicación entre ellos.

\- \*\*Configuración de conexión:\*\* gestión de los parámetros necesarios para establecer la comunicación entre dispositivos.

\- \*\*Gestión de conexiones:\*\* control del estado de conexión y manejo de errores de red.

\- \*\*Permisos de Android:\*\* gestión de los permisos requeridos por las funciones de conectividad y ubicación implementadas.

\- \*\*Almacenamiento protegido:\*\* utilización de herramientas de seguridad de Android para proteger las credenciales almacenadas localmente.



\## Tecnologías utilizadas



| Tecnología | Descripción |

|---|---|

| Kotlin | Lenguaje de programación |

| Android Studio | Entorno de desarrollo |

| Jetpack Compose | Construcción de la interfaz de usuario |

| Material 3 | Componentes visuales |

| Kotlin Coroutines | Ejecución de tareas asíncronas |

| `Dispatchers.IO` | Ejecución de operaciones de entrada y salida |

| `ServerSocket` y `Socket` | Comunicación mediante TCP |

| `EncryptedSharedPreferences` | Almacenamiento cifrado de preferencias |

| Android Keystore | Gestión protegida de claves criptográficas |



\## Arquitectura y funcionamiento



\### Patrón MVVM



La aplicación utiliza el patrón arquitectónico \*\*MVVM (Model-View-ViewModel)\*\* para separar la interfaz de usuario de la lógica de la aplicación.



El componente `WirelessViewModel` gestiona el estado y la lógica relacionada con las funcionalidades de comunicación, favoreciendo la organización y el mantenimiento del código.



\### Comunicación mediante sockets TCP



La aplicación contempla dos modos de funcionamiento:



\- \*\*Host (servidor):\*\* utiliza `ServerSocket` para escuchar conexiones entrantes mediante el método `accept()`.

\- \*\*Cliente:\*\* utiliza `Socket` para establecer una conexión con el servidor mediante una dirección IP y un puerto determinados.



Una vez establecida la conexión, los dispositivos pueden intercambiar información mediante los flujos de entrada y salida correspondientes.



\### Operaciones asíncronas



Las operaciones de red que pueden bloquear la ejecución se realizan fuera del hilo principal mediante corrutinas de Kotlin y `Dispatchers.IO`, según la implementación de la aplicación.



Esto permite mantener la interfaz de usuario receptiva mientras se realizan tareas como establecer conexiones, esperar conexiones entrantes y leer o enviar datos.



\## Seguridad IoT y referencia a la norma ISO/IEC 27400



La norma \*\*ISO/IEC 27400\*\* proporciona directrices relacionadas con la ciberseguridad y la privacidad en los sistemas de Internet de las Cosas (IoT).



En este proyecto, se utiliza como referencia conceptual para considerar riesgos asociados al control de acceso, la protección de información sensible y la comunicación entre dispositivos.



Las medidas de seguridad consideradas incluyen:



\- \*\*Protección de datos almacenados:\*\* utilización de almacenamiento cifrado para proteger las credenciales guardadas localmente en el dispositivo.

\- \*\*Control de acceso:\*\* incorporación de una pantalla de inicio de sesión para acceder a las funcionalidades de la aplicación.

\- \*\*Protección visual de contraseñas:\*\* uso de `PasswordVisualTransformation` para ocultar los caracteres ingresados en los campos de contraseña.

\- \*\*Protección durante la depuración:\*\* evitar la exposición de credenciales y otros datos sensibles en los registros de Logcat.



\### Alcance y limitaciones de seguridad



El proyecto tiene fines académicos y busca aplicar conceptos de desarrollo Android, comunicación de red y protección de información en un escenario IoT simulado.



La implementación de estas medidas no implica el cumplimiento integral ni la certificación bajo la norma ISO/IEC 27400.



Asimismo, TCP no cifra por sí mismo los datos transmitidos. Si la comunicación no utiliza TLS u otro mecanismo de cifrado equivalente, la información intercambiada puede quedar expuesta a quienes tengan capacidad de observar el tráfico de red.



Por ello, la aplicación está orientada a un entorno de pruebas controlado y requeriría medidas adicionales de seguridad antes de utilizarse en un entorno productivo.



\## Capturas de pantalla



\### Inicio de sesión



Pantalla de acceso a la aplicación mediante credenciales.



!\[Inicio de sesión](screenshots/login.png)



\### Configuración de red



Pantallas relacionadas con la configuración de los modos de comunicación.



| Configuración del Cliente | Configuración del Host |

|---|---|

| !\[Configuración del Cliente](screenshots/configuracion\_cliente.png) | !\[Configuración del Host](screenshots/configuracion\_host.png) |



\### Modos de comunicación



Vistas correspondientes al funcionamiento de la aplicación como cliente TCP o como servidor.



| Modo Cliente | Modo Host |

|---|---|

| !\[Modo Cliente](screenshots/modo\_cliente.png) | !\[Modo Host](screenshots/modo\_host.png) |



\### Permisos de Android



Pantallas relacionadas con los permisos solicitados por la aplicación.



| Permisos de conexión | Permisos de ubicación |

|---|---|

| !\[Permisos de conexión](screenshots/permisos\_conexion.png) | !\[Permisos de ubicación](screenshots/permisos\_ubicacion.png) |



\## Instalación y ejecución



\### Requisitos



\- Android Studio compatible con la configuración del proyecto.

\- Android SDK 34 como versión de compilación.

\- Kotlin 2.0.21.

\- Dispositivo Android compatible o emulador.



\### Ejecutar desde el código fuente



1\. Clonar o descargar el repositorio.

2\. Abrir el proyecto en Android Studio.

3\. Sincronizar las dependencias de Gradle.

4\. Esperar a que finalice la sincronización.

5\. Ejecutar la aplicación en un dispositivo físico o emulador.



Para probar la comunicación entre dos dispositivos, ambos deben estar conectados a una red que permita la comunicación entre ellos. También se debe configurar la dirección IP y el puerto correspondientes al dispositivo que actúa como Host.



\### Instalación mediante APK



Si el archivo compilado está incluido en el repositorio, puedes encontrarlo en la siguiente ruta:



`apk/app-debug.apk`



Este archivo permite instalar la versión de depuración de la aplicación sin necesidad de compilar el proyecto desde el código fuente.



\## Objetivo del proyecto



Aplicar conocimientos de programación móvil Android, diseño de interfaces, arquitectura de software, ejecución de tareas asíncronas y comunicación mediante sockets TCP, considerando principios de ciberseguridad relevantes para un escenario IoT.



\## Contexto académico



Proyecto desarrollado como parte del Certamen 2, con fines educativos y de aplicación práctica de los contenidos relacionados con el desarrollo de aplicaciones móviles, la comunicación entre dispositivos y la protección de información local.

