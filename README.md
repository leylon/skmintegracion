# skmintegracion

Este proyecto sirve como un middleware o puente para facilitar la comunicación entre la aplicación de punto de venta **Hiopos** y la pasarela de pagos **Izipay**. El objetivo principal es procesar transacciones de venta de manera fluida y automatizada.

## Flujo de Funcionamiento

El proceso de una transacción se realiza de la siguiente manera:

1.  **Inicio desde Hiopos**: El proceso de venta se inicia en la aplicación de Hiopos.
2.  **Envío de Datos**: Hiopos envía los datos de la transacción (monto, detalles del pedido, etc.) a este servicio de integración (`skmintegracion`).
3.  **Procesamiento y Redirección**: La aplicación procesa los datos recibidos y los formatea según los requerimientos de Izipay. Luego, envía la solicitud de pago a la pasarela de Izipay.
4.  **Respuesta de Izipay**: Una vez que el pago es completado (o rechazado) en Izipay, la pasarela envía una respuesta a este servicio.
5.  **Notificación a Hiopos**: Finalmente, `skmintegracion` recibe la respuesta de Izipay, la interpreta y la reenvía a Hiopos para actualizar el estado de la venta (aprobada, denegada, etc.).

## Diagrama del Flujo

```mermaid
sequenceDiagram
    participant Hiopos
    participant skmintegracion as SkmIntegración
    participant Izipay

    Note over Hiopos: 1. Inicio de Venta
    Hiopos->>skmintegracion: 2. Envío de Datos (monto, pedido, etc.)
    
    Note over skmintegracion: 3. Procesamiento y Formateo (Req. Izipay)
    skmintegracion->>Izipay: 3. Envío de solicitud de pago
    
    Izipay-->>skmintegracion: 4. Respuesta de Izipay (Pago completado/rechazado)
    
    Note over skmintegracion: 5. Interpretación de respuesta
    skmintegracion-->>Hiopos: 5. Notificación a Hiopos (Estado de la venta)
```
