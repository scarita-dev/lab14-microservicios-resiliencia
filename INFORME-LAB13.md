# Laboratorio 13 — Service Discovery y Gateway

**Estudiante:** Sofia Cindell Carita Quispe  
**Guía:** `GLAB-S13-RCOELLO-2026-01.docx` — *Desarrollo de aplicaciones Web: Service Discovery y Gateway*  
**Proyecto base:** `lab14-microservicios-resiliencia-main` (versión acumulada que incluye el Lab 13 y el Lab 14)  
**Fecha del informe:** 23/11/2026

> **Nota de alcance:** la carpeta del proyecto se llama `lab14` porque corresponde a la entrega acumulada. El presente informe documenta únicamente los requerimientos del **Lab 13**: Eureka Server, API Gateway y publicación de los servicios. La implementación de Config Server y Resilience4j se detalla en `INFORME-LAB14.md`.

---

## 1. Arquitectura implementada

| Componente | Proyecto / carpeta | Puerto | Función |
| :-- | :-- | :--: | :-- |
| Servidor de descubrimiento | `eureka-server` | 8761 | Registro de instancias |
| API Gateway | `gateway-server` | 8080 | Punto de entrada único y enrutamiento |
| Servicio de categorías | `categoria-service` | 8082 | CRUD de categorías |
| Servicio de productos | `producto-service` | 8083 | CRUD de productos |
| Servicio de clientes | `cliente-service` | 8084 | CRUD de clientes (actividad) |
| Servicio de pedidos | `pedido-service` | 8085 | CRUD de pedidos (actividad) |
| Config Server | `config-server` | 8888 | Configuración centralizada (necesario para los puertos) |

Flujo de una petición:

```text
Cliente -> http://localhost:8080/api/... -> gateway-server
        -> lb://nombre-servicio -> Eureka (resolución) -> instancia real
```

---

## 2. Checklist de requerimientos del Lab 13

| # | Requerimiento de la guía | Implementación en el proyecto | Estado |
| :--: | :-- | :-- | :--: |
| 1 | Crear proyecto Maven `gateway-service` | Proyecto `gateway-server` con el mismo propósito | ✅ |
| 2 | Agregarlo a la carpeta `microservicios` | Módulo declarado en el `pom.xml` raíz | ✅ |
| 3 | Importar el módulo Maven | Proyecto incluido en el workspace | ✅ |
| 4 | Dependencias Gateway + Eureka Client + LoadBalancer + Actuator | `gateway-server/pom.xml` | ✅ |
| 5 | Clase principal con `@SpringBootApplication` | `GatewayServerApplication` | ✅ |
| 6 | `application.yaml` con puerto 8080 y rutas | `gateway-server/src/main/resources/application.yml` | ✅ |
| 7 | Ruta `/api/categorias/**` | `lb://categoria-service` | ✅ |
| 8 | Ruta `/api/productos/**` | `lb://producto-service` | ✅ |
| 9 | Servicios registrados en Eureka | `eureka.client.service-url` en los cuatro servicios | ✅ |
| 10 | Ejecutar Eureka, categoría, producto y Gateway | Ejecutado el 23/11/2026; puertos 8761, 8082, 8083 y 8080 activos | ✅ |
| 11 | Verificar Eureka Server | Evidencia `evidencias/25-lab13-eureka-servicios.png` | ✅ |
| 12 | Probar `:8080/api/categorias` y `:8080/api/productos` | Evidencias `26` y `27`; ambos respondieron `HTTP 200` | ✅ |
| 13 | **Actividad:** publicar `cliente-service` y `pedido-service` | Rutas `/api/clientes/**` y `/api/pedidos/**` | ✅ |
| 14 | Conclusiones (5) | Sección 9 | ✅ |

---

## 3. Implementación del Gateway

### 3.1 Dependencias principales

Archivo: `gateway-server/pom.xml`

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway-server-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Versiones usadas:

- Spring Boot `3.5.3`
- Spring Cloud `2025.0.0`
- Java `17`

### 3.2 Clase principal

Archivo: `gateway-server/src/main/java/com/tecsup/GatewayServerApplication.java`

```java
package com.tecsup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayServerApplication.class, args);
    }
}
```

> La guía muestra además `@EnableDiscoveryClient`. En Spring Cloud 2025.0.0 el cliente de Eureka se autoconfigura, por lo que la anotación es opcional. Si el docente exige que aparezca de forma explícita, se puede agregar sin cambiar el funcionamiento.

### 3.3 Configuración del Gateway

Archivo: `gateway-server/src/main/resources/application.yml`

```yaml
server:
  port: 8080

spring:
  application:
    name: gateway-server
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: categoria-service
              uri: lb://categoria-service
              predicates:
                - Path=/api/categorias/**
            - id: producto-service
              uri: lb://producto-service
              predicates:
                - Path=/api/productos/**
            - id: cliente-service
              uri: lb://cliente-service
              predicates:
                - Path=/api/clientes/**
            - id: pedido-service
              uri: lb://pedido-service
              predicates:
                - Path=/api/pedidos/**

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka

management:
  endpoints:
    web:
      exposure:
        include: health,info,gateway
```

### 3.4 Diferencia importante con el YAML de la guía

La guía utiliza la propiedad:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - ...
```

Esa forma corresponde a versiones anteriores de Spring Cloud Gateway. En **Spring Cloud 2025.0.0** las rutas se declaran bajo:

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - ...
```

Por eso **no se debe reemplazar** el YAML actual por el fragmento literal de la guía: las rutas podrían no cargarse. El proyecto ya usa la estructura correcta para su versión.

---

## 4. Rutas publicadas mediante el Gateway

| Ruta pública | Servicio destino | Archivo del controlador |
| :-- | :-- | :-- |
| `GET /api/categorias` | `categoria-service` (8082) | `categoria-service/.../CategoriaController.java` |
| `GET /api/productos` | `producto-service` (8083) | `producto-service/.../ProductoController.java` |
| `GET /api/clientes` | `cliente-service` (8084) | `cliente-service/.../ClienteController.java` |
| `GET /api/pedidos` | `pedido-service` (8085) | `pedido-service/.../PedidoController.java` |

Los cuatro controladores están mapeados con `@RequestMapping("/api/...")`, por lo que coinciden exactamente con los `Path` del Gateway.

---

## 5. Actividad: publicación de cliente y pedido

La actividad solicitaba publicar `cliente-service` y `pedido-service` a través del Gateway. Ya está resuelta con las dos rutas agregadas en `application.yml`:

```yaml
- id: cliente-service
  uri: lb://cliente-service
  predicates:
    - Path=/api/clientes/**

- id: pedido-service
  uri: lb://pedido-service
  predicates:
    - Path=/api/pedidos/**
```

Con esto, el cliente externo solo conoce el puerto `8080`; los puertos internos `8084` y `8085` quedan ocultos y se resuelven por nombre mediante Eureka y `lb://`.

---

## 6. Procedimiento de ejecución

### Prerrequisitos

- JDK 17
- Maven 3.9+
- MySQL 8.x en `localhost:3306`, usuario `root` sin contraseña
- Bases de datos:

```sql
CREATE DATABASE IF NOT EXISTS bd_categoria_dev;
CREATE DATABASE IF NOT EXISTS bd_producto_dev;
CREATE DATABASE IF NOT EXISTS bd_cliente_dev;
CREATE DATABASE IF NOT EXISTS bd_pedido_dev;
```

### Orden de arranque

Abrir una terminal por servicio, desde la raíz del proyecto.

**1. Config Server** — necesario porque los servicios obtienen su puerto desde `config-repo`:

```powershell
mvn -pl config-server spring-boot:run
```

Verificar: `http://localhost:8888/categoria-service/dev`

**2. Eureka Server:**

```powershell
mvn -pl eureka-server spring-boot:run
```

Verificar: `http://localhost:8761`

**3. Servicios de negocio** (en terminales separadas):

```powershell
mvn -pl categoria-service spring-boot:run
mvn -pl producto-service spring-boot:run
mvn -pl cliente-service spring-boot:run
mvn -pl pedido-service spring-boot:run
```

Puertos esperados: `8082`, `8083`, `8084`, `8085`.

**4. Gateway:**

```powershell
mvn -pl gateway-server spring-boot:run
```

Puerto: `8080`.

> **Importante:** si no se levanta el Config Server, los servicios arrancan igual porque el import es `optional:configserver:...`, pero todos usarían el puerto `8080` por defecto y chocarían con el Gateway. Por eso el Config Server debe iniciarse primero.

> Esperar unos 30 segundos después de arrancar los servicios para que Eureka y el LoadBalancer actualicen el registro antes de probar el Gateway.

---

## 7. Pruebas y resultados obtenidos

**Fecha de ejecución:** 23/11/2026, 19:50 (America/Lima).

| Prueba | URL | Resultado obtenido |
| :-- | :-- | :-- |
| Panel de Eureka | `http://localhost:8761` | `CATEGORIA-SERVICE`, `PRODUCTO-SERVICE`, `CLIENTE-SERVICE`, `PEDIDO-SERVICE` y `GATEWAY-SERVER` en estado `UP` |
| Categorías por Gateway | `http://localhost:8080/api/categorias` | `HTTP 200` + JSON con 3 categorías |
| Productos por Gateway | `http://localhost:8080/api/productos` | `HTTP 200` + JSON con 3 productos y su categoría |
| Clientes por Gateway | `http://localhost:8080/api/clientes` | `HTTP 200` + JSON con 2 clientes |
| Pedidos por Gateway | `http://localhost:8080/api/pedidos` | `HTTP 200` + JSON con 2 pedidos, cliente y producto |
| Health del Gateway | `http://localhost:8080/actuator/health` | `HTTP 200` + `{"status":"UP"}` |

Respuesta real observada para categorías:

```json
[
  {"id":1,"nombre":"Tecnología","estado":true},
  {"id":2,"nombre":"Hogar","estado":true},
  {"id":3,"nombre":"Deportes","estado":true}
]
```

Respuesta real resumida para productos:

```json
[
  {"id":1,"nombre":"Laptop","precio":2500.00,"stock":10,"categoriaId":1},
  {"id":2,"nombre":"Mouse","precio":80.00,"stock":50,"categoriaId":1},
  {"id":3,"nombre":"Silla","precio":450.00,"stock":15,"categoriaId":2}
]
```

Las cuatro rutas respondieron correctamente usando el puerto **8080**, lo que confirma que la petición pasó por el Gateway y no directamente por el puerto interno del servicio. Las cabeceras HTTP completas y los cuerpos de respuesta se conservan en `evidencias/consola-gateway-lab13.txt`.

> **Nota de ejecución:** el equipo donde se tomaron las capturas no tenía MySQL activo, por lo que los servicios se levantaron con una base **H2 en memoria** y los mismos puertos `8082`–`8085`. No se modificó el código ni los `pom.xml`; la configuración MySQL del `config-repo` sigue siendo la usada en el entorno del laboratorio. Para el objetivo del Lab 13 (enrutamiento y descubrimiento) el resultado es equivalente.

---

## 8. Evidencias

### Evidencias generadas para el Lab 13

| Archivo | Contenido |
| :-- | :-- |
| `evidencias/25-lab13-eureka-servicios.png` | Panel de Eureka con los 5 servicios `UP`, incluido `GATEWAY-SERVER` |
| `evidencias/26-lab13-gateway-categorias.png` | `GET http://localhost:8080/api/categorias` → `HTTP 200` |
| `evidencias/27-lab13-gateway-productos.png` | `GET http://localhost:8080/api/productos` → `HTTP 200` |
| `evidencias/28-lab13-gateway-clientes.png` | `GET http://localhost:8080/api/clientes` → `HTTP 200` |
| `evidencias/29-lab13-gateway-pedidos.png` | `GET http://localhost:8080/api/pedidos` → `HTTP 200` |
| `evidencias/30-lab13-gateway-health.png` | `GET http://localhost:8080/actuator/health` → `HTTP 200`, `{"status":"UP"}` |
| `evidencias/consola-gateway-lab13.txt` | Salidas `curl -i` con cabeceras y cuerpos de las cinco pruebas, más el listado de servicios en Eureka |

Cada captura incluye un rótulo con la URL exacta y el código `HTTP 200` para dejar constancia de que la petición se hizo por el puerto `8080`. Las evidencias previas `11-eureka-servicios-activos.png` y `15-categorias-activas.png` se conservan como respaldo.

---

## 9. Conclusiones

1. **El Service Discovery elimina el acoplamiento a direcciones fijas.** Los consumidores usan el nombre lógico del servicio (`categoria-service`, `producto-service`, etc.) y Eureka resuelve la instancia disponible; si cambia el puerto o se levanta una réplica, el cliente no necesita reconfigurarse.

2. **El API Gateway centraliza y simplifica el acceso.** Los clientes externos solo conocen `http://localhost:8080` y las rutas `/api/...`; los puertos internos `8082`–`8085` quedan ocultos. Esto reduce la superficie expuesta y facilita aplicar después políticas comunes de seguridad, monitoreo o límites de tráfico.

3. **`lb://` aporta balanceo y tolerancia a fallos del lado del cliente.** Al combinar Eureka con Spring Cloud LoadBalancer, el Gateway puede elegir entre varias instancias del mismo servicio. Si una instancia no está disponible, el registro se actualiza y las peticiones se dirigen a las restantes.

4. **La configuración del Gateway depende de la versión de Spring Cloud.** El prefijo `spring.cloud.gateway.routes` de la guía no es el adecuado para Spring Cloud 2025.0.0; en esta versión las rutas van bajo `spring.cloud.gateway.server.webflux.routes`. Copiar el fragmento antiguo sin adaptarlo puede dejar el Gateway sin rutas, aunque compile correctamente.

5. **Publicar un nuevo servicio es una tarea acotada y declarativa.** Para la actividad bastó con agregar una ruta con su `id`, `uri: lb://...` y `Path` en el YAML; no fue necesario modificar los controladores ni el código de los servicios. Esto demuestra que el Gateway actúa como una capa de composición sobre servicios ya existentes.

---

## 10. Observaciones y recomendaciones

- **Nombre del artefacto:** la guía indica `gateway-service`, pero el proyecto usa `gateway-server`. No afecta el funcionamiento; solo debe considerarse si la entrega exige el nombre exacto.
- **`@EnableDiscoveryClient`:** es opcional en Spring Cloud 2025.0.0. Agregarla no cambia el comportamiento, pero deja la clase principal idéntica a la guía.
- **Locator de descubrimiento:** la guía habilita `spring.cloud.gateway.discovery.locator.enabled`. El proyecto no lo usa porque declara rutas explícitas para los cuatro servicios. Las rutas explícitas son preferibles cuando se quiere controlar qué servicios se exponen.
- **Dependencia del Config Server:** para reproducir el laboratorio tal como está en este proyecto, el Config Server debe estar activo; de lo contrario, los puertos `8082`–`8085` no se aplican.
- **Evidencia completa:** las capturas `25`–`30` y el archivo `consola-gateway-lab13.txt` cubren el panel de Eureka y las cinco pruebas por el puerto `8080`.

---

## 11. Archivos relevantes

```text
pom.xml
gateway-server/pom.xml
gateway-server/src/main/java/com/tecsup/GatewayServerApplication.java
gateway-server/src/main/resources/application.yml
eureka-server/src/main/resources/application.yml
categoria-service/src/main/resources/application.yml
producto-service/src/main/resources/application.yml
cliente-service/src/main/resources/application.yml
pedido-service/src/main/resources/application.yml
```
