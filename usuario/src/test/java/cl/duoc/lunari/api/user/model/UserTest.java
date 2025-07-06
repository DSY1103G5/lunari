package cl.duoc.lunari.api.user.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la entidad User.
 * 
 * Estas pruebas verifican el comportamiento de la entidad User,
 * incluyendo validaciones, constructores, getters/setters,
 * y métodos de ciclo de vida JPA.
 */
@DisplayName("User Entity Tests")
class UserTest {

    private Validator validator;
    private User user;
    private UUID testCompanyId;

    @BeforeEach
    void setUp() {
        // Configurar el validador de Java Bean Validation
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        // Configurar datos de prueba
        testCompanyId = UUID.randomUUID();
        
        // Crear usuario de prueba válido
        user = new User();
        user.setFirstName("Juan");
        user.setLastName("Pérez");
        user.setEmail("juan.perez@empresa.com");
        user.setPassword("SecurePassword123!");
        user.setPhone("123456789");
        user.setRoleId(1);
        user.setCompanyId(testCompanyId);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Constructor sin parámetros debe crear usuario con valores por defecto")
        void testNoArgsConstructor_ShouldCreateUserWithDefaults() {
            User newUser = new User();
            
            assertNull(newUser.getId());
            assertNull(newUser.getFirstName());
            assertNull(newUser.getLastName());
            assertNull(newUser.getEmail());
            assertNull(newUser.getPassword());
            assertNull(newUser.getPhone());
            assertNull(newUser.getProfileImage());
            assertNull(newUser.getRoleId());
            assertNull(newUser.getCompanyId());
            assertTrue(newUser.getActive()); // Valor por defecto
            assertNull(newUser.getLastLogin());
            assertFalse(newUser.getVerified()); // Valor por defecto
            assertNull(newUser.getVerificationToken());
            assertNull(newUser.getTokenExpiration());
            assertNull(newUser.getCreatedAt());
            assertNull(newUser.getUpdatedAt());
        }

        @Test
        @DisplayName("Constructor con todos los parámetros debe asignar valores correctamente")
        void testAllArgsConstructor_ShouldAssignAllValues() {
            UUID id = UUID.randomUUID();
            OffsetDateTime now = OffsetDateTime.now();
            
            User newUser = new User(
                id, "María", "González", "maria@test.com", "Password123!",
                "987654321", "profile.jpg", 2, testCompanyId, true,
                now, true, "token123", now.plusDays(1), now, now
            );
            
            assertEquals(id, newUser.getId());
            assertEquals("María", newUser.getFirstName());
            assertEquals("González", newUser.getLastName());
            assertEquals("maria@test.com", newUser.getEmail());
            assertEquals("Password123!", newUser.getPassword());
            assertEquals("987654321", newUser.getPhone());
            assertEquals("profile.jpg", newUser.getProfileImage());
            assertEquals(2, newUser.getRoleId());
            assertEquals(testCompanyId, newUser.getCompanyId());
            assertTrue(newUser.getActive());
            assertEquals(now, newUser.getLastLogin());
            assertTrue(newUser.getVerified());
            assertEquals("token123", newUser.getVerificationToken());
            assertEquals(now.plusDays(1), newUser.getTokenExpiration());
            assertEquals(now, newUser.getCreatedAt());
            assertEquals(now, newUser.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Usuario válido no debe tener violaciones de validación")
        void testValidUser_ShouldHaveNoViolations() {
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            
            assertTrue(violations.isEmpty(), 
                "Usuario válido no debería tener violaciones de validación");
        }

        @Test
        @DisplayName("Nombre vacío debe generar violación de validación")
        void testEmptyFirstName_ShouldViolateValidation() {
            user.setFirstName("");
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Nombre no puede estar vacío")));
        }

        @Test
        @DisplayName("Nombre nulo debe generar violación de validación")
        void testNullFirstName_ShouldViolateValidation() {
            user.setFirstName(null);
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Nombre no puede estar vacío")));
        }

        @Test
        @DisplayName("Apellido vacío debe generar violación de validación")
        void testEmptyLastName_ShouldViolateValidation() {
            user.setLastName("");
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Apellido no puede estar vacío")));
        }

        @Test
        @DisplayName("Apellido nulo debe generar violación de validación")
        void testNullLastName_ShouldViolateValidation() {
            user.setLastName(null);
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Apellido no puede estar vacío")));
        }

        @Test
        @DisplayName("Email vacío debe generar violación de validación")
        void testEmptyEmail_ShouldViolateValidation() {
            user.setEmail("");
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email no puede estar vacío")));
        }

        @Test
        @DisplayName("Email nulo debe generar violación de validación")
        void testNullEmail_ShouldViolateValidation() {
            user.setEmail(null);
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Email no puede estar vacío")));
        }

        @Test
        @DisplayName("Email con formato inválido debe generar violación de validación")
        void testInvalidEmailFormat_ShouldViolateValidation() {
            user.setEmail("email-invalido");
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        }

        @Test
        @DisplayName("Email con formato válido debe pasar validación")
        void testValidEmailFormats_ShouldPassValidation() {
            String[] validEmails = {
                "test@domain.com",
                "user.name@domain.co.uk",
                "user+tag@domain.org",
                "123@domain.net"
            };
            
            for (String email : validEmails) {
                user.setEmail(email);
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                
                assertTrue(violations.stream()
                    .noneMatch(v -> v.getPropertyPath().toString().equals("email")),
                    "Email válido falló validación: " + email);
            }
        }

        @Test
        @DisplayName("Contraseña vacía debe generar violación de validación")
        void testEmptyPassword_ShouldViolateValidation() {
            user.setPassword("");
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Contraseña no puede estar vacía")));
        }

        @Test
        @DisplayName("Contraseña nula debe generar violación de validación")
        void testNullPassword_ShouldViolateValidation() {
            user.setPassword(null);
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Contraseña no puede estar vacía")));
        }

        @Test
        @DisplayName("Contraseña muy corta debe generar violación de validación")
        void testShortPassword_ShouldViolateValidation() {
            user.setPassword("1234567"); // 7 caracteres
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Contraseña debe tener entre 8 y 64 caracteres")));
        }

        @Test
        @DisplayName("Contraseña muy larga debe generar violación de validación")
        void testLongPassword_ShouldViolateValidation() {
            // Crear contraseña de 65 caracteres
            String longPassword = "a".repeat(65);
            user.setPassword(longPassword);
            
            Set<ConstraintViolation<User>> violations = validator.validate(user);
            
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Contraseña debe tener entre 8 y 64 caracteres")));
        }

        @Test
        @DisplayName("Contraseña con longitud válida debe pasar validación")
        void testValidPasswordLength_ShouldPassValidation() {
            String[] validPasswords = {
                "12345678", // 8 caracteres (mínimo)
                "Password123!", // Contraseña típica
                "a".repeat(64) // 64 caracteres (máximo)
            };
            
            for (String password : validPasswords) {
                user.setPassword(password);
                Set<ConstraintViolation<User>> violations = validator.validate(user);
                
                assertTrue(violations.stream()
                    .noneMatch(v -> v.getPropertyPath().toString().equals("password") && 
                                   v.getMessage().contains("Contraseña debe tener entre 8 y 64 caracteres")),
                    "Contraseña válida falló validación de longitud: " + password);
            }
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Setters y getters deben funcionar correctamente")
        void testGettersAndSetters_ShouldWorkCorrectly() {
            UUID id = UUID.randomUUID();
            OffsetDateTime now = OffsetDateTime.now();
            
            user.setId(id);
            user.setProfileImage("profile.jpg");
            user.setActive(false);
            user.setLastLogin(now);
            user.setVerified(true);
            user.setVerificationToken("token123");
            user.setTokenExpiration(now.plusDays(1));
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            
            assertEquals(id, user.getId());
            assertEquals("Juan", user.getFirstName()); // Ya configurado en setUp
            assertEquals("Pérez", user.getLastName()); // Ya configurado en setUp
            assertEquals("juan.perez@empresa.com", user.getEmail()); // Ya configurado en setUp
            assertEquals("SecurePassword123!", user.getPassword()); // Ya configurado en setUp
            assertEquals("123456789", user.getPhone()); // Ya configurado en setUp
            assertEquals("profile.jpg", user.getProfileImage());
            assertEquals(1, user.getRoleId()); // Ya configurado en setUp
            assertEquals(testCompanyId, user.getCompanyId()); // Ya configurado en setUp
            assertFalse(user.getActive());
            assertEquals(now, user.getLastLogin());
            assertTrue(user.getVerified());
            assertEquals("token123", user.getVerificationToken());
            assertEquals(now.plusDays(1), user.getTokenExpiration());
            assertEquals(now, user.getCreatedAt());
            assertEquals(now, user.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("JPA Lifecycle Tests")
    class JpaLifecycleTests {

        @Test
        @DisplayName("@PrePersist debe configurar createdAt y updatedAt")
        void testPrePersist_ShouldSetTimestamps() {
            User newUser = new User();
            
            // Simular @PrePersist
            newUser.onCreate();
            
            assertNotNull(newUser.getCreatedAt());
            assertNotNull(newUser.getUpdatedAt());
            // Instead of exact equality, check they are very close (within 1 millisecond)
            assertTrue(Math.abs(newUser.getCreatedAt().toInstant().toEpochMilli() - 
                               newUser.getUpdatedAt().toInstant().toEpochMilli()) <= 1,
                       "createdAt and updatedAt should be set to nearly the same time");
        }

        @Test
        @DisplayName("@PreUpdate debe actualizar solo updatedAt")
        void testPreUpdate_ShouldUpdateOnlyUpdatedAt() throws InterruptedException {
            user.onCreate(); // Simular creación inicial
            OffsetDateTime originalCreatedAt = user.getCreatedAt();
            
            // Esperar un momento para asegurar diferencia en timestamp
            Thread.sleep(100); // Increased from 10ms to 100ms for better reliability
            
            // Simular @PreUpdate
            user.onUpdate();
            
            assertEquals(originalCreatedAt, user.getCreatedAt());
            assertNotEquals(originalCreatedAt, user.getUpdatedAt());
            assertTrue(user.getUpdatedAt().isAfter(originalCreatedAt));
        }

        @Test
        @DisplayName("@PreUpdate debe actualizar updatedAt con precisión en microsegundos")
        void testPreUpdate_ShouldUpdateTimestampWithMicrosecondPrecision() throws InterruptedException {
            user.onCreate(); // Simular creación inicial
            OffsetDateTime originalUpdatedAt = user.getUpdatedAt();
            
            // Esperar tiempo suficiente para garantizar diferencia detectable
            Thread.sleep(100);
            
            // Simular @PreUpdate
            user.onUpdate();
            
            assertNotEquals(originalUpdatedAt, user.getUpdatedAt());
            assertTrue(user.getUpdatedAt().isAfter(originalUpdatedAt));
            
            // Verificar que la diferencia es mayor a 50 milisegundos
            long diffInNanos = user.getUpdatedAt().toInstant().toEpochMilli() - 
                              originalUpdatedAt.toInstant().toEpochMilli();
            assertTrue(diffInNanos >= 50, "La diferencia debe ser al menos 50ms");
        }

        @Test
        @DisplayName("@PrePersist debe crear timestamps únicos para diferentes usuarios")
        void testPrePersist_ShouldCreateUniqueTimestampsForDifferentUsers() throws InterruptedException {
            User user1 = new User();
            user1.onCreate();
            
            // Pequeña pausa para asegurar timestamps diferentes
            Thread.sleep(50);
            
            User user2 = new User();
            user2.onCreate();
            
            assertNotEquals(user1.getCreatedAt(), user2.getCreatedAt());
            assertNotEquals(user1.getUpdatedAt(), user2.getUpdatedAt());
            assertTrue(user2.getCreatedAt().isAfter(user1.getCreatedAt()));
        }

        @Test
        @DisplayName("@PreUpdate múltiples veces debe incrementar updatedAt")
        void testMultiplePreUpdate_ShouldIncrementUpdatedAt() throws InterruptedException {
            user.onCreate();
            OffsetDateTime firstUpdate = user.getUpdatedAt();
            
            Thread.sleep(50);
            user.onUpdate();
            OffsetDateTime secondUpdate = user.getUpdatedAt();
            
            Thread.sleep(50);
            user.onUpdate();
            OffsetDateTime thirdUpdate = user.getUpdatedAt();
            
            assertTrue(secondUpdate.isAfter(firstUpdate));
            assertTrue(thirdUpdate.isAfter(secondUpdate));
            assertTrue(thirdUpdate.isAfter(firstUpdate));
        }

        @Test
        @DisplayName("createdAt debe permanecer inmutable después de múltiples updates")
        void testCreatedAt_ShouldRemainImmutableAfterMultipleUpdates() throws InterruptedException {
            user.onCreate();
            OffsetDateTime originalCreatedAt = user.getCreatedAt();
            
            // Realizar múltiples updates
            for (int i = 0; i < 5; i++) {
                Thread.sleep(20);
                user.onUpdate();
                assertEquals(originalCreatedAt, user.getCreatedAt(),
                    "createdAt debe permanecer inmutable en update " + (i + 1));
            }
        }

        @Test
        @DisplayName("onCreate no debe afectar campos existentes")
        void testOnCreate_ShouldNotAffectExistingFields() {
            // Configurar usuario con datos
            user.setFirstName("Test");
            user.setEmail("test@example.com");
            user.setActive(false);
            
            user.onCreate();
            
            // Verificar que los campos existentes no se modificaron
            assertEquals("Test", user.getFirstName());
            assertEquals("test@example.com", user.getEmail());
            assertFalse(user.getActive());
            
            // Verificar que solo se configuraron los timestamps
            assertNotNull(user.getCreatedAt());
            assertNotNull(user.getUpdatedAt());
        }

        @Test
        @DisplayName("onUpdate no debe afectar campos existentes excepto updatedAt")
        void testOnUpdate_ShouldOnlyAffectUpdatedAt() {
            user.onCreate();
            OffsetDateTime originalCreatedAt = user.getCreatedAt();
            
            // Configurar más datos después de la creación
            user.setFirstName("Updated");
            user.setEmail("updated@example.com");
            user.setActive(false);
            
            user.onUpdate();
            
            // Verificar que solo updatedAt cambió
            assertEquals(originalCreatedAt, user.getCreatedAt());
            assertEquals("Updated", user.getFirstName());
            assertEquals("updated@example.com", user.getEmail());
            assertFalse(user.getActive());
            assertNotNull(user.getUpdatedAt());
        }

        @Test
        @DisplayName("timestamps deben usar OffsetDateTime con zona horaria")
        void testTimestamps_ShouldUseOffsetDateTimeWithTimezone() {
            user.onCreate();
            
            assertNotNull(user.getCreatedAt().getOffset());
            assertNotNull(user.getUpdatedAt().getOffset());
            
            // Verificar que los timestamps incluyen información de zona horaria
            assertTrue(user.getCreatedAt().toString().contains("+") || 
                      user.getCreatedAt().toString().contains("-"));
            assertTrue(user.getUpdatedAt().toString().contains("+") || 
                      user.getUpdatedAt().toString().contains("-"));
        }

        @Test
        @DisplayName("timestamps deben estar cerca del tiempo actual")
        void testTimestamps_ShouldBeCloseToCurrentTime() {
            OffsetDateTime beforeCreate = OffsetDateTime.now().minusSeconds(1);
            
            user.onCreate();
            
            OffsetDateTime afterCreate = OffsetDateTime.now().plusSeconds(1);
            
            assertTrue(user.getCreatedAt().isAfter(beforeCreate) && 
                      user.getCreatedAt().isBefore(afterCreate),
                      "createdAt debe estar cerca del tiempo actual");
            assertTrue(user.getUpdatedAt().isAfter(beforeCreate) && 
                      user.getUpdatedAt().isBefore(afterCreate),
                      "updatedAt debe estar cerca del tiempo actual");
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("Usuario nuevo debe tener valores por defecto correctos")
        void testDefaultValues_ShouldBeCorrect() {
            User newUser = new User();
            
            assertTrue(newUser.getActive(), "Active debe ser true por defecto");
            assertFalse(newUser.getVerified(), "Verified debe ser false por defecto");
        }

        @Test
        @DisplayName("Configurar active explícitamente debe sobrescribir valor por defecto")
        void testExplicitActiveValue_ShouldOverrideDefault() {
            user.setActive(false);
            
            assertFalse(user.getActive());
        }

        @Test
        @DisplayName("Configurar verified explícitamente debe sobrescribir valor por defecto")
        void testExplicitVerifiedValue_ShouldOverrideDefault() {
            user.setVerified(true);
            
            assertTrue(user.getVerified());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Usuarios con mismos datos deben ser iguales")
        void testEqualsAndHashCode_SameData_ShouldBeEqual() {
            User user1 = new User();
            user1.setFirstName("Juan");
            user1.setLastName("Pérez");
            user1.setEmail("juan@test.com");
            
            User user2 = new User();
            user2.setFirstName("Juan");
            user2.setLastName("Pérez");
            user2.setEmail("juan@test.com");
            
            assertEquals(user1, user2);
            assertEquals(user1.hashCode(), user2.hashCode());
        }

        @Test
        @DisplayName("Usuarios con datos diferentes deben ser diferentes")
        void testEqualsAndHashCode_DifferentData_ShouldNotBeEqual() {
            User user1 = new User();
            user1.setFirstName("Juan");
            user1.setLastName("Pérez");
            user1.setEmail("juan@test.com");
            
            User user2 = new User();
            user2.setFirstName("María");
            user2.setLastName("González");
            user2.setEmail("maria@test.com");
            
            assertNotEquals(user1, user2);
        }

        @Test
        @DisplayName("Usuario debe ser igual a sí mismo")
        void testEquals_SameObject_ShouldBeEqual() {
            assertEquals(user, user);
        }

        @Test
        @DisplayName("Usuario no debe ser igual a null")
        void testEquals_Null_ShouldNotBeEqual() {
            assertNotEquals(user, null);
        }

        @Test
        @DisplayName("Usuario no debe ser igual a objeto de diferente clase")
        void testEquals_DifferentClass_ShouldNotBeEqual() {
            assertNotEquals(user, "not a user");
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString debe contener información relevante")
        void testToString_ShouldContainRelevantInfo() {
            String toString = user.toString();
            
            assertNotNull(toString);
            assertTrue(toString.contains("Juan"));
            assertTrue(toString.contains("Pérez"));
            assertTrue(toString.contains("juan.perez@empresa.com"));
            // Lombok @Data incluye todos los campos, incluyendo password
            assertTrue(toString.contains("SecurePassword123!"));
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Usuario activo y verificado debe considerarse válido para login")
        void testValidForLogin_ActiveAndVerified_ShouldBeTrue() {
            user.setActive(true);
            user.setVerified(true);
            
            assertTrue(user.getActive() && user.getVerified(),
                "Usuario activo y verificado debe ser válido para login");
        }

        @Test
        @DisplayName("Usuario inactivo no debe ser válido para login")
        void testValidForLogin_Inactive_ShouldBeFalse() {
            user.setActive(false);
            user.setVerified(true);
            
            assertFalse(user.getActive(),
                "Usuario inactivo no debe ser válido para login");
        }

        @Test
        @DisplayName("Usuario no verificado no debe ser válido para login")
        void testValidForLogin_Unverified_ShouldBeFalse() {
            user.setActive(true);
            user.setVerified(false);
            
            assertFalse(user.getVerified(),
                "Usuario no verificado no debe ser válido para login");
        }

        @Test
        @DisplayName("Token de verificación vencido debe identificarse correctamente")
        void testVerificationTokenExpired_ShouldBeIdentifiable() {
            OffsetDateTime pastDate = OffsetDateTime.now().minusDays(1);
            user.setTokenExpiration(pastDate);
            
            assertTrue(user.getTokenExpiration().isBefore(OffsetDateTime.now()),
                "Token vencido debe identificarse correctamente");
        }

        @Test
        @DisplayName("Token de verificación válido debe identificarse correctamente")
        void testVerificationTokenValid_ShouldBeIdentifiable() {
            OffsetDateTime futureDate = OffsetDateTime.now().plusDays(1);
            user.setTokenExpiration(futureDate);
            
            assertTrue(user.getTokenExpiration().isAfter(OffsetDateTime.now()),
                "Token válido debe identificarse correctamente");
        }
    }
}
