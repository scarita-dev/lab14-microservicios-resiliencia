# Laboratorio 14 — Config Server y Resiliencia

**Estudiante:** Ricardo Coello Palomino (documento base GLAB-S14-RCOELLO-2026-01)
**Fecha de ejecución:** 21/11/2026
**Repositorio de configuración:** https://github.com/scarita-dev/config-repo (público, rama `main`)

> Nota: la guía propone la URI `https://github.com/rcoellopalomino-tech/config-repo`, pero la cuenta de GitHub autenticada en el navegador es `scarita-dev`. Se creó `config-repo` en esa cuenta y se ajustó la URI del Config Server. La guía también menciona Spring Boot 3.5.15 en un paso aislado; se empleó **3.5.3** según la indicación recibida y el POM del propio laboratorio.

---

## 0. Entorno y proyectos base creados

| Herramienta | Versión |
| :-- | :-- |
| JDK (Eclipse Temurin) | 17.0.21 |
| Apache Maven | 3.9.9 (portable) |
| MySQL Server | 8.4.9 (root sin contraseña) |
| Spring Boot | 3.5.3 |
| Spring Cloud | 2025.0.0 |
| Java | 17 |

No existían proyectos previos en la laptop, por lo que se creó `microservicios/` con siete proyectos Maven independientes (más un POM agregador) y se verificó la compilación con `mvn -DskipTests package` → **BUILD SUCCESS**.

| Servicio | Puerto | Funcionalidad base |
| :-- | :--: | :-- |
| config-server | 8888 | `@EnableConfigServer`, repositorio Git |
| eureka-server | 8761 | `@EnableEurekaServer` |
| gateway-server | 8080 | Rutas `lb://` a los cuatro servicios |
| categoria-service | 8082 | CRUD JPA + Eureka + OpenFeign + Config Client |
| producto-service | 8083 | CRUD JPA + Feign a categoría + Resilience4j + Actuator |
| cliente-service | 8084 | CRUD JPA + Feign a pedido + Resilience4j + Actuator |
| pedido-service | 8085 | CRUD JPA + Feign a cliente y producto + Resilience4j + Actuator |

Bases de datos creadas: `bd_categoria_dev`, `bd_producto_dev`, `bd_cliente_dev`, `bd_pedido_dev`.

---

## 1. Crear Config Server

`microservicios/config-server/src/main/java/com/tecsup/ConfigServerApplication.java`

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

`application.yaml`

```yaml
server:
  port: 8888
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/scarita-dev/config-repo
          clone-on-start: true
        default-label: main
```

**Repositorio creado en GitHub:** `scarita-dev/config-repo`, con 12 archivos YAML (4 base + 4 `dev` + 4 `prod`). Evidencia: `05-config-repo-perfiles.png`.

---

## 2. Prueba inicial del Config Server

Se ejecutó config-server y se consultaron los cuatro endpoints:

| Endpoint | `server.port` | `mensaje.texto` |
| :-- | :--: | :-- |
| `/categoria-service/default` | 8082 | Configuración ... para Categoría Service |
| `/producto-service/default` | 8083 | Configuración ... para Producto Service |
| `/cliente-service/default` | 8084 | Configuración ... para Cliente Service |
| `/pedido-service/default` | 8085 | Configuración ... para Pedido Service |

Evidencias: `01`, `02`, `03`, `04`.

---

## Actividad 1 — Configuración para cliente y pedido

Se agregaron `cliente-service.yml` y `pedido-service.yml` al repositorio con el mismo patrón de la guía (puerto, datasource MySQL, JPA y `mensaje.texto`). En cada proyecto local se agregó la dependencia:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```

y en `application.yml`:

```yaml
spring:
  application:
    name: cliente-service   # / pedido-service
  config:
    import: optional:configserver:http://localhost:8888
```

**Resultado:** los endpoints `/cliente-service/default` y `/pedido-service/default` devolvieron `server.port` 8084 y 8085 respectivamente, junto con sus mensajes personalizados. Evidencias: `03-config-cliente-default-actividad1.png` y `04-config-pedido-default-actividad1.png`.

---

## 3. Configuración con perfiles

Se modificaron los cuatro archivos base para dejarlos neutros (sin puerto ni datasource) y se crearon los archivos `-dev.yml` y `-prod.yml`:

```yaml
# categoria-service.yml
spring:
  application:
    name: categoria-service
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
mensaje:
  texto: Configuración obtenida desde Config Server para Categoría Service

# categoria-service-dev.yml
server:
  port: 8082
spring:
  application:
    name: categoria-service
  datasource:
    url: jdbc:mysql://localhost:3306/bd_categoria_dev
    username: root
    password:

# categoria-service-prod.yml
server:
  port: 9092
spring:
  datasource:
    url: jdbc:mysql://servidor/bd_categoria_prod
    username: root
    password:
```

El mismo patrón se aplicó a producto (8083/9093), cliente (8084/9094) y pedido (8085/9095). En los cuatro servicios locales se activó el perfil:

```yaml
spring:
  profiles:
    active: dev
```

**Resultado:** `/producto-service/default` devuelve una sola fuente (sin `server.port`), mientras que `/producto-service/dev` devuelve dos fuentes y `server.port: 8083`. Evidencias: `05` a `08`.

---

## Actividad 2 — Perfiles para cliente y pedido

Se aplicó la configuración por perfiles también a `cliente-service` y `pedido-service`:

```yaml
spring:
  application:
    name: cliente-service   # / pedido-service
  profiles:
    active: dev
  config:
    import: optional:configserver:http://localhost:8888
```

**Resultado verificado:**

| Endpoint | Fuentes | `server.port` |
| :-- | :--: | :--: |
| `/cliente-service/dev` | 2 | 8084 |
| `/pedido-service/dev` | 2 | 8085 |

Evidencias: `09-config-cliente-dev-actividad2.png` y `10-config-pedido-dev-actividad2.png`.

---

## 4. Circuit Breaker con Resilience4j (producto → categoría)

Dependencias agregadas a `producto-service`: `spring-cloud-starter-openfeign`, `spring-cloud-starter-config`, `resilience4j-spring-boot3` y `spring-boot-starter-actuator`.

`CategoriaCircuitService.java`:

```java
@Bulkhead(name = "categoriaBulkhead", fallbackMethod = "categoriaFallback")
@Retry(name = "categoriaRetry", fallbackMethod = "categoriaFallback")
@CircuitBreaker(name = "categoriaService", fallbackMethod = "categoriaFallback")
public CategoriaDTO obtenerCategoria(Long id) {
    System.out.println("Llamando a categoria-service...");
    return categoriaClient.obtenerCategoria(id);
}

public CategoriaDTO categoriaFallback(Long id, Throwable ex) {
    CategoriaDTO dto = new CategoriaDTO();
    dto.setId(id);
    dto.setNombre("Categoría temporal no disponible");
    return dto;
}
```

`application.yml` (resumen):

```yaml
resilience4j:
  circuitbreaker:
    instances:
      categoriaService:
        registerHealthIndicator: true
        slidingWindowSize: 5
        minimumNumberOfCalls: 3
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
  bulkhead:
    instances:
      categoriaBulkhead:
        maxConcurrentCalls: 2
        maxWaitDuration: 1s
  retry:
    instances:
      categoriaRetry:
        maxAttempts: 3
        waitDuration: 2s
```

**Prueba 1 (servicio arriba):** `GET http://localhost:8083/api/productos` → 200 con `categoria: {"id":1,"nombre":"Tecnología","estado":true}`. Evidencia `12`.

**Prueba 2 (categoría detenida):** mismo endpoint → 200 con `categoria: {"id":1,"nombre":"Categoría temporal no disponible"}`. Evidencia `16`; health del circuit breaker en `17`.

---

## Actividad 3 — Circuit Breaker en cliente → pedido y pedido → producto

| Requisito | Implementación |
| :-- | :-- |
| 1. Servicio CircuitBreaker | `PedidoCircuitService` (cliente) y `ProductoResilienceService` (pedido) |
| 2. Método fallback | `pedidoFallback` y `productoFallback`, devuelven DTO de emergencia |
| 3. Configuración Resilience4j | `pedidoService` en cliente; `pedidoProductoService` + `productoBulkhead` en pedido |
| 4. Demostración apagando el servicio | Cliente con pedido-service detenido; pedido con producto-service detenido |
| 5. Evidencias antes y después | `13`/`14` vs `18`; `13` vs `20` |

**Cliente → pedido:** con pedido-service detenido, `GET /api/clientes/1/pedidos` devolvió 200 con `total: 0` (fallback) en lugar de un 503. Evidencias: `14` (antes) y `18` (después), health en `19`.

**Pedido → producto:** con producto-service detenido, `GET /api/pedidos` devolvió 200 con `"producto": {"nombre":"Producto temporal no disponible"}` para cada pedido, manteniendo los datos del cliente reales. Evidencias: `13` (antes) y `20` (después), health en `21`.

> Corrección aplicada: inicialmente los métodos con `@CircuitBreaker`/`@Retry` de `pedido-service` se invocaban desde `PedidoService` mediante auto-invocación (`this...`), por lo que el proxy AOP no se aplicaba y se obtenía 500. Se extrajeron a los beans `ClienteResilienceService` y `ProductoResilienceService`, se recompiló y se repitió la prueba con éxito.

---

## 5. Retry Pattern (consulta al cliente)

`ClienteResilienceService.java`:

```java
@Retry(name = "clienteRetry", fallbackMethod = "clienteFallback")
public ClienteDTO obtenerCliente(Long id) {
    System.out.println("Llamando a cliente-service...");
    return clienteClient.obtenerCliente(id);
}

public ClienteDTO clienteFallback(Long id, Throwable ex) {
    ClienteDTO dto = new ClienteDTO();
    dto.setId(id);
    dto.setNombre("Cliente temporal no disponible");
    return dto;
}
```

```yaml
resilience4j:
  retry:
    instances:
      clienteRetry:
        maxAttempts: 3
        waitDuration: 2s
```

**Prueba:** se detuvo cliente-service y se llamó a `GET /api/pedidos`. La consola mostró tres intentos por pedido y después el fallback:

```
Llamando a cliente-service...
Llamando a cliente-service...
Llamando a cliente-service...
Fallback ejecutado (Retry cliente): Connection refused: getsockopt executing GET http://cliente-service/api/clientes/1
```

Respuesta HTTP: 200 con `"cliente": {"nombre":"Cliente temporal no disponible"}`. Evidencia `22`.
Además se simuló una demora de **5 s** en cliente-service (`GET /api/clientes/slow/1`, medido en 5.0 s). Evidencia `23`.

---

## 6. Bulkhead Pattern (consulta al producto)

`ProductoResilienceService.java`:

```java
@Bulkhead(name = "productoBulkhead", fallbackMethod = "productoFallback")
@CircuitBreaker(name = "pedidoProductoService", fallbackMethod = "productoFallback")
public ProductoDTO obtenerProducto(Long id) {
    System.out.println("Llamando a producto-service...");
    return productoClient.obtenerProducto(id);
}

public ProductoDTO productoFallback(Long id, Throwable ex) {
    ProductoDTO dto = new ProductoDTO();
    dto.setId(id);
    dto.setNombre("Producto temporal no disponible");
    return dto;
}
```

```yaml
resilience4j:
  bulkhead:
    instances:
      productoBulkhead:
        maxConcurrentCalls: 2
        maxWaitDuration: 1s
```

Se simuló una demora de **10 s** en producto-service (`GET /api/productos/bulkhead/1`, medido en 10.1 s). A continuación se lanzaron **4 peticiones concurrentes** a `GET /api/pedidos/bulkhead` desde Chrome:

```
Llamando a producto-service (lento)...
Llamando a producto-service (lento)...
Fallback ejecutado (Bulkhead producto): Bulkhead 'productoBulkhead' is full and does not permit further calls
```

Las peticiones admitidas tardaron ~20 s y devolvieron `Laptop`; las rechazadas respondieron de inmediato con `Producto temporal no disponible`. Evidencia `24` y `consola-resiliencia.txt`.

---

## Actividad final — Retry vs Bulkhead

**Requerimientos cumplidos**

| Requerimiento | Cumplimiento |
| :-- | :-- |
| Retry Pattern para la consulta al cliente | `@Retry(name="clienteRetry")` en `ClienteResilienceService` |
| Bulkhead Pattern para la consulta al producto | `@Bulkhead(name="productoBulkhead")` en `ProductoResilienceService` |
| Demora de 5 s en cliente-service | `/api/clientes/slow/{id}` → 5.0 s (`23`) |
| Demora de 10 s en producto-service | `/api/productos/bulkhead/{id}` → 10.1 s |
| Verificación por consola y `GET /api/pedidos` | `22`, `24` y `consola-resiliencia.txt` |

**Evidencias solicitadas**

1. Código Retry: `microservicios/pedido-service/src/main/java/com/tecsup/services/ClienteResilienceService.java` (sección 5).
2. Código Bulkhead: `microservicios/pedido-service/src/main/java/com/tecsup/services/ProductoResilienceService.java` (sección 6).
3. Configuración `application.yml`: `microservicios/pedido-service/src/main/resources/application.yml` (secciones 5 y 6).
4. Capturas: `22`, `23`, `24` y `consola-resiliencia.txt`.
5. Comportamiento observado: ver análisis siguiente.

**Análisis: diferencias entre Retry y Bulkhead**

| | Retry | Bulkhead |
| :-- | :-- | :-- |
| Qué hace | Repite la misma llamada ante un error | Limita el número de llamadas concurrentes |
| Configuración clave | `maxAttempts: 3`, `waitDuration: 2s` | `maxConcurrentCalls: 2`, `maxWaitDuration: 1s` |
| Observado | 3 intentos por pedido y luego fallback al detener cliente-service | 2 peticiones admitidas; las demás recibieron fallback inmediato |
| Cuándo conviene | Fallos transitorios (red, reinicio del proveedor) | Proveedor lento o con saturación; evita propagar la espera |

Retry es adecuado cuando el fallo es breve y probablemente se resuelva solo, pero multiplica la carga sobre el proveedor. Bulkhead es adecuado cuando el proveedor responde lento: acota los recursos (hilos) consumidos y entrega una respuesta degradada rápida a las peticiones excedentes. En este laboratorio se combinaron ambos con Circuit Breaker para que, después de varios fallos, el circuito se abra y no se siga llamando al servicio caído durante 10 s.

---

## 7. Conclusiones

1. **La configuración centralizada eliminó la duplicidad de archivos.** Un solo repositorio (`config-repo`) alimenta a los cuatro microservicios mediante `spring.config.import: optional:configserver:...`; al cambiar `mensaje.texto` o un puerto en GitHub, todos los servicios lo reciben sin recompilar, y `optional:` permite que arranquen incluso si el Config Server no está disponible.

2. **Los perfiles permiten separar entornos sin tocar el código.** Los archivos base, `-dev` y `-prod` se fusionan en el orden correcto: con `spring.profiles.active: dev` se obtiene el puerto 8082/8083/8084/8085 y MySQL local, mientras que el perfil `prod` apunta a `servidor` y a los puertos 9092-9095. Verificar `/categoria-service/dev` y `/producto-service/dev` hace evidente cuántas fuentes se aplicaron.

3. **Eureka + OpenFeign simplifican la comunicación, pero requieren sincronización.** Los clients `@FeignClient(name="...")` resuelven las instancias por nombre; al iniciar todos los servicios al mismo tiempo, el caché del load balancer todavía no tenía los registros y se produjeron 503 temporales. Tras el refresco del registro (≈30 s), las llamadas funcionaron, lo que confirma que la resolución depende del descubrimiento y no de URLs fijas.

4. **Resilience4j evita que una falla en cascada tumbe al conjunto.** Con categoria-service detenido, producto-service siguió respondiendo 200 con una categoría de emergencia; lo mismo ocurrió con cliente→pedido y pedido→producto. El fallback debe devolver un DTO válido para que el cliente reciba una respuesta degradada y no un error 500.

5. **Cada patrón de resiliencia resuelve un problema distinto y se complementan.** Retry absorbe fallos transitorios repitiendo la llamada (3 intentos, 2 s), Bulkhead limita la concurrencia (2 llamadas, espera máxima de 1 s) y Circuit Breaker deja de llamar al proveedor tras superar el umbral del 50 % de fallos durante 10 s. La observación en consola y en `/actuator/health` permitió comprobar los tres comportamientos y elegir el patrón según el síntoma (error vs. latencia).

---

## 8. Índice de evidencias (carpeta `microservicios/evidencias`)

| Archivo | Contenido |
| :-- | :-- |
| `00-commit-dialog.png` | Commit del primer archivo en GitHub |
| `01`-`04` | Endpoints `/{servicio}/default` del Config Server (incluye Actividad 1) |
| `05` | Repositorio `config-repo` con los 12 archivos YAML |
| `06`-`08` | Cambio a perfiles: `/producto-service/default`, `/producto-service/dev`, `/categoria-service/dev` |
| `09`-`10` | Actividad 2: `/cliente-service/dev` y `/pedido-service/dev` |
| `11` | Eureka con los servicios registrados |
| `12`-`17` | Circuit Breaker producto→categoría (antes/después y health) |
| `18`-`21` | Actividad 3: cliente→pedido y pedido→producto (antes/después y health) |
| `22`-`23` | Retry con cliente caído y demora de 5 s en cliente-service |
| `24` | Bulkhead con 4 peticiones concurrentes |
| `consola-resiliencia.txt` | Extractos de consola de Retry, Bulkhead y Circuit Breaker |
