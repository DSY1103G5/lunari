package cl.duoc.lunari.api.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.net.URI;

/**
 * Configuración de AWS DynamoDB para el microservicio de usuario.
 *
 * Proporciona beans para:
 * - DynamoDbClient: Cliente de bajo nivel para operaciones de DynamoDB
 * - DynamoDbEnhancedClient: Cliente de alto nivel con mapeo objeto-documento
 *
 * Soporta configuración para:
 * - AWS DynamoDB en producción
 * - DynamoDB Local para desarrollo y testing
 */
@Configuration
public class DynamoDbConfig {

    @Value("${aws.region:us-east-1}")
    private String awsRegion;

    @Value("${aws.dynamodb.endpoint:}")
    private String dynamoDbEndpoint;

    @Value("${aws.accessKeyId:}")
    private String accessKeyId;

    @Value("${aws.secretKey:}")
    private String secretKey;

    /**
     * Crea el cliente de DynamoDB configurado para AWS o DynamoDB Local.
     *
     * Para DynamoDB Local:
     * - Requiere aws.dynamodb.endpoint configurado (ej: http://localhost:8000)
     * - Requiere credenciales ficticias en accessKeyId y secretKey
     *
     * Para AWS DynamoDB:
     * - aws.dynamodb.endpoint debe estar vacío o no configurado
     * - Usará las credenciales configuradas o perfil por defecto
     *
     * @return DynamoDbClient configurado
     */
    @Bean
    public DynamoDbClient dynamoDbClient() {
        DynamoDbClientBuilder builder = DynamoDbClient.builder()
            .region(Region.of(awsRegion));

        // Configuración para DynamoDB Local (desarrollo y testing)
        if (!dynamoDbEndpoint.isEmpty()) {
            builder.endpointOverride(URI.create(dynamoDbEndpoint));

            // DynamoDB Local requiere credenciales (pueden ser ficticias)
            if (!accessKeyId.isEmpty() && !secretKey.isEmpty()) {
                AwsBasicCredentials credentials = AwsBasicCredentials.create(
                    accessKeyId,
                    secretKey
                );
                builder.credentialsProvider(StaticCredentialsProvider.create(credentials));
            }
        }

        return builder.build();
    }

    /**
     * Crea el cliente Enhanced de DynamoDB para mapeo objeto-documento.
     *
     * El Enhanced Client proporciona:
     * - Mapeo automático entre objetos Java y items de DynamoDB
     * - API de alto nivel para operaciones CRUD
     * - Soporte para consultas y escaneos con paginación
     * - Manejo automático de índices secundarios (GSI/LSI)
     *
     * @param dynamoDbClient Cliente de DynamoDB de bajo nivel
     * @return DynamoDbEnhancedClient para operaciones ORM-like
     */
    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
            .dynamoDbClient(dynamoDbClient)
            .build();
    }
}
