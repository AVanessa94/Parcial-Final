import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

// Excepciones personalizadas
class LibroNoDisponibleException extends RuntimeException {
    public LibroNoDisponibleException(String mensaje) { super(mensaje); }
}

class UsuarioSinCupoException extends RuntimeException {
    public UsuarioSinCupoException(String mensaje) { super(mensaje); }
}

class ISBNInvalidoException extends RuntimeException {
    public ISBNInvalidoException(String mensaje) { super(mensaje); }
}

class EmailInvalidoException extends RuntimeException {
    public EmailInvalidoException(String mensaje) { super(mensaje); }
}

// Enums
enum EstadoPrestamo {
    ACTIVO, DEVUELTO, VENCIDO
}

// Clase Libro
class Libro {
    private String isbn;
    private String titulo;
    private String autor;
    private int año;
    private int ejemplaresTotales;
    private int ejemplaresDisponibles;
    private AtomicInteger contadorPrestamos;

    public Libro(String isbn, String titulo, String autor, int año, int ejemplaresTotales) {
        validarISBN(isbn);
        validarAño(año);
        
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.año = año;
        this.ejemplaresTotales = ejemplaresTotales;
        this.ejemplaresDisponibles = ejemplaresTotales;
        this.contadorPrestamos = new AtomicInteger(0);
    }

    private void validarISBN(String isbn) {
        if (isbn == null || !isbn.matches("\\d{13}")) {
            throw new ISBNInvalidoException("ISBN debe tener exactamente 13 dígitos");
        }
    }

    private void validarAño(int año) {
        int añoActual = LocalDate.now().getYear();
        if (año < 1000 || año > añoActual) {
            throw new IllegalArgumentException("Año inválido: " + año);
        }
    }

    public synchronized void prestar() {
        if (!estaDisponible()) {
            throw new LibroNoDisponibleException("No hay ejemplares disponibles de: " + titulo);
        }
        ejemplaresDisponibles--;
        contadorPrestamos.incrementAndGet();
    }

    public synchronized void devolver() {
        if (ejemplaresDisponibles >= ejemplaresTotales) {
            throw new IllegalStateException("No se puede devolver más ejemplares de los prestados");
        }
        ejemplaresDisponibles++;
    }

    public boolean estaDisponible() {
        return ejemplaresDisponibles > 0;
    }

    // Getters
    public String getIsbn() { return isbn; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public int getAño() { return año; }
    public int getEjemplaresDisponibles() { return ejemplaresDisponibles; }
    public int getEjemplaresTotales() { return ejemplaresTotales; }
    public int getContadorPrestamos() { return contadorPrestamos.get(); }

    @Override
    public String toString() {
        return String.format("ISBN: %s | Título: %s | Autor: %s | Disponibles: %d/%d | Préstamos: %d",
                isbn, titulo, autor, ejemplaresDisponibles, ejemplaresTotales, contadorPrestamos.get());
    }
}

// Clase Usuario
class Usuario {
    private static final AtomicInteger CONTADOR_ID = new AtomicInteger(1);
    
    private final int id;
    private String nombre;
    private String email;
    private List<String> librosPrestados;
    private BigDecimal multasAcumuladas;
    private static final BigDecimal MULTA_MAXIMA = new BigDecimal("5000");
    private static final int MAX_LIBROS = 3;

    public Usuario(String nombre, String email) {
        validarEmail(email);
        
        this.id = CONTADOR_ID.getAndIncrement();
        this.nombre = nombre;
        this.email = email;
        this.librosPrestados = new ArrayList<>();
        this.multasAcumuladas = BigDecimal.ZERO;
    }

    private void validarEmail(String email) {
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new EmailInvalidoException("Email inválido: " + email);
        }
    }

    public boolean puedePedirPrestado() {
        return librosPrestados.size() < MAX_LIBROS && multasAcumuladas.compareTo(MULTA_MAXIMA) < 0;
    }

    public void agregarLibroPrestado(String isbn) {
        if (!puedePedirPrestado()) {
            throw new UsuarioSinCupoException("Usuario no puede pedir más libros. Límite: " + MAX_LIBROS);
        }
        librosPrestados.add(isbn);
    }

    public void removerLibroPrestado(String isbn) {
        librosPrestados.remove(isbn);
    }

    public void agregarMulta(BigDecimal multa) {
        this.multasAcumuladas = this.multasAcumuladas.add(multa);
    }

    public void pagarMultas() {
        this.multasAcumuladas = BigDecimal.ZERO;
    }

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public List<String> getLibrosPrestados() { return new ArrayList<>(librosPrestados); }
    public BigDecimal getMultasAcumuladas() { return multasAcumuladas; }
    public int getCantidadLibrosPrestados() { return librosPrestados.size(); }

    @Override
    public String toString() {
        return String.format("ID: %d | Nombre: %s | Email: %s | Libros prestados: %d | Multas: $%.2f",
                id, nombre, email, librosPrestados.size(), multasAcumuladas);
    }
}

// Clase Prestamo
class Prestamo {
    private String id;
    private String isbnLibro;
    private int idUsuario;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private LocalDate fechaDevolucionReal;
    private EstadoPrestamo estado;
    private BigDecimal multa;
    private static final BigDecimal MULTA_POR_DIA = new BigDecimal("500");
    private static final int DIAS_PRESTAMO = 14;

    public Prestamo(String isbnLibro, int idUsuario) {
        this.id = UUID.randomUUID().toString();
        this.isbnLibro = isbnLibro;
        this.idUsuario = idUsuario;
        this.fechaPrestamo = LocalDate.now();
        this.fechaDevolucion = fechaPrestamo.plusDays(DIAS_PRESTAMO);
        this.estado = EstadoPrestamo.ACTIVO;
        this.multa = BigDecimal.ZERO;
    }

    public void devolver() {
        this.fechaDevolucionReal = LocalDate.now();
        this.estado = EstadoPrestamo.DEVUELTO;
        calcularMulta();
    }

    private void calcularMulta() {
        if (fechaDevolucionReal.isAfter(fechaDevolucion)) {
            long diasRetraso = ChronoUnit.DAYS.between(fechaDevolucion, fechaDevolucionReal);
            this.multa = MULTA_POR_DIA.multiply(BigDecimal.valueOf(diasRetraso));
        }
    }

    public void marcarComoVencido() {
        if (estado == EstadoPrestamo.ACTIVO && LocalDate.now().isAfter(fechaDevolucion)) {
            this.estado = EstadoPrestamo.VENCIDO;
        }
    }

    // Getters
    public String getId() { return id; }
    public String getIsbnLibro() { return isbnLibro; }
    public int getIdUsuario() { return idUsuario; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public EstadoPrestamo getEstado() { return estado; }
    public BigDecimal getMulta() { return multa; }

    @Override
    public String toString() {
        return String.format("Préstamo ID: %s | Libro: %s | Usuario: %d | Estado: %s | Multa: $%.2f",
                id.substring(0, 8), isbnLibro, idUsuario, estado, multa);
    }
}

// Clase principal Biblioteca
class Biblioteca {
    private Map<String, Libro> libros;
    private Map<Integer, Usuario> usuarios;
    private List<Prestamo> prestamos;

    public Biblioteca() {
        this.libros = new HashMap<>();
        this.usuarios = new HashMap<>();
        this.prestamos = new ArrayList<>();
    }

    public void agregarLibro(Libro libro) {
        libros.put(libro.getIsbn(), libro);
    }

    public void registrarUsuario(Usuario usuario) {
        usuarios.put(usuario.getId(), usuario);
    }

    public synchronized void realizarPrestamo(String isbn, int idUsuario) {
        Libro libro = buscarLibroPorISBN(isbn)
                .orElseThrow(() -> new LibroNoDisponibleException("Libro no encontrado: " + isbn));
        
        Usuario usuario = buscarUsuarioPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + idUsuario));

        if (!libro.estaDisponible()) {
            throw new LibroNoDisponibleException("Libro no disponible: " + libro.getTitulo());
        }

        if (!usuario.puedePedirPrestado()) {
            throw new UsuarioSinCupoException("Usuario no puede pedir más libros: " + usuario.getNombre());
        }

        libro.prestar();
        usuario.agregarLibroPrestado(isbn);
        
        Prestamo prestamo = new Prestamo(isbn, idUsuario);
        prestamos.add(prestamo);
        
        System.out.println("✅ Préstamo realizado exitosamente");
    }

    public synchronized void devolverLibro(String isbn, int idUsuario) {
        Prestamo prestamo = prestamos.stream()
                .filter(p -> p.getIsbnLibro().equals(isbn) && 
                            p.getIdUsuario() == idUsuario && 
                            p.getEstado() == EstadoPrestamo.ACTIVO)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Préstamo activo no encontrado"));

        Libro libro = libros.get(isbn);
        Usuario usuario = usuarios.get(idUsuario);

        libro.devolver();
        usuario.removerLibroPrestado(isbn);
        prestamo.devolver();

        if (prestamo.getMulta().compareTo(BigDecimal.ZERO) > 0) {
            usuario.agregarMulta(prestamo.getMulta());
            System.out.printf("⚠️ Multa generada: $%.2f%n", prestamo.getMulta());
        }
        
        System.out.println("✅ Libro devuelto exitosamente");
    }

    public Optional<Libro> buscarLibroPorISBN(String isbn) {
        return Optional.ofNullable(libros.get(isbn));
    }

    public List<Libro> buscarLibrosPorTitulo(String titulo) {
        return libros.values().stream()
                .filter(libro -> libro.getTitulo().toLowerCase().contains(titulo.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Optional<Usuario> buscarUsuarioPorId(int id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    public List<Libro> obtenerLibrosDisponibles() {
        return libros.values().stream()
                .filter(Libro::estaDisponible)
                .collect(Collectors.toList());
    }

    public List<Prestamo> obtenerPrestamosDeUsuario(int idUsuario) {
        return prestamos.stream()
                .filter(p -> p.getIdUsuario() == idUsuario)
                .collect(Collectors.toList());
    }

    public List<Usuario> obtenerUsuariosConMultas() {
        return usuarios.values().stream()
                .filter(u -> u.getMultasAcumuladas().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
    }

    public List<Libro> obtenerTopLibrosPrestados(int limite) {
        return libros.values().stream()
                .sorted((l1, l2) -> Integer.compare(l2.getContadorPrestamos(), l1.getContadorPrestamos()))
                .limit(limite)
                .collect(Collectors.toList());
    }

    public void actualizarEstadosPrestamos() {
        prestamos.forEach(Prestamo::marcarComoVencido);
    }
}

// CLASE PRINCIPAL CON MAIN
public class BibliotecaApp {
    private static Scanner scanner = new Scanner(System.in);
    private static Biblioteca biblioteca = new Biblioteca();

    public static void main(String[] args) {
        inicializarDatosEjemplo();
        
        System.out.println("📚 SISTEMA DE GESTIÓN DE BIBLIOTECA 📚");
        
        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            int opcion = leerOpcion();
            
            switch (opcion) {
                case 1: agregarLibro(); break;
                case 2: registrarUsuario(); break;
                case 3: realizarPrestamo(); break;
                case 4: devolverLibro(); break;
                case 5: consultarLibrosDisponibles(); break;
                case 6: consultarPrestamosUsuario(); break;
                case 7: listarUsuariosConMultas(); break;
                case 8: topLibrosPrestados(); break;
                case 9: salir = true; break;
                default: System.out.println("❌ Opción inválida");
            }
            
            if (!salir) {
                System.out.println("\nPresiona Enter para continuar...");
                scanner.nextLine();
            }
        }
        
        System.out.println("👋 ¡Gracias por usar el sistema!");
    }

    private static void inicializarDatosEjemplo() {
        // Libros de ejemplo
        biblioteca.agregarLibro(new Libro("9788437604947", "Cien años de soledad", "Gabriel García Márquez", 1967, 5));
        biblioteca.agregarLibro(new Libro("9788408268521", "El Quijote", "Miguel de Cervantes", 1605, 3));
        biblioteca.agregarLibro(new Libro("9788497593798", "1984", "George Orwell", 1949, 4));
        biblioteca.agregarLibro(new Libro("9788466338141", "Harry Potter y la piedra filosofal", "J.K. Rowling", 1997, 6));
        
        // Usuarios de ejemplo
        biblioteca.registrarUsuario(new Usuario("Ana García", "ana@email.com"));
        biblioteca.registrarUsuario(new Usuario("Carlos López", "carlos@email.com"));
    }

    private static void mostrarMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📋 MENÚ PRINCIPAL");
        System.out.println("=".repeat(50));
        System.out.println("1. 📖 Agregar libro");
        System.out.println("2. 👤 Registrar usuario");
        System.out.println("3. 🔄 Realizar préstamo");
        System.out.println("4. 📚 Devolver libro");
        System.out.println("5. 🔍 Consultar libros disponibles");
        System.out.println("6. 📋 Consultar préstamos de usuario");
        System.out.println("7. 💰 Listar usuarios con multas");
        System.out.println("8. 🏆 Top 5 libros más prestados");
        System.out.println("9. ❌ Salir");
        System.out.println("=".repeat(50));
        System.out.print("Selecciona una opción: ");
    }

    private static int leerOpcion() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void agregarLibro() {
        System.out.println("\n📖 AGREGAR LIBRO");
        try {
            System.out.print("ISBN (13 dígitos): ");
            String isbn = scanner.nextLine();
            
            System.out.print("Título: ");
            String titulo = scanner.nextLine();
            
            System.out.print("Autor: ");
            String autor = scanner.nextLine();
            
            System.out.print("Año: ");
            int año = Integer.parseInt(scanner.nextLine());
            
            System.out.print("Ejemplares totales: ");
            int ejemplares = Integer.parseInt(scanner.nextLine());
            
            Libro libro = new Libro(isbn, titulo, autor, año, ejemplares);
            biblioteca.agregarLibro(libro);
            System.out.println("✅ Libro agregado exitosamente");
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void registrarUsuario() {
        System.out.println("\n👤 REGISTRAR USUARIO");
        try {
            System.out.print("Nombre: ");
            String nombre = scanner.nextLine();
            
            System.out.print("Email: ");
            String email = scanner.nextLine();
            
            Usuario usuario = new Usuario(nombre, email);
            biblioteca.registrarUsuario(usuario);
            System.out.println("✅ Usuario registrado exitosamente. ID: " + usuario.getId());
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void realizarPrestamo() {
        System.out.println("\n🔄 REALIZAR PRÉSTAMO");
        try {
            System.out.print("ISBN del libro: ");
            String isbn = scanner.nextLine();
            
            System.out.print("ID del usuario: ");
            int idUsuario = Integer.parseInt(scanner.nextLine());
            
            biblioteca.realizarPrestamo(isbn, idUsuario);
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void devolverLibro() {
        System.out.println("\n📚 DEVOLVER LIBRO");
        try {
            System.out.print("ISBN del libro: ");
            String isbn = scanner.nextLine();
            
            System.out.print("ID del usuario: ");
            int idUsuario = Integer.parseInt(scanner.nextLine());
            
            biblioteca.devolverLibro(isbn, idUsuario);
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void consultarLibrosDisponibles() {
        System.out.println("\n🔍 LIBROS DISPONIBLES");
        List<Libro> disponibles = biblioteca.obtenerLibrosDisponibles();
        if (disponibles.isEmpty()) {
            System.out.println("No hay libros disponibles");
        } else {
            disponibles.forEach(System.out::println);
        }
    }

    private static void consultarPrestamosUsuario() {
        System.out.println("\n📋 PRÉSTAMOS DE USUARIO");
        try {
            System.out.print("ID del usuario: ");
            int idUsuario = Integer.parseInt(scanner.nextLine());
            
            List<Prestamo> prestamos = biblioteca.obtenerPrestamosDeUsuario(idUsuario);
            if (prestamos.isEmpty()) {
                System.out.println("El usuario no tiene préstamos registrados");
            } else {
                prestamos.forEach(System.out::println);
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void listarUsuariosConMultas() {
        System.out.println("\n💰 USUARIOS CON MULTAS");
        List<Usuario> usuariosConMultas = biblioteca.obtenerUsuariosConMultas();
        if (usuariosConMultas.isEmpty()) {
            System.out.println("No hay usuarios con multas pendientes");
        } else {
            usuariosConMultas.forEach(System.out::println);
        }
    }

    private static void topLibrosPrestados() {
        System.out.println("\n🏆 TOP 5 LIBROS MÁS PRESTADOS");
        List<Libro> topLibros = biblioteca.obtenerTopLibrosPrestados(5);
        if (topLibros.isEmpty()) {
            System.out.println("No hay préstamos registrados");
        } else {
            topLibros.forEach(System.out::println);
        }
    }
}