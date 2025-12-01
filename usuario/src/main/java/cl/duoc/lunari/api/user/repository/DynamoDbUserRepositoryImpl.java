package cl.duoc.lunari.api.user.repository;

import cl.duoc.lunari.api.user.model.User;
import cl.duoc.lunari.api.user.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del repositorio de usuarios usando DynamoDB Enhanced Client.
 *
 * Operaciones implementadas:
 * - CRUD básico: save, findById, findByEmail, delete, exists
 * - Queries con GSI: findByRoleName, findByRoleNameAndActive
 * - Scans con filtros: findByActive, findUsersWithFilters, searchUsers
 * - Paginación: Todas las operaciones de listado soportan paginación con tokens
 * - Conteo: count, countByActive, countByRoleName
 */
@Repository
@Slf4j
public class DynamoDbUserRepositoryImpl implements UserRepository {

    private final DynamoDbTable<User> userTable;
    private final DynamoDbIndex<User> emailIndex;
    private final DynamoDbIndex<User> usernameIndex;

    @Value("${app.pagination.defaultLimit:10}")
    private int defaultLimit;

    @Value("${app.pagination.maxLimit:100}")
    private int maxLimit;

    @Autowired
    public DynamoDbUserRepositoryImpl(
            DynamoDbEnhancedClient enhancedClient,
            @Value("${aws.dynamodb.tableName}") String tableName) {

        this.userTable = enhancedClient.table(tableName, TableSchema.fromBean(User.class));
        this.emailIndex = userTable.index("EmailIndex");
        this.usernameIndex = userTable.index("UsernameIndex");

        log.info("DynamoDbUserRepository initialized with table: {} (Email and Username indexes)", tableName);
    }

    // ==================== Operaciones CRUD Básicas ====================

    @Override
    public User save(User user) {
        try {
            userTable.putItem(user);
            log.debug("User saved successfully: {}", user.getId());
            return user;
        } catch (Exception e) {
            log.error("Error saving user: {}", user.getId(), e);
            throw new RuntimeException("Error al guardar usuario", e);
        }
    }

    @Override
    public Optional<User> findById(String userId) {
        try {
            Key key = Key.builder()
                .partitionValue(userId)
                .build();

            User user = userTable.getItem(key);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            log.error("Error finding user by id: {}", userId, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try {
            QueryConditional queryConditional = QueryConditional.keyEqualTo(
                Key.builder().partitionValue(email).build()
            );

            QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional)
                .limit(1)
                .build();

            return emailIndex.query(request)
                .stream()
                .flatMap(page -> page.items().stream())
                .findFirst();
        } catch (Exception e) {
            log.error("Error finding user by email: {}", email, e);
            return Optional.empty();
        }
    }

    @Override
    public void deleteById(String userId) {
        try {
            Key key = Key.builder()
                .partitionValue(userId)
                .build();

            userTable.deleteItem(key);
            log.debug("User deleted: {}", userId);
        } catch (Exception e) {
            log.error("Error deleting user: {}", userId, e);
            throw new RuntimeException("Error al eliminar usuario", e);
        }
    }

    @Override
    public boolean existsById(String userId) {
        return findById(userId).isPresent();
    }

    // ==================== Operaciones de Listado y Paginación ====================

    @Override
    public DynamoDbPage<User> findAll(int limit, String paginationToken) {
        try {
            limit = validateLimit(limit);

            ScanEnhancedRequest.Builder requestBuilder = ScanEnhancedRequest.builder()
                .limit(limit);

            // Agregar token de paginación si existe
            if (paginationToken != null && !paginationToken.isEmpty()) {
                Map<String, AttributeValue> exclusiveStartKey = PaginationUtil.decodePaginationToken(paginationToken);
                requestBuilder.exclusiveStartKey(exclusiveStartKey);
            }

            Iterator<Page<User>> pages = userTable.scan(requestBuilder.build()).iterator();

            if (pages.hasNext()) {
                Page<User> page = pages.next();
                List<User> users = page.items();
                String nextToken = PaginationUtil.encodePaginationToken(page.lastEvaluatedKey());

                return new DynamoDbPage<>(users, nextToken, limit);
            }

            return new DynamoDbPage<>(Collections.emptyList(), limit);
        } catch (Exception e) {
            log.error("Error in findAll", e);
            throw new RuntimeException("Error al listar usuarios", e);
        }
    }

    @Override
    public DynamoDbPage<User> findByRoleName(String roleName, int limit, String paginationToken) {
        // Roles removed from e-commerce client model
        throw new UnsupportedOperationException("Role-based queries are not supported for e-commerce clients");
    }

    @Override
    public DynamoDbPage<User> findByRoleNameAndActive(String roleName, Boolean active, int limit, String paginationToken) {
        // Roles removed from e-commerce client model
        throw new UnsupportedOperationException("Role-based queries are not supported for e-commerce clients");
    }

    @Override
    public DynamoDbPage<User> findByActive(Boolean active, int limit, String paginationToken) {
        try {
            limit = validateLimit(limit);

            // Usar scan con filter expression
            Map<String, AttributeValue> expressionValues = new HashMap<>();
            expressionValues.put(":active", AttributeValue.builder().bool(active).build());

            ScanEnhancedRequest.Builder requestBuilder = ScanEnhancedRequest.builder()
                .limit(limit)
                .filterExpression(
                    Expression.builder()
                        .expression("isActive = :active")
                        .expressionValues(expressionValues)
                        .build()
                );

            // Agregar token de paginación si existe
            if (paginationToken != null && !paginationToken.isEmpty()) {
                Map<String, AttributeValue> exclusiveStartKey = PaginationUtil.decodePaginationToken(paginationToken);
                requestBuilder.exclusiveStartKey(exclusiveStartKey);
            }

            Iterator<Page<User>> pages = userTable.scan(requestBuilder.build()).iterator();

            if (pages.hasNext()) {
                Page<User> page = pages.next();
                List<User> users = page.items();
                String nextToken = PaginationUtil.encodePaginationToken(page.lastEvaluatedKey());

                return new DynamoDbPage<>(users, nextToken, limit);
            }

            return new DynamoDbPage<>(Collections.emptyList(), limit);
        } catch (Exception e) {
            log.error("Error in findByActive: {}", active, e);
            throw new RuntimeException("Error al buscar usuarios por estado", e);
        }
    }

    @Override
    public DynamoDbPage<User> findUsersWithFilters(Boolean active, String roleName, int limit, String paginationToken) {
        try {
            limit = validateLimit(limit);

            // Construir filter expression dinámicamente
            List<String> conditions = new ArrayList<>();
            Map<String, AttributeValue> expressionValues = new HashMap<>();

            if (active != null) {
                conditions.add("isActive = :active");
                expressionValues.put(":active", AttributeValue.builder().bool(active).build());
            }

            if (roleName != null && !roleName.isEmpty()) {
                conditions.add("#role.#roleName = :roleName");
                expressionValues.put(":roleName", AttributeValue.builder().s(roleName).build());
            }

            ScanEnhancedRequest.Builder requestBuilder = ScanEnhancedRequest.builder()
                .limit(limit);

            if (!conditions.isEmpty()) {
                String filterExpression = String.join(" AND ", conditions);

                Map<String, String> expressionNames = new HashMap<>();
                if (roleName != null) {
                    expressionNames.put("#role", "role");
                    expressionNames.put("#roleName", "roleName");
                }

                requestBuilder.filterExpression(
                    Expression.builder()
                        .expression(filterExpression)
                        .expressionValues(expressionValues)
                        .expressionNames(expressionNames)
                        .build()
                );
            }

            // Agregar token de paginación si existe
            if (paginationToken != null && !paginationToken.isEmpty()) {
                Map<String, AttributeValue> exclusiveStartKey = PaginationUtil.decodePaginationToken(paginationToken);
                requestBuilder.exclusiveStartKey(exclusiveStartKey);
            }

            Iterator<Page<User>> pages = userTable.scan(requestBuilder.build()).iterator();

            if (pages.hasNext()) {
                Page<User> page = pages.next();
                List<User> users = page.items();
                String nextToken = PaginationUtil.encodePaginationToken(page.lastEvaluatedKey());

                return new DynamoDbPage<>(users, nextToken, limit);
            }

            return new DynamoDbPage<>(Collections.emptyList(), limit);
        } catch (Exception e) {
            log.error("Error in findUsersWithFilters", e);
            throw new RuntimeException("Error al buscar usuarios con filtros", e);
        }
    }

    @Override
    public DynamoDbPage<User> searchUsers(String query, int limit, String paginationToken) {
        try {
            limit = validateLimit(limit);

            String lowerQuery = query.toLowerCase();

            // Scan y filtrar en memoria (para case-insensitive contains)
            // DynamoDB no soporta case-insensitive LIKE nativamente
            ScanEnhancedRequest.Builder requestBuilder = ScanEnhancedRequest.builder()
                .limit(limit * 3); // Buscar más para compensar el filtrado en memoria

            // Agregar token de paginación si existe
            if (paginationToken != null && !paginationToken.isEmpty()) {
                Map<String, AttributeValue> exclusiveStartKey = PaginationUtil.decodePaginationToken(paginationToken);
                requestBuilder.exclusiveStartKey(exclusiveStartKey);
            }

            Iterator<Page<User>> pages = userTable.scan(requestBuilder.build()).iterator();

            if (pages.hasNext()) {
                Page<User> page = pages.next();

                // Filtrar en memoria para búsqueda case-insensitive
                List<User> filteredUsers = page.items().stream()
                    .filter(user -> {
                        String lowerUsername = user.getUsername() != null ? user.getUsername().toLowerCase() : "";
                        String lowerEmail = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
                        String lowerFirstName = (user.getPersonal() != null && user.getPersonal().getFirstName() != null)
                                ? user.getPersonal().getFirstName().toLowerCase() : "";
                        String lowerLastName = (user.getPersonal() != null && user.getPersonal().getLastName() != null)
                                ? user.getPersonal().getLastName().toLowerCase() : "";

                        return lowerUsername.contains(lowerQuery) ||
                               lowerEmail.contains(lowerQuery) ||
                               lowerFirstName.contains(lowerQuery) ||
                               lowerLastName.contains(lowerQuery);
                    })
                    .limit(limit)
                    .collect(Collectors.toList());

                String nextToken = PaginationUtil.encodePaginationToken(page.lastEvaluatedKey());

                return new DynamoDbPage<>(filteredUsers, nextToken, limit);
            }

            return new DynamoDbPage<>(Collections.emptyList(), limit);
        } catch (Exception e) {
            log.error("Error in searchUsers: {}", query, e);
            throw new RuntimeException("Error al buscar usuarios", e);
        }
    }

    // ==================== Operaciones de Conteo ====================

    @Override
    public long count() {
        try {
            long count = 0;
            Iterator<Page<User>> pages = userTable.scan().iterator();

            while (pages.hasNext()) {
                count += pages.next().items().size();
            }

            return count;
        } catch (Exception e) {
            log.error("Error counting users", e);
            return 0;
        }
    }

    @Override
    public long countByActive(Boolean active) {
        try {
            Map<String, AttributeValue> expressionValues = new HashMap<>();
            expressionValues.put(":active", AttributeValue.builder().bool(active).build());

            ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .filterExpression(
                    Expression.builder()
                        .expression("isActive = :active")
                        .expressionValues(expressionValues)
                        .build()
                )
                .build();

            long count = 0;
            Iterator<Page<User>> pages = userTable.scan(request).iterator();

            while (pages.hasNext()) {
                count += pages.next().items().size();
            }

            return count;
        } catch (Exception e) {
            log.error("Error counting users by active: {}", active, e);
            return 0;
        }
    }

    @Override
    public long countByRoleName(String roleName) {
        // Roles removed from e-commerce client model
        throw new UnsupportedOperationException("Role-based queries are not supported for e-commerce clients");
    }

    // ==================== Helper Methods ====================

    /**
     * Valida y ajusta el límite de paginación.
     *
     * @param limit Límite solicitado
     * @return Límite validado
     */
    private int validateLimit(int limit) {
        if (limit <= 0) {
            return defaultLimit;
        }
        return Math.min(limit, maxLimit);
    }
}
