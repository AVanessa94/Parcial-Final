# 📚 SISTEMA DE GESTIÓN DE BIBLIOTECA

## **DESCRIPCIÓN**
Sistema desarrollado en **Java** para la gestión integral de una biblioteca, aplicando principios de **Programación Orientada a Objetos (POO)**, uso de **tipos primitivos y wrappers**, y manejo de **excepciones personalizadas**. Permite registrar libros, usuarios y préstamos, con control de disponibilidad, multas y reportes automáticos.

## **CARACTERÍSTICAS PRINCIPALES**

📘 **Gestión de Libros**
- Registro de libros con ISBN, título, autor, año y ejemplares disponibles.
- Validación del ISBN (13 dígitos).
- Control de ejemplares totales y disponibles.
- Búsqueda por ISBN o título.
- Lanza la excepción `LibroNoDisponibleException` cuando no hay copias disponibles.

👥 **Gestión de Usuarios**
- Registro con ID autogenerado y validación de correo electrónico.
- Control de préstamos activos (máximo 3 por usuario).
- Control de multas (máx. $5000 acumulados).
- Lanza `UsuarioSinCupoException` si el usuario supera los límites.

🔁 **Sistema de Préstamos**
- Registro de préstamos con fecha de inicio y devolución.
- Periodo de préstamo estándar: 14 días.
- Cálculo automático de multas ($500 por día de retraso).
- Control de estados mediante enum `EstadoPrestamo` (ACTIVO, DEVUELTO, VENCIDO).

📊 **Reportes y Consultas**
- Listado de libros más prestados.
- Reporte de usuarios con multas pendientes.
- Consulta de libros disponibles.
- Historial de préstamos por usuario.

## **🧩 ESTRUCTURA DEL PROYECTO**

BibliotecaApp/
├── BibliotecaApp.java
├── modelos/
│   ├── Libro.java
│   ├── Usuario.java
│   ├── Prestamo.java
│   ├── EstadoPrestamo.java
├── excepciones/
│   ├── LibroNoDisponibleException.java
│   ├── UsuarioSinCupoException.java
│   ├── EmailInvalidoException.java
│   ├── PrestamoInvalidoException.java
├── servicios/
│   ├── BibliotecaService.java
│   ├── PrestamoService.java
│   └── ReporteService.java

## **🚨 MANEJO DE ERRORES**

SaldoInsuficienteException → No hay ejemplares disponibles para préstamo.
UsuarioSinCupoException → El usuario ya alcanzó el límite de libros o multas.
EmailInvalidoException → El formato del correo electrónico no es válido.
PrestamoInvalidoException → Datos del préstamo incorrectos o préstamo duplicado.

## **🧠 PRINCIPIOS POO APLICADOS**
- Encapsulación: Atributos privados y métodos públicos.
- Abstracción: Clases y métodos que representan conceptos del dominio.
- Responsabilidad Única: Cada clase cumple una función específica.
- Manejo de Excepciones: Control de errores mediante clases personalizadas.
- Abierto/Cerrado: Fácil de extender sin modificar el código base.

## **⚙️ TECNOLOGÍAS UTILIZADAS**
- Lenguaje: Java
- Paradigma: Programación Orientada a Objetos
- Colecciones: HashMap, ArrayList, Optional, Streams
- Tipos de Datos: Primitivos vs Clases Wrapper
- Concurrencia: synchronized, AtomicInteger
- Formato Monetario: BigDecimal

## **💽 REQUISITOS DEL SISTEMA**
- Java JDK: versión 8 o superior
- RAM mínima: 512 MB
- Espacio en disco: 100 MB
- Sistema operativo: Windows, Linux o macOS

## **▶️ INSTALACIÓN Y EJECUCIÓN**
1️⃣ Compilar el programa:
javac BibliotecaApp.java
2️⃣ Ejecutar el sistema:
java BibliotecaApp

## **💻 FUNCIONALIDADES DISPONIBLES**
- Registrar y listar usuarios
- Agregar nuevos libros
- Realizar y devolver préstamos
- Control de disponibilidad
- Cálculo automático de multas
- Reporte de usuarios con sanciones
- Listar libros más prestados

## **📖 DATOS DE EJEMPLO INICIALES**
Libros disponibles:
- ISBN: 9788437604947 – Cien años de soledad – Gabriel García Márquez
- ISBN: 9788408268521 – El Quijote – Miguel de Cervantes
- ISBN: 9788497593798 – 1984 – George Orwell
- ISBN: 9788466338141 – Harry Potter y la piedra filosofal – J.K. Rowling

Usuarios registrados:
- Ana García – ana@email.com
- Carlos López – carlos@email.com

## **🧭 MENÚ PRINCIPAL**
1️⃣ Agregar libro
2️⃣ Registrar usuario
3️⃣ Realizar préstamo
4️⃣ Devolver libro
5️⃣ Consultar libros disponibles
6️⃣ Consultar préstamos de usuario
7️⃣ Listar usuarios con multas
8️⃣ Top 5 libros más prestados
9️⃣ Salir
