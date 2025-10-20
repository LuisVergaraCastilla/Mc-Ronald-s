# 🍔 Mc Ronald's

## 🧠 Flujo de trabajo general 
El proyecto utiliza la estrategia **Git Flow** para garantizar un desarrollo ordenado y estable.

## Branch Rules
Las ramas sirven para trabajar en distintas partes del código sin dañar la versión principal:
- main → versión estable en producción  
- develop → versión en desarrollo  
- feature/* → nuevas funcionalidades  
- bugfix/* → correcciones en desarrollo  
- hotfix/* → correcciones urgentes en producción  
- release/* → preparación de una versión estable

en nuestro proyecto se puede visualizar el:
- "feature/integracion-pago-yape"
- "bugfix/error-calculo-total-pedido"
- "release/v1.2.0"

## Convenciones de nombres de ramas
Cada rama debe seguir el siguiente formato:

| Tipo de rama | Ejemplo | Descripción |
|---------------|----------|-------------|
| **feature/** | `feature/registro-de-clientes` | Nueva funcionalidad para registrar clientes. |
| **feature/** | `feature/integracion-pago-yape` | Añade integración del sistema de pagos con Yape. |
| **bugfix/** | `bugfix/error-calculo-total-pedido` | Corrige error en el cálculo del monto total. |
| **hotfix/** | `hotfix/fallo-login-clientes` | Corrige error crítico en login en producción. |
| **release/** | `release/v1.2.0` | Prepara la versión 1.2.0 para su despliegue. |

## Creacion de las ramas 
Antes de crear una rama, siempre debe estas actualizado repositorio local:
  ```bash
    git checkout develop
    git pull origin develop
  ```
  ```bas
     git checkout -b feature/gestion-inventario
  ```
---

## Políticas de commits
Los commits deben ser claros, específicos y en tiempo presente, por ejemplo nuestro proyecto lleva:

- feat: agregar endpoint para registrar pedidos
- fix: corregir error en cálculo de monto total del pedido
- refactor: optimizar consulta de inventario
- docs: actualizar instrucciones de instalación

## Reglas para Pull Requests (PR)
Antes de fusionar (merge) una rama a develop o main, deben cumplirse los siguientes requisitos:

✅ El código debe compilar correctamente.

✅ Todas las pruebas deben pasar exitosamente.

✅ El PR debe incluir una descripción clara del cambio.

✅ Se debe solicitar al menos una revisión de código (code review).

🚫 No se permite realizar merge directo a main sin revisión.

🚫 No se permite subir archivos temporales o de entorno local (.env, node_modules/, etc.).

-Ejemplo:
```bas
[feature] Implementación del módulo de control de inventario
```
```bas
Se agregó el módulo de control de inventario en el backend.
- Nuevas entidades: Producto, Categoría, Stock.
- Endpoints creados: GET /productos, POST /productos.
- Validaciones añadidas al registrar nuevos productos.
```
## Tag Rules (Reglas de etiquetas)
Las etiquetas (tags) se utilizan para identificar versiones estables o puntos importantes del desarrollo, por ello el proyecto McRonald’s utiliza el estándar Semantic Versioning (SemVer) con el formato:
| Campo     | Descripción                                     | Ejemplo  |
| --------- | ----------------------------------------------- | -------- |
| **MAJOR** | Cambios incompatibles con versiones anteriores. | `v2.0.0` |
| **MINOR** | Nuevas funcionalidades compatibles.             | `v1.3.0` |
| **PATCH** | Correcciones de errores o mejoras menores.      | `v1.2.1` |

Ejemplos de tags usados:
| Versión  | Descripción                                                    |
| -------- | -------------------------------------------------------------- |
| `v1.0.0` | Primera versión estable módulo de pedidos, pagos y usuarios. |
| `v1.1.0` | Se añadió integración con Yape.                                |
| `v1.1.1` | Corrección de errores en el proceso de pago.                   |
| `v1.2.0` | Mejora en gestión de inventario y actualización de reportes.   |

## Creación y publicación de una etiqueta
```bas
# Crear nueva rama de funcionalidad
git checkout develop
git checkout -b feature/integracion-pago-yape

#Desarrollar y realizar commits descriptivos
git add .
git commit -m "feat: agregar integración con Yape en módulo de pago"

#Subir rama al repositorio remoto
git push origin feature/integracion-pago-yape

#Crear Pull Request hacia develop y solicitar revisión

#Al aprobarse y probarse, mergear a develop

#Crear tag para marcar versión estable
git tag -a v1.1.0 -m "Versión 1.1.0 - Integración con Yape"
git push origin v1.1.0

