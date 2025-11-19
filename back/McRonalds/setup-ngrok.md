# 🚀 Configuración de ngrok para MercadoPago

## 📋 Pasos para Solucionar el Error de Redirección

### 1. **Instalar ngrok**

#### Opción A: Descargar desde el sitio web
1. Ve a https://ngrok.com/
2. Regístrate (gratis)
3. Descarga ngrok para Windows
4. Extrae el archivo `ngrok.exe` en una carpeta
5. Abre PowerShell en esa carpeta

#### Opción B: Usar npm (si tienes Node.js)
```bash
npm install -g ngrok
```

### 2. **Ejecutar ngrok**
```bash
# En PowerShell o CMD:
ngrok http 8080
```

**Salida esperada:**
```
ngrok by @inconshreveable

Session Status                online
Account                       tu-email@ejemplo.com
Version                       3.1.0
Region                        United States (us)
Latency                       45ms
Web Interface                 http://127.0.0.1:4040
Forwarding                    https://abc123.ngrok.io -> http://localhost:8080
Forwarding                    http://abc123.ngrok.io -> http://localhost:8080

Connections                   ttl     opn     rt1     rt5     p50     p90
                              0       0       0.00    0.00    0.00    0.00
```

### 3. **Copiar la URL de ngrok**
Copia la URL que aparece en "Forwarding" (ejemplo: `https://abc123.ngrok.io`)

### 4. **Actualizar application.properties**
Reemplaza las URLs en `src/main/resources/application.properties`:

```properties
# REEMPLAZA abc123.ngrok.io con tu URL de ngrok
mercadopago.backurl.success=https://abc123.ngrok.io/api/mercadopago/success
mercadopago.backurl.failure=https://abc123.ngrok.io/api/mercadopago/failure
mercadopago.backurl.pending=https://abc123.ngrok.io/api/mercadopago/pending
mercadopago.notificationUrl=https://abc123.ngrok.io/api/mercadopago/webhook
```

### 5. **Reiniciar la aplicación**
```bash
# Detén la aplicación (Ctrl+C)
# Vuelve a ejecutar:
mvn spring-boot:run
```

### 6. **Probar el flujo**
1. Crea una preferencia de pago
2. Usa la URL de sandbox
3. Completa el pago con tarjeta de prueba
4. Deberías ser redirigido correctamente a tu aplicación

## 🔍 Verificar que Funciona

### Verificar ngrok:
- Ve a http://127.0.0.1:4040 (interfaz web de ngrok)
- Deberías ver las peticiones que llegan a tu aplicación

### Verificar URLs:
- Las URLs de retorno ahora apuntan a ngrok, no a localhost
- MercadoPago puede acceder a tu aplicación a través de ngrok

## ⚠️ Importante

- **ngrok debe estar ejecutándose** mientras uses la aplicación
- **La URL de ngrok cambia** cada vez que reinicias ngrok (en la versión gratuita)
- **Para producción**, usa un servidor real, no ngrok

## 🎯 Resultado Esperado

Después de configurar ngrok, cuando completes un pago:
- ✅ Serás redirigido a tu aplicación (no a la página de error)
- ✅ El webhook funcionará correctamente
- ✅ Los registros se guardarán en la base de datos
