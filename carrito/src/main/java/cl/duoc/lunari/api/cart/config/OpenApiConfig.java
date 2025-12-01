package cl.duoc.lunari.api.cart.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port}")
    private String serverPort;

    @Bean
    public OpenAPI lunariCartOpenAPI() {
        Server localServer = new Server()
                .url("http://localhost:" + serverPort)
                .description("Local Development Server");

        Server devServer = new Server()
                .url("https://dev-api.lunari.cl/cart")
                .description("Development Server");

        Server prodServer = new Server()
                .url("https://api.lunari.cl/cart")
                .description("Production Server");

        Contact contact = new Contact()
                .name("LUNARi Team")
                .email("osca.munozs@duocuc.cl")
                .url("https://github.com/dsy1103g5/lunari");

        License license = new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT");

        Info info = new Info()
                .title("LUNARi Cart & Checkout API")
                .description("""
                    API REST para gestión de carritos de compra, pedidos y procesamiento de pagos.

                    ## Características principales:
                    - **Gestión de Carritos**: Crear y administrar carritos de compra
                    - **Proceso de Checkout**: Flujo completo de pago con Transbank WebPay Plus
                    - **Gestión de Pedidos**: Seguimiento de pedidos y estados
                    - **Consultas de Pagos**: Verificación de estado de transacciones

                    ## Flujo de Checkout:
                    1. Usuario agrega items al carrito (/api/v1/cart)
                    2. Inicia checkout (/api/v1/checkout/initiate)
                    3. Redirección a Transbank para pago
                    4. Confirmación de pago (/api/v1/checkout/confirm)
                    5. Procesamiento asíncrono (reducción stock, asignación puntos)

                    ## Integración:
                    - **Inventario Service**: Sincronización de stock de productos
                    - **Usuario Service**: Sistema de puntos de lealtad
                    - **Transbank**: Procesamiento de pagos WebPay Plus

                    ## Versión: 1.0.0
                    """)
                .version("1.0.0")
                .contact(contact)
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer, devServer, prodServer));
    }
}