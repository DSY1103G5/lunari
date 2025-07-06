package cl.duoc.lunari.api.user.service;

import cl.duoc.lunari.api.user.model.User;
import cl.duoc.lunari.api.user.repository.RoleRepository;
import cl.duoc.lunari.api.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) 
class UserServiceImplTest {

    @Mock 
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks 
    private UserServiceImpl userService;

    private User testUser;
    private UUID testUserId = UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef");

    @BeforeEach
    void setUp() {
       
        testUser = new User();
        testUser.setId(testUserId);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("securePassword123");
        testUser.setActive(true);
        testUser.setRoleId(1);
    }

    //Tests para createUser
    // Verifica que el método createUser crea un usuario correctamente si el email no existe.
    @Test
    void createUser_shouldReturnUser_whenEmailDoesNotExist() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(testUser)).thenReturn(testUser);

        User createdUser = userService.createUser(testUser);

        assertNotNull(createdUser);
        assertEquals(testUser.getEmail(), createdUser.getEmail());
        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
        verify(userRepository, times(1)).save(testUser);
    }

    // Verifica que el método createUser lanza una excepción si el email ya existe en la base de datos.
    // En este caso, se simula que el repositorio devuelve un usuario con el mismo email,
    // y se espera que se lance una RuntimeException con un mensaje específico.
    @Test
    void createUser_shouldThrowRuntimeException_whenEmailAlreadyExists() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(new User())); 

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userService.createUser(testUser);
        });

        assertEquals("Email ya existe: " + testUser.getEmail(), thrown.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    //Tests para getUserById
    //Verifica que el método getUserById devuelve un usuario correctamente encapsulado en un Optional 
    //cuando el usuario con el ID especificado existe en la base de datos.
    @Test
    void getUserById_shouldReturnUser_whenUserExists() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        Optional<User> foundUser = userService.getUserById(testUserId);

        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
        verify(userRepository, times(1)).findById(testUserId);
    }

    // Verifica que el método getUserById devuelve un Optional vacío cuando el usuario no existe.
    // Se simula que el repositorio no encuentra un usuario con el ID dado,
    // y se verifica que el Optional devuelto está vacío.
    // También se verifica que se llama al método findById del repositorio una vez.
    @Test
    void getUserById_shouldReturnEmptyOptional_whenUserDoesNotExist() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.getUserById(testUserId);

        assertFalse(foundUser.isPresent());
        verify(userRepository, times(1)).findById(testUserId);
    }

    //Tests para getUserByEmail
    // Verifica que el método getUserByEmail devuelve un usuario correctamente encapsulado en un Optional
    // cuando un usuario con el email especificado existe en la base de datos.
    @Test
    void getUserByEmail_shouldReturnUser_whenUserExists() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        Optional<User> foundUser = userService.getUserByEmail(testUser.getEmail());

        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
    }

    // Verifica que el método getUserByEmail devuelve un Optional vacío cuando el usuario no existe.
    // Se simula que el repositorio no encuentra un usuario con el email dado,
    // y se verifica que el Optional devuelto está vacío.
    // También se verifica que se llama al método findByEmail del repositorio una vez.
    @Test
    void getUserByEmail_shouldReturnEmptyOptional_whenUserDoesNotExist() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());

        Optional<User> foundUser = userService.getUserByEmail(testUser.getEmail());

        assertFalse(foundUser.isPresent());
        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
    }

    //Tests para getAllUsers
    // Verifica que el método getAllUsers devuelve una lista de usuarios paginada.
    // Se simula el comportamiento del repositorio para devolver una página de usuarios,
    // y se verifica que la lista devuelta tiene el tamaño correcto.
    // También se verifica que se llama al método findAll del repositorio con el Pageable correcto
    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        List<User> users = Arrays.asList(testUser, new User()); 
        Page<User> userPage = new PageImpl<>(users); 
        Pageable pageable = PageRequest.of(0, 10); 

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        List<User> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll(pageable);
    }

    //Tests para updateUser
    // Verifica que el método updateUser actualiza correctamente los detalles de un
    // usuario existente, siempre que el nuevo email proporcionado sea único (no esté en uso por otro usuario).
    @Test
    void updateUser_shouldReturnUpdatedUser_whenUserExistsAndEmailUnique() {
        User updatedDetails = new User();
        updatedDetails.setFirstName("Jane");
        updatedDetails.setLastName("Doe");
        updatedDetails.setEmail("jane.doe@example.com"); 

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(updatedDetails.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0)); 

        User result = userService.updateUser(testUserId, updatedDetails);

        assertNotNull(result);
        assertEquals(updatedDetails.getFirstName(), result.getFirstName());
        assertEquals(updatedDetails.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, times(1)).findByEmail(updatedDetails.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    // Verifica que el método updateUser lanza una excepción si el usuario no existe.
    // Se simula que el repositorio no encuentra un usuario con el ID dado,
    // y se espera que se lance una RuntimeException con un mensaje específico.
    // También se verifica que se llama al método findById del repositorio una vez,
    // y que no se llama a los métodos findByEmail ni save.
    @Test
    void updateUser_shouldThrowRuntimeException_whenUserDoesNotExist() {
        User updatedDetails = new User();
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(testUserId, updatedDetails);
        });

        assertEquals("Usuario no encontrado con ID: " + testUserId, thrown.getMessage());
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, never()).findByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    // Verifica que el método updateUser lanza una excepción si el nuevo email ya existe para otro usuario.
    // Se simula que el repositorio encuentra un usuario con el mismo email,
    // y se espera que se lance una RuntimeException con un mensaje específico.
    // También se verifica que se llama al método findById del repositorio una vez,
    // y que se llama al método findByEmail del repositorio una vez,
    // pero que no se llama al método save.
    @Test
    void updateUser_shouldThrowRuntimeException_whenNewEmailAlreadyExistsForAnotherUser() {
        User existingUserWithNewEmail = new User();
        existingUserWithNewEmail.setId(UUID.randomUUID());
        existingUserWithNewEmail.setEmail("existing@example.com");

        User updatedDetails = new User();
        updatedDetails.setFirstName("Jane");
        updatedDetails.setEmail("existing@example.com");

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail(updatedDetails.getEmail())).thenReturn(Optional.of(existingUserWithNewEmail));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userService.updateUser(testUserId, updatedDetails);
        });

        assertEquals("Email ya existe en la base de datos: " + updatedDetails.getEmail(), thrown.getMessage());
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, times(1)).findByEmail(updatedDetails.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    //Tests para deleteUser
    // Verifica que el método deleteUser elimina un usuario si existe.
    // Se simula que el repositorio encuentra el usuario por ID,
    // y se verifica que se llama al método deleteById del repositorio una vez.
    // También se verifica que se llama al método existsById del repositorio una vez.
    @Test
    void deleteUser_shouldDeleteUser_whenUserExists() {
        when(userRepository.existsById(testUserId)).thenReturn(true);

        userService.deleteUser(testUserId);

        verify(userRepository, times(1)).existsById(testUserId);
        verify(userRepository, times(1)).deleteById(testUserId);
    }

    // Verifica que el método deleteUser lanza una excepción si el usuario no existe.
    // Se simula que el repositorio no encuentra un usuario con el ID dado,
    // y se espera que se lance una RuntimeException con un mensaje específico.
    // También se verifica que se llama al método existsById del repositorio una vez,
    // y que no se llama al método deleteById.
    @Test
    void deleteUser_shouldThrowRuntimeException_whenUserDoesNotExist() {
        when(userRepository.existsById(testUserId)).thenReturn(false);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(testUserId);
        });

        assertEquals("User not found with id: " + testUserId, thrown.getMessage());
        verify(userRepository, times(1)).existsById(testUserId);
        verify(userRepository, never()).deleteById(any(UUID.class));
    }

    //Tests para updatePassword
    // Verifica que el método updatePassword actualiza la contraseña de un usuario si existe,
    // y lanza una excepción si el usuario no existe.
    // Se simula que el repositorio encuentra el usuario por ID,
    // y se verifica que la contraseña del usuario se actualiza correctamente.
    // También se verifica que se llama al método save del repositorio una vez,
    // y que se llama al método findById del repositorio una vez.
    @Test
    void updatePassword_shouldUpdatePassword_whenUserExists() {
        String newPassword = "newSimplePassword456";

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userBeingSaved = invocation.getArgument(0);
            assertEquals(newPassword, userBeingSaved.getPassword()); 
            return userBeingSaved;
        });

        userService.updatePassword(testUserId, newPassword);

        verify(userRepository, times(1)).findById(testUserId);
            verify(userRepository, times(1)).save(testUser); 
            assertEquals(newPassword, testUser.getPassword());
        }

    
    // Verifica que el método updatePassword lanza una excepción si el usuario no existe.
    // Se simula que el repositorio no encuentra un usuario con el ID dado,
    // y se espera que se lance una RuntimeException con un mensaje específico.
    // También se verifica que se llama al método findById del repositorio una vez,
    // y que no se llama al método save.
    @Test
    void updatePassword_shouldThrowRuntimeException_whenUserDoesNotExist() {
        String newPassword = "somePassword";

        // Simula que el repositorio NO encuentra el usuario
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Llama al método del servicio y verifica que lanza la excepción
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userService.updatePassword(testUserId, newPassword);
        });

        // Verificar el mensaje de la excepción
        assertEquals("Usuario no encontrado con ID: " + testUserId, thrown.getMessage());
        
        // Verificar que no se llamó a save
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, never()).save(any(User.class));
    }

    //Tests para assignRoleToUser
    // Verifica que el método assignRoleToUser asigna un rol a un usuario si el usuario existe,
    // y lanza una excepción si el usuario no existe.
    // Se simula que el repositorio encuentra el usuario por ID,
    // y se verifica que el rol del usuario se actualiza correctamente.
    // También se verifica que se llama al método save del repositorio una vez,
    // y que se llama al método findById del repositorio una vez.
    @Test
    void assignRoleToUser_shouldAssignRole_whenUserExists() {
        Integer newRoleId = 2; 

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals(newRoleId, savedUser.getRoleId());
            return savedUser;
        });

        userService.assignRoleToUser(testUserId, newRoleId);

        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, times(1)).save(testUser); 
        assertEquals(newRoleId, testUser.getRoleId()); 
    }


    // Verifica que el método assignRoleToUser lanza una excepción si el usuario no existe.
    // Se simula que el repositorio no encuentra un usuario con el ID dado,
    // y se espera que se lance una RuntimeException con un mensaje específico.
    // También se verifica que se llama al método findById del repositorio una vez,
    // y que no se llama al método save.
    @Test
    void assignRoleToUser_shouldThrowRuntimeException_whenUserDoesNotExist() {
        Integer newRoleId = 2;

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userService.assignRoleToUser(testUserId, newRoleId);
        });

        assertEquals("Usuario no encontrado con ID: " + testUserId, thrown.getMessage());
        
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, never()).save(any(User.class));
    }

    //Tests para getAllRoles
    // Verifica que el método getAllRoles devuelve una lista de todos los roles disponibles.
    // Se simula que el repositorio devuelve una lista de roles,
    // y se verifica que la lista devuelta tiene el tamaño correcto.
    // También se verifica que se llama al método findAll del roleRepository una vez.
    @Test
    void getAllRoles_shouldReturnListOfRoles() {
        cl.duoc.lunari.api.user.model.UserRole role1 = new cl.duoc.lunari.api.user.model.UserRole();
        role1.setId(1);
        role1.setName("ADMIN");
        
        cl.duoc.lunari.api.user.model.UserRole role2 = new cl.duoc.lunari.api.user.model.UserRole();
        role2.setId(2);
        role2.setName("USER");
        
        List<cl.duoc.lunari.api.user.model.UserRole> roles = Arrays.asList(role1, role2);
        
        when(roleRepository.findAll()).thenReturn(roles);

        List<cl.duoc.lunari.api.user.model.UserRole> result = userService.getAllRoles();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ADMIN", result.get(0).getName());
        assertEquals("USER", result.get(1).getName());
        verify(roleRepository, times(1)).findAll();
    }

    //Tests para getUsersByCompany
    // Verifica que el método getUsersByCompany devuelve una lista de usuarios de una compañía específica.
    // Se simula que el repositorio devuelve una lista de usuarios para la compañía dada,
    // y se verifica que la lista devuelta tiene el tamaño correcto.
    // También se verifica que se llama al método findByCompanyId del repositorio una vez.
    @Test
    void getUsersByCompany_shouldReturnUsersOfCompany() {
        UUID companyId = UUID.randomUUID();
        User user1 = new User();
        user1.setCompanyId(companyId);
        User user2 = new User();
        user2.setCompanyId(companyId);
        
        List<User> companyUsers = Arrays.asList(user1, user2);
        
        when(userRepository.findByCompanyId(companyId)).thenReturn(companyUsers);

        List<User> result = userService.getUsersByCompany(companyId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(companyId, result.get(0).getCompanyId());
        assertEquals(companyId, result.get(1).getCompanyId());
        verify(userRepository, times(1)).findByCompanyId(companyId);
    }

    //Tests para assignUserToCompany
    // Verifica que el método assignUserToCompany asigna un usuario a una compañía si el usuario existe.
    // Se simula que el repositorio encuentra el usuario por ID,
    // y se verifica que el companyId del usuario se actualiza correctamente.
    // También se verifica que se llama al método save del repositorio una vez.
    @Test
    void assignUserToCompany_shouldAssignCompany_whenUserExists() {
        UUID companyId = UUID.randomUUID();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals(companyId, savedUser.getCompanyId());
            return savedUser;
        });

        userService.assignUserToCompany(testUserId, companyId);

        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, times(1)).save(testUser);
        assertEquals(companyId, testUser.getCompanyId());
    }

    // Verifica que el método assignUserToCompany lanza una excepción si el usuario no existe.
    // Se simula que el repositorio no encuentra un usuario con el ID dado,
    // y se espera que se lance una RuntimeException con un mensaje específico.
    // También se verifica que se llama al método findById del repositorio una vez,
    // y que no se llama al método save.
    @Test
    void assignUserToCompany_shouldThrowRuntimeException_whenUserDoesNotExist() {
        UUID companyId = UUID.randomUUID();

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userService.assignUserToCompany(testUserId, companyId);
        });

        assertEquals("Usuario no encontrado con ID: " + testUserId, thrown.getMessage());
        
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, never()).save(any(User.class));
    }

    //Tests para getUsersPaginated
    // Verifica que el método getUsersPaginated devuelve usuarios paginados con filtros aplicados.
    // Se simula que el repositorio devuelve una página de usuarios filtrada,
    // y se verifica que la página devuelta tiene el contenido correcto.
    // También se verifica que se llama al método findUsersWithFilters del repositorio una vez.
    @Test
    void getUsersPaginated_shouldReturnFilteredUsers() {
        Boolean active = true;
        Integer roleId = 1;
        UUID companyId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);
        
        when(userRepository.findUsersWithFilters(active, roleId, companyId, pageable)).thenReturn(userPage);

        Page<User> result = userService.getUsersPaginated(pageable, active, roleId, companyId);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testUser, result.getContent().get(0));
        verify(userRepository, times(1)).findUsersWithFilters(active, roleId, companyId, pageable);
    }

    //Tests para searchUsers
    // Verifica que el método searchUsers devuelve usuarios que coinciden con la consulta de búsqueda.
    // Se simula que el repositorio devuelve una página de usuarios que coinciden con la búsqueda,
    // y se verifica que la página devuelta tiene el contenido correcto.
    // También se verifica que se llama al método searchUsers del repositorio una vez.
    @Test
    void searchUsers_shouldReturnMatchingUsers() {
        String query = "john";
        Pageable pageable = PageRequest.of(0, 10);
        
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);
        
        when(userRepository.searchUsers(query, pageable)).thenReturn(userPage);

        Page<User> result = userService.searchUsers(query, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testUser, result.getContent().get(0));
        verify(userRepository, times(1)).searchUsers(query, pageable);
    }

    //Tests para getUsersByCompanyPaginated
    // Verifica que el método getUsersByCompanyPaginated devuelve usuarios paginados de una compañía específica.
    // Se simula que el repositorio devuelve una página de usuarios para la compañía dada,
    // y se verifica que la página devuelta tiene el contenido correcto.
    // También se verifica que se llama al método findByCompanyId con paginación del repositorio una vez.
    @Test
    void getUsersByCompanyPaginated_shouldReturnPaginatedCompanyUsers() {
        UUID companyId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);
        
        when(userRepository.findByCompanyId(companyId, pageable)).thenReturn(userPage);

        Page<User> result = userService.getUsersByCompanyPaginated(companyId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testUser, result.getContent().get(0));
        verify(userRepository, times(1)).findByCompanyId(companyId, pageable);
    }

    //Tests para getUsersByRolePaginated
    // Verifica que el método getUsersByRolePaginated devuelve usuarios paginados con un rol específico.
    // Se simula que el repositorio devuelve una página de usuarios para el rol dado,
    // y se verifica que la página devuelta tiene el contenido correcto.
    // También se verifica que se llama al método findByRoleId con paginación del repositorio una vez.
    @Test
    void getUsersByRolePaginated_shouldReturnPaginatedRoleUsers() {
        Integer roleId = 1;
        Pageable pageable = PageRequest.of(0, 10);
        
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);
        
        when(userRepository.findByRoleId(roleId, pageable)).thenReturn(userPage);

        Page<User> result = userService.getUsersByRolePaginated(roleId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testUser, result.getContent().get(0));
        verify(userRepository, times(1)).findByRoleId(roleId, pageable);
    }

    //Tests para getUserStats
    // Verifica que el método getUserStats devuelve estadísticas correctas de usuarios.
    // Se simula que los repositorios devuelven conteos específicos,
    // y se verifica que las estadísticas devueltas contienen la información correcta.
    // También se verifica que se llaman a los métodos de conteo del repositorio.
    @Test
    void getUserStats_shouldReturnCorrectStats() {
        // Configurar roles para la distribución
        cl.duoc.lunari.api.user.model.UserRole role1 = new cl.duoc.lunari.api.user.model.UserRole();
        role1.setId(1);
        role1.setName("ADMIN");
        
        cl.duoc.lunari.api.user.model.UserRole role2 = new cl.duoc.lunari.api.user.model.UserRole();
        role2.setId(2);
        role2.setName("USER");
        
        List<cl.duoc.lunari.api.user.model.UserRole> roles = Arrays.asList(role1, role2);
        
        // Configurar mocks
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByActive(true)).thenReturn(80L);
        when(userRepository.countByActive(false)).thenReturn(20L);
        when(roleRepository.findAll()).thenReturn(roles);
        when(userRepository.countByRoleId(1)).thenReturn(10L);
        when(userRepository.countByRoleId(2)).thenReturn(70L);

        Object result = userService.getUserStats();

        assertNotNull(result);
        assertTrue(result instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) result;
        
        assertEquals(100L, stats.get("totalUsers"));
        assertEquals(80L, stats.get("activeUsers"));
        assertEquals(20L, stats.get("inactiveUsers"));
        assertEquals("Not implemented yet", stats.get("verifiedUsers"));
        
        @SuppressWarnings("unchecked")
        Map<String, Long> roleDistribution = (Map<String, Long>) stats.get("roleDistribution");
        assertEquals(10L, roleDistribution.get("ADMIN"));
        assertEquals(70L, roleDistribution.get("USER"));
        
        verify(userRepository, times(1)).count();
        verify(userRepository, times(1)).countByActive(true);
        verify(userRepository, times(1)).countByActive(false);
        verify(roleRepository, times(1)).findAll();
        verify(userRepository, times(1)).countByRoleId(1);
        verify(userRepository, times(1)).countByRoleId(2);
    }

    //Tests para updateUserStatus
    // Verifica que el método updateUserStatus actualiza el estado activo de un usuario si existe.
    // Se simula que el repositorio encuentra el usuario por ID,
    // y se verifica que el estado activo del usuario se actualiza correctamente.
    // También se verifica que se llama al método save del repositorio una vez.
    @Test
    void updateUserStatus_shouldUpdateStatus_whenUserExists() {
        Boolean newStatus = false;

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals(newStatus, savedUser.getActive());
            return savedUser;
        });

        User result = userService.updateUserStatus(testUserId, newStatus);

        assertNotNull(result);
        assertEquals(newStatus, result.getActive());
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, times(1)).save(testUser);
        assertEquals(newStatus, testUser.getActive());
    }

    // Verifica que el método updateUserStatus lanza una excepción si el usuario no existe.
    // Se simula que el repositorio no encuentra un usuario con el ID dado,
    // y se espera que se lance una RuntimeException con un mensaje específico.
    // También se verifica que se llama al método findById del repositorio una vez,
    // y que no se llama al método save.
    @Test
    void updateUserStatus_shouldThrowRuntimeException_whenUserDoesNotExist() {
        Boolean newStatus = false;

        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            userService.updateUserStatus(testUserId, newStatus);
        });

        assertEquals("Usuario no encontrado con ID: " + testUserId, thrown.getMessage());
        
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, never()).save(any(User.class));
    }

   
}