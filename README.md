# 🍔 Mc Ronald's - Sistema de Pedido de Comida

## 📌 Descripción General
Mc Ronald's es un sistema desarrollado para un restaurante de comida rápida.  
El proyecto tiene como objetivo digitalizar la experiencia de compra de los clientes mediante **pantallas táctiles** dentro de los locales.  

Los clientes podrán:
- Visualizar el menú (promociones, más pedidos, etc.).
- Realizar un pedido como **invitado** (solo colocando su nombre).
- Pagar con **tarjeta** o **Yape**.
- Entrega de su boleta y recibir su orden en caja cuando esté lista.

- Además, el sistema incluye dos roles principales:
- **Administrador (ADMIN)**  
  - Gestión de usuarios (CRUD).  
  - Gestión del menú (CRUD).  
- **Cocinero (COCINERO)**  
  - Visualización en tiempo real de los pedidos realizados por los clientes.  

---

## ⚙️ Tecnologías Usadas
- **Backend:** Spring Boot (Java 11+)  
- **Frontend:** React con Vite / Create React App (dependiendo del setup)  
- **Base de datos:** MySQL 
- **Seguridad:** Spring Security + JWT  
- **Estilos:** TailwindCSS / Bootstrap (a definir según diseño)  
- **Control de versiones:** Git + GitHub  

---

## 📋 Responsabilidades

| Miembro del equipo |  Rol  |
|:-----|:--------:|
| Abraham Vergara   | Full Stack |
| Angel Salazar   | Backend|
| Erixon Castillo   | Full Stack |
| Francis Moreno   |  Frontend |
| Samanta Cordova   | Full Stack |


---

## :jigsaw: Flujo de trabajo con Git

**1. Ramas utilizadas**

- `main` → Rama estable, lista para producción.

- `develop` → Rama de desarrollo.

- `feature/nombre` → Para nuevas funcionalidades.

- `fix/nombre` → Para correcciones de errores.
#### Ejemplo
  ```bash
  git checkout -b feature/gestion-usuarios
  ```

**2. Commits**
- Commits atómicos y descriptivos..
#### Ejemplo
  ```bash
  git commit -m "feat: agregar CRUD de usuarios en backend"
  git commit -m "fix: corregir validación de contraseña en registro"
  ```
**3. Uso de comandos Git**

Durante el desarrollo se documentaron los siguientes comandos:

#### 🔄 Restaurar archivos
  ```bash
  git restore src/components/Menu.jsx
  ```
**📌 Por qué:** Cuando se modificó accidentalmente el componente Menu.jsx y se necesitó volver al estado previo.

#### ⏪ Resetear cambios
  ```bash
  git reset --soft HEAD~1
  ```
**📌 Por qué:** Para deshacer el último commit sin perder los cambios en el área de staging
#### 🔀 Cambiar de ramas
  ```bash
  git switch develop
  ```
**📌 Por qué:** Al pasar del desarrollo de una nueva feature hacia la rama develop.
  ```bash
  git checkout -b fix/error-pago
  ```
**📌 Por qué:** Para crear una rama de corrección desde la rama estable

**4. Pull Request (PR) / Merge Request (MR)**

Se generó una Pull Request desde `feature/gestion-usuarios` hacia `develop.`
- Descripción clara del cambio.

- Checklist de revisión.

- Revisión y aprobación antes de hacer merge.

**5. Resolución de Conflictos**

Al intentar fusionar `feature/gestion-usuarios` con `develop`, hubo un conflicto en:

`src/components/Header.jsx`
  ```bash
  <<<<<<< HEAD
<h1>Bienvenido a Mc Ronald's</h1>
=======
<h1>Mc Ronald's - Sistema de Pedidos</h1>
>>>>>>> feature/gestion-usuarios
  ```
**✅ Solución:** Se unificaron los cambios y se mantuvo el mensaje más completo:
  ```bash
  <h1> Bienvenido a Mc Ronald's - Sistema de Pedidos </h1>
  ```
**6. Historial de commits (puntos de control)**

#### Ejemplo de salida:
  ```bash
  git log --oneline --graph
  ```
#### Simulación:
  ```bash
  * a1b2c3d (HEAD -> develop) feat: agregar CRUD de menú
  * d4e5f6g fix: corregir error en validación de usuario
  * h7i8j9k feat: agregar login con JWT
  * l0m1n2o init: configuración inicial de Spring Boot
  ```
**7.Historial de cabeceras**
  ```bash
  git reflog
  ```
#### Simulación:
  ```bash
  d4e5f6g HEAD@{0}: commit: fix: corregir error en validación de usuario
h7i8j9k HEAD@{1}: commit: feat: agregar login con JWT
l0m1n2o HEAD@{2}: commit (initial): init: configuración inicial de Spring Boot
  ```
