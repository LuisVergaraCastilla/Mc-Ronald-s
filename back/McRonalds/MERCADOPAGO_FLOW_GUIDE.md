# Guía del Flujo de MercadoPago - McRonalds

## 🔧 Problemas Corregidos

### 1. **Registro de Payment faltante**
- **Problema**: No se creaba el registro de `Payment` en la base de datos al crear la preferencia
- **Solución**: Ahora se crea automáticamente el registro con estado `PENDING`

### 2. **Logging mejorado**
- **Problema**: No había logs para debugging
- **Solución**: Agregados logs detallados en todo el flujo

### 3. **Configuración de URLs**
- **Problema**: URLs de localhost no funcionan con webhooks de MercadoPago
- **Solución**: Documentación para usar ngrok en desarrollo

## 📋 Flujo Corregido

### 1. **Crear Preferencia de Pago**

**✅ ENDPOINT RECOMENDADO:**
```http
POST /api/payments/create-preference/{orderId}
```

**⚠️ ENDPOINT ALTERNATIVO (también funciona ahora):**
```http
POST /api/mercadopago/preference
Content-Type: application/json

{
  "id": "123",
  "title": "Orden McRonalds #123",
  "description": "Pago de orden",
  "quantity": 1,
  "unitPrice": 25.50,
  "currencyId": "PEN"
}
```

**Respuesta exitosa:**
```json
{
  "preference_id": "1234567890-abc123",
  "init_point": "https://www.mercadopago.com.pe/checkout/v1/redirect?pref_id=1234567890-abc123",
  "sandbox_init_point": "https://sandbox.mercadopago.com.pe/checkout/v1/redirect?pref_id=1234567890-abc123",
  "payment_id": 1,
  "external_reference": "123"
}
```

**Lo que sucede internamente:**
1. ✅ Se crea la preferencia en MercadoPago
2. ✅ Se guarda el registro de `Payment` en BD con estado `PENDING`
3. ✅ Se retorna la URL para redirigir al usuario

### 2. **Usuario Completa el Pago**
- Usuario es redirigido a MercadoPago
- Completa el pago con tarjeta de prueba
- MercadoPago procesa el pago

### 3. **Webhook de MercadoPago**
```http
POST /api/mercadopago/webhook?type=payment&data_id=1234567890
```

**Lo que sucede internamente:**
1. ✅ MercadoPago envía notificación al webhook
2. ✅ Se obtiene información del pago desde MercadoPago
3. ✅ Se busca el pago en BD por `external_reference`
4. ✅ Se actualiza el estado del pago en BD
5. ✅ Si el pago es aprobado, se actualiza la orden a `CONFIRMED`

## 🛠️ Configuración para Desarrollo

### 1. **Instalar ngrok**
```bash
# Descargar ngrok desde https://ngrok.com/
# O usar npm:
npm install -g ngrok
```

### 2. **Exponer tu aplicación**
```bash
# En una terminal separada:
ngrok http 8080
```

### 3. **Actualizar URLs en application.properties**
```properties
# Reemplaza localhost con la URL de ngrok
mercadopago.notificationUrl=https://abc123.ngrok.io/api/mercadopago/webhook
```

## 🧪 Tarjetas de Prueba

### Tarjetas Aprobadas:
- **Visa**: 4509 9535 6623 3704
- **Mastercard**: 5031 7557 3453 0604
- **American Express**: 3753 651535 56885

### Datos de Prueba:
- **CVV**: 123
- **Fecha**: Cualquier fecha futura
- **Nombre**: Cualquier nombre

## 🔍 Debugging

### Logs que verás en la consola:

#### Al crear preferencia:
```
🔧 Creando preferencia de MercadoPago:
   - ID: 123
   - Title: Orden McRonalds #123
   - Amount: 25.50
   - External Reference: 123
✅ Payment creado en BD con ID: 1 para orden: 123
```

#### Al recibir webhook:
```
🔔 Webhook recibido - Type: payment, Data ID: 1234567890
💳 Procesando notificación de pago: 1234567890
✅ Pago obtenido de MercadoPago:
   - ID: 1234567890
   - Status: approved
   - External Reference: 123
   - Amount: 25.50
✅ Pago encontrado en BD con ID: 1
🔄 Actualizando estado de PENDING a APPROVED
✅ Estado de pago actualizado en BD: 1 -> APPROVED
✅ Orden actualizada a CONFIRMED: 123
```

## 🚨 Solución de Problemas

### Problema: "No se crea nada en la base de datos"
**Solución**: Verifica que el endpoint `/api/payments/create-preference/{orderId}` esté funcionando correctamente. Ahora debería crear el registro automáticamente.

### Problema: "Error que pruebe otro método de pago"
**Causas posibles**:
1. **Token incorrecto**: Verifica que el token en `application.properties` sea válido
2. **URLs incorrectas**: Asegúrate de usar ngrok para webhooks
3. **Datos de tarjeta**: Usa exactamente las tarjetas de prueba mencionadas

### Problema: "Webhook no se ejecuta"
**Solución**: 
1. Usa ngrok para exponer tu aplicación
2. Actualiza la URL de notificación en `application.properties`
3. Verifica que el endpoint `/api/mercadopago/webhook` esté accesible

## 📊 Estados de Pago

- `PENDING`: Pago creado, esperando confirmación
- `IN_PROCESS`: Pago en proceso
- `APPROVED`: Pago aprobado
- `REJECTED`: Pago rechazado
- `CANCELLED`: Pago cancelado

## 🔄 Flujo Completo

1. **Frontend** → `POST /api/payments/create-preference/{orderId}`
2. **Backend** → Crea preferencia en MercadoPago + guarda Payment en BD
3. **Frontend** → Redirige usuario a MercadoPago
4. **Usuario** → Completa pago con tarjeta de prueba
5. **MercadoPago** → Envía webhook a tu aplicación
6. **Backend** → Actualiza estado del pago en BD
7. **Frontend** → Usuario regresa a success/failure/pending

¡El flujo ahora debería funcionar correctamente! 🎉
