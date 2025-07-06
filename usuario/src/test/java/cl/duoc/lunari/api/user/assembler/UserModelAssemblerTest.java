package cl.duoc.lunari.api.user.assembler;

import cl.duoc.lunari.api.user.controller.UserController;
import cl.duoc.lunari.api.user.dto.UserMapper;
import cl.duoc.lunari.api.user.dto.UserRepresentation;
import cl.duoc.lunari.api.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.Link;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para UserModelAssembler.
 * 
 * Estas pruebas verifican que el assembler convierte correctamente
 * las entidades User a UserRepresentation con los enlaces HATEOAS apropiados.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserModelAssembler Tests")
class UserModelAssemblerTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserModelAssembler userModelAssembler;

    private User testUser;
    private UserRepresentation testUserRepresentation;
    private UUID userId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        companyId = UUID.randomUUID();
        
        // Configurar usuario de prueba
        testUser = new User();
        testUser.setId(userId);
        testUser.setFirstName("Juan");
        testUser.setLastName("Pérez");
        testUser.setEmail("juan.perez@empresa.com");
        testUser.setPhone("123456789");
        testUser.setProfileImage("profile.jpg");
        testUser.setRoleId(1);
        testUser.setCompanyId(companyId);
        testUser.setActive(true);
        testUser.setVerified(true);
        testUser.setLastLogin(OffsetDateTime.now());
        testUser.setCreatedAt(OffsetDateTime.now());
        testUser.setUpdatedAt(OffsetDateTime.now());

        // Configurar representación de prueba
        testUserRepresentation = new UserRepresentation();
        testUserRepresentation.setId(userId);
        testUserRepresentation.setFirstName("Juan");
        testUserRepresentation.setLastName("Pérez");
        testUserRepresentation.setEmail("juan.perez@empresa.com");
        testUserRepresentation.setPhone("123456789");
        testUserRepresentation.setProfileImage("profile.jpg");
        testUserRepresentation.setRoleId(1);
        testUserRepresentation.setCompanyId(companyId);
        testUserRepresentation.setActive(true);
        testUserRepresentation.setVerified(true);
        testUserRepresentation.setLastLogin(testUser.getLastLogin());
        testUserRepresentation.setCreatedAt(testUser.getCreatedAt());
        testUserRepresentation.setUpdatedAt(testUser.getUpdatedAt());
    }

    @Nested
    @DisplayName("toModel Tests")
    class ToModelTests {

        @Test
        @DisplayName("Debe convertir User a UserRepresentation con datos básicos")
        void testToModel_ShouldConvertUserToRepresentation() {
            // Configurar mock
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(testUser);

            // Verificar datos básicos
            assertNotNull(result);
            assertEquals(userId, result.getId());
            assertEquals("Juan", result.getFirstName());
            assertEquals("Pérez", result.getLastName());
            assertEquals("juan.perez@empresa.com", result.getEmail());
            assertEquals("123456789", result.getPhone());
            assertEquals("profile.jpg", result.getProfileImage());
            assertEquals(1, result.getRoleId());
            assertEquals(companyId, result.getCompanyId());
            assertTrue(result.getActive());
            assertTrue(result.getVerified());
            assertNotNull(result.getLastLogin());
            assertNotNull(result.getCreatedAt());
            assertNotNull(result.getUpdatedAt());
        }

        @Test
        @DisplayName("Debe incluir enlace self correcto")
        void testToModel_ShouldIncludeSelfLink() {
            // Configurar mock
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(testUser);

            // Verificar enlace self
            assertTrue(result.hasLink("self"));
            Link selfLink = result.getRequiredLink("self");
            assertTrue(selfLink.getHref().contains("/api/v1/users/" + userId));
        }

        @Test
        @DisplayName("Debe incluir enlace a la colección de usuarios")
        void testToModel_ShouldIncludeUsersCollectionLink() {
            // Configurar mock
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(testUser);

            // Verificar enlace a usuarios
            assertTrue(result.hasLink("users"));
            Link usersLink = result.getRequiredLink("users");
            assertTrue(usersLink.getHref().contains("/api/v1/users"));
        }

        @Test
        @DisplayName("Debe incluir enlace de actualización")
        void testToModel_ShouldIncludeUpdateLink() {
            // Configurar mock
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(testUser);

            // Verificar enlace de actualización
            assertTrue(result.hasLink("update"));
            Link updateLink = result.getRequiredLink("update");
            assertTrue(updateLink.getHref().contains("/api/v1/users/" + userId));
        }

        @Test
        @DisplayName("Debe incluir enlace de eliminación")
        void testToModel_ShouldIncludeDeleteLink() {
            // Configurar mock
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(testUser);

            // Verificar enlace de eliminación
            assertTrue(result.hasLink("delete"));
            Link deleteLink = result.getRequiredLink("delete");
            assertTrue(deleteLink.getHref().contains("/api/v1/users/" + userId));
        }

        @Test
        @DisplayName("Debe incluir enlace de desactivación para usuario activo")
        void testToModel_ActiveUser_ShouldIncludeDeactivateLink() {
            // Configurar usuario activo
            testUser.setActive(true);
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(testUser);

            // Verificar enlace de desactivación
            assertTrue(result.hasLink("deactivate"));
            Link deactivateLink = result.getRequiredLink("deactivate");
            assertTrue(deactivateLink.getHref().contains("/api/v1/users/" + userId));
            assertTrue(deactivateLink.getHref().contains("false"));
        }

        @Test
        @DisplayName("Debe incluir enlace de activación para usuario inactivo")
        void testToModel_InactiveUser_ShouldIncludeActivateLink() {
            // Configurar usuario inactivo
            testUser.setActive(false);
            testUserRepresentation.setActive(false);
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(testUser);

            // Verificar enlace de activación
            assertTrue(result.hasLink("activate"));
            Link activateLink = result.getRequiredLink("activate");
            assertTrue(activateLink.getHref().contains("/api/v1/users/" + userId));
            assertTrue(activateLink.getHref().contains("true"));
        }

        @Test
        @DisplayName("No debe incluir enlaces de estado si active es null")
        void testToModel_NullActiveStatus_ShouldNotIncludeStatusLinks() {
            // Configurar usuario con estado nulo
            testUser.setActive(null);
            testUserRepresentation.setActive(null);
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(testUser);

            // Verificar que no hay enlaces de estado
            assertFalse(result.hasLink("activate"));
            assertFalse(result.hasLink("deactivate"));
        }

        @Test
        @DisplayName("Debe incluir enlace de cambio de contraseña")
        void testToModel_ShouldIncludeChangePasswordLink() {
            // Configurar mock
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(testUser);

            // Verificar enlace de cambio de contraseña
            assertTrue(result.hasLink("change-password"));
            Link changePasswordLink = result.getRequiredLink("change-password");
            assertTrue(changePasswordLink.getHref().contains("/api/v1/users/" + userId));
        }

        @Test
        @DisplayName("Debe incluir enlace a usuarios de la empresa si tiene companyId")
        void testToModel_WithCompanyId_ShouldIncludeCompanyUsersLink() {
            // Configurar mock
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(testUser);

            // Verificar enlace a usuarios de la empresa
            assertTrue(result.hasLink("company-users"));
            Link companyUsersLink = result.getRequiredLink("company-users");
            assertTrue(companyUsersLink.getHref().contains("/api/v1/users/company/" + companyId));
        }

        @Test
        @DisplayName("No debe incluir enlace a usuarios de la empresa si companyId es null")
        void testToModel_NullCompanyId_ShouldNotIncludeCompanyUsersLink() {
            // Configurar usuario sin empresa
            testUser.setCompanyId(null);
            testUserRepresentation.setCompanyId(null);
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(testUser);

            // Verificar que no hay enlace a usuarios de la empresa
            assertFalse(result.hasLink("company-users"));
        }

        @Test
        @DisplayName("Debe incluir enlaces relacionados con rol si tiene roleId")
        void testToModel_WithRoleId_ShouldIncludeRoleLinks() {
            // Configurar mock
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(testUser);

            // Verificar enlaces relacionados con rol
            assertTrue(result.hasLink("role-users"));
            assertTrue(result.hasLink("assign-role"));
            
            Link roleUsersLink = result.getRequiredLink("role-users");
            assertTrue(roleUsersLink.getHref().contains("/api/v1/users/role/1"));
            
            Link assignRoleLink = result.getRequiredLink("assign-role");
            assertTrue(assignRoleLink.getHref().contains("/api/v1/users/" + userId));
        }

        @Test
        @DisplayName("No debe incluir enlaces de rol si roleId es null")
        void testToModel_NullRoleId_ShouldNotIncludeRoleLinks() {
            // Configurar usuario sin rol
            testUser.setRoleId(null);
            testUserRepresentation.setRoleId(null);
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(testUser);

            // Verificar que no hay enlaces de rol
            assertFalse(result.hasLink("role-users"));
            assertFalse(result.hasLink("assign-role"));
        }

        @Test
        @DisplayName("Debe incluir enlace de verificación para usuario no verificado")
        void testToModel_UnverifiedUser_ShouldIncludeVerifyLink() {
            // Configurar usuario no verificado
            testUser.setVerified(false);
            testUserRepresentation.setVerified(false);
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(testUser);

            // Verificar enlace de verificación
            assertTrue(result.hasLink("verify"));
            Link verifyLink = result.getRequiredLink("verify");
            assertTrue(verifyLink.getHref().contains("/api/v1/users"));
        }

        @Test
        @DisplayName("No debe incluir enlace de verificación para usuario verificado")
        void testToModel_VerifiedUser_ShouldNotIncludeVerifyLink() {
            // Configurar usuario verificado
            testUser.setVerified(true);
            testUserRepresentation.setVerified(true);
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(testUser);

            // Verificar que no hay enlace de verificación
            assertFalse(result.hasLink("verify"));
        }

        @Test
        @DisplayName("No debe incluir enlace de verificación si verified es null")
        void testToModel_NullVerified_ShouldNotIncludeVerifyLink() {
            // Configurar usuario con verificación nula
            testUser.setVerified(null);
            testUserRepresentation.setVerified(null);
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(testUser);

            // Verificar que no hay enlace de verificación
            assertFalse(result.hasLink("verify"));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Debe manejar usuario con datos mínimos")
        void testToModel_MinimalUser_ShouldWork() {
            // Configurar usuario con datos mínimos
            User minimalUser = new User();
            minimalUser.setId(userId);
            minimalUser.setFirstName("Test");
            minimalUser.setLastName("User");
            minimalUser.setEmail("test@test.com");

            UserRepresentation minimalRepresentation = new UserRepresentation();
            minimalRepresentation.setId(userId);
            minimalRepresentation.setFirstName("Test");
            minimalRepresentation.setLastName("User");
            minimalRepresentation.setEmail("test@test.com");

            when(userMapper.toRepresentation(minimalUser)).thenReturn(minimalRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(minimalUser);

            // Verificar que funciona con datos mínimos
            assertNotNull(result);
            assertEquals(userId, result.getId());
            assertEquals("Test", result.getFirstName());
            assertEquals("User", result.getLastName());
            assertEquals("test@test.com", result.getEmail());
            
            // Verificar que al menos tiene enlaces básicos
            assertTrue(result.hasLink("self"));
            assertTrue(result.hasLink("users"));
            assertTrue(result.hasLink("update"));
            assertTrue(result.hasLink("delete"));
            assertTrue(result.hasLink("change-password"));
        }

        @Test
        @DisplayName("Debe generar todos los enlaces para usuario completo")
        void testToModel_CompleteUser_ShouldHaveAllApplicableLinks() {
            // Configurar usuario completo no verificado e inactivo
            testUser.setActive(false);
            testUser.setVerified(false);
            testUserRepresentation.setActive(false);
            testUserRepresentation.setVerified(false);
            
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(testUser);

            // Verificar que tiene todos los enlaces aplicables
            assertTrue(result.hasLink("self"));
            assertTrue(result.hasLink("users"));
            assertTrue(result.hasLink("update"));
            assertTrue(result.hasLink("delete"));
            assertTrue(result.hasLink("activate")); // porque está inactivo
            assertTrue(result.hasLink("change-password"));
            assertTrue(result.hasLink("company-users")); // porque tiene companyId
            assertTrue(result.hasLink("role-users")); // porque tiene roleId
            assertTrue(result.hasLink("assign-role")); // porque tiene roleId
            assertTrue(result.hasLink("verify")); // porque no está verificado
            
            // Verificar que NO tiene enlace de desactivación (porque ya está inactivo)
            assertFalse(result.hasLink("deactivate"));
        }
    }

    @Nested
    @DisplayName("Computed Properties Tests")
    class ComputedPropertiesTests {

        @Test
        @DisplayName("Debe preservar propiedades computadas de UserRepresentation")
        void testToModel_ShouldPreserveComputedProperties() {
            // Configurar mock
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            // Ejecutar
            UserRepresentation result = userModelAssembler.toModel(testUser);

            // Verificar propiedades computadas
            assertEquals("Juan Pérez", result.getFullName());
            assertEquals("active", result.getStatus()); // activo y verificado
        }

        @Test
        @DisplayName("Debe manejar diferentes estados de usuario")
        void testToModel_DifferentUserStates_ShouldReturnCorrectStatus() {
            // Probar usuario pendiente de verificación
            testUser.setActive(true);
            testUser.setVerified(false);
            testUserRepresentation.setActive(true);
            testUserRepresentation.setVerified(false);
            
            when(userMapper.toRepresentation(testUser)).thenReturn(testUserRepresentation);

            UserRepresentation result = userModelAssembler.toModel(testUser);
            assertEquals("pending_verification", result.getStatus());

            // Probar usuario inactivo
            testUser.setActive(false);
            testUser.setVerified(true);
            testUserRepresentation.setActive(false);
            testUserRepresentation.setVerified(true);
            
            result = userModelAssembler.toModel(testUser);
            assertEquals("inactive", result.getStatus());
        }
    }
}
