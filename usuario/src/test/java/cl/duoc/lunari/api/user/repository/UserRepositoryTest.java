package cl.duoc.lunari.api.user.repository;

import cl.duoc.lunari.api.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;
    private User testUser3;
    private UUID companyId1;
    private UUID companyId2;

    @BeforeEach
    void setUp() {
        companyId1 = UUID.randomUUID();
        companyId2 = UUID.randomUUID();

        // Usuario activo con rol 1 en compañía 1
        testUser1 = new User();
        testUser1.setFirstName("Juan");
        testUser1.setLastName("Pérez");
        testUser1.setEmail("juan.perez@empresa1.com");
        testUser1.setPassword("SecurePassword123!");
        testUser1.setPhone("123456789");
        testUser1.setRoleId(1);
        testUser1.setCompanyId(companyId1);
        testUser1.setActive(true);
        testUser1.setVerified(true);
        testUser1.setCreatedAt(OffsetDateTime.now());
        testUser1.setUpdatedAt(OffsetDateTime.now());

        // Usuario inactivo con rol 2 en compañía 1
        testUser2 = new User();
        testUser2.setFirstName("María");
        testUser2.setLastName("González");
        testUser2.setEmail("maria.gonzalez@empresa1.com");
        testUser2.setPassword("AnotherPassword456!");
        testUser2.setPhone("987654321");
        testUser2.setRoleId(2);
        testUser2.setCompanyId(companyId1);
        testUser2.setActive(false);
        testUser2.setVerified(false);
        testUser2.setCreatedAt(OffsetDateTime.now());
        testUser2.setUpdatedAt(OffsetDateTime.now());

        // Usuario activo con rol 1 en compañía 2
        testUser3 = new User();
        testUser3.setFirstName("Carlos");
        testUser3.setLastName("Rodríguez");
        testUser3.setEmail("carlos.rodriguez@empresa2.com");
        testUser3.setPassword("ThirdPassword789!");
        testUser3.setPhone("555666777");
        testUser3.setRoleId(1);
        testUser3.setCompanyId(companyId2);
        testUser3.setActive(true);
        testUser3.setVerified(true);
        testUser3.setCreatedAt(OffsetDateTime.now());
        testUser3.setUpdatedAt(OffsetDateTime.now());

        // Persistir usuarios en la base de datos de prueba
        entityManager.persistAndFlush(testUser1);
        entityManager.persistAndFlush(testUser2);
        entityManager.persistAndFlush(testUser3);
    }

    //Tests para métodos básicos de JpaRepository

    // Verifica que el método save de JpaRepository funciona correctamente.
    // Se crea un nuevo usuario, se guarda y se verifica que se persiste con un ID generado.
    @Test
    void save_shouldPersistUser_andGenerateId() {
        User newUser = new User();
        newUser.setFirstName("Nuevo");
        newUser.setLastName("Usuario");
        newUser.setEmail("nuevo.usuario@test.com");
        newUser.setPassword("NuevaPassword123!");
        newUser.setRoleId(1);
        newUser.setActive(true);
        newUser.setVerified(false);

        User savedUser = userRepository.save(newUser);

        assertNotNull(savedUser.getId());
        assertEquals("nuevo.usuario@test.com", savedUser.getEmail());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());
    }

    // Verifica que el método findById encuentra un usuario existente por su ID.
    // Se busca un usuario por ID y se verifica que se encuentra correctamente.
    @Test
    void findById_shouldReturnUser_whenUserExists() {
        Optional<User> foundUser = userRepository.findById(testUser1.getId());

        assertTrue(foundUser.isPresent());
        assertEquals(testUser1.getEmail(), foundUser.get().getEmail());
        assertEquals(testUser1.getFirstName(), foundUser.get().getFirstName());
    }

    // Verifica que el método findById devuelve Optional vacío cuando el usuario no existe.
    // Se busca un usuario con ID inexistente y se verifica que no se encuentra.
    @Test
    void findById_shouldReturnEmpty_whenUserDoesNotExist() {
        UUID nonExistentId = UUID.randomUUID();
        
        Optional<User> foundUser = userRepository.findById(nonExistentId);

        assertFalse(foundUser.isPresent());
    }

    // Verifica que el método findAll devuelve todos los usuarios persistidos.
    // Se verifica que la cantidad de usuarios devueltos coincide con los usuarios creados en setup.
    @Test
    void findAll_shouldReturnAllUsers() {
        List<User> users = userRepository.findAll();

        assertEquals(3, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("juan.perez@empresa1.com")));
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("maria.gonzalez@empresa1.com")));
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("carlos.rodriguez@empresa2.com")));
    }

    // Verifica que el método deleteById elimina correctamente un usuario.
    // Se elimina un usuario y se verifica que ya no se puede encontrar.
    @Test
    void deleteById_shouldRemoveUser_whenUserExists() {
        UUID userIdToDelete = testUser1.getId();
        
        userRepository.deleteById(userIdToDelete);
        entityManager.flush();

        Optional<User> deletedUser = userRepository.findById(userIdToDelete);
        assertFalse(deletedUser.isPresent());
        
        List<User> remainingUsers = userRepository.findAll();
        assertEquals(2, remainingUsers.size());
    }

    //Tests para métodos personalizados del repositorio

    // Verifica que el método findByEmail encuentra un usuario por su email.
    // Se busca un usuario existente por email y se verifica que se encuentra correctamente.
    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {
        Optional<User> foundUser = userRepository.findByEmail("juan.perez@empresa1.com");

        assertTrue(foundUser.isPresent());
        assertEquals(testUser1.getId(), foundUser.get().getId());
        assertEquals("Juan", foundUser.get().getFirstName());
    }

    // Verifica que el método findByEmail devuelve Optional vacío cuando el email no existe.
    // Se busca un usuario con email inexistente y se verifica que no se encuentra.
    @Test
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
        Optional<User> foundUser = userRepository.findByEmail("noexiste@test.com");

        assertFalse(foundUser.isPresent());
    }

    // Verifica que el método findByEmail es case-sensitive.
    // Se busca un usuario con email en mayúsculas y se verifica que no se encuentra.
    @Test
    void findByEmail_shouldBeCaseSensitive() {
        Optional<User> foundUser = userRepository.findByEmail("JUAN.PEREZ@EMPRESA1.COM");

        assertFalse(foundUser.isPresent());
    }

    // Verifica que el método findByCompanyId devuelve usuarios de una compañía específica.
    // Se buscan usuarios de la compañía 1 y se verifica que se devuelven los usuarios correctos.
    @Test
    void findByCompanyId_shouldReturnUsersOfCompany() {
        List<User> companyUsers = userRepository.findByCompanyId(companyId1);

        assertEquals(2, companyUsers.size());
        assertTrue(companyUsers.stream().allMatch(u -> u.getCompanyId().equals(companyId1)));
        assertTrue(companyUsers.stream().anyMatch(u -> u.getEmail().equals("juan.perez@empresa1.com")));
        assertTrue(companyUsers.stream().anyMatch(u -> u.getEmail().equals("maria.gonzalez@empresa1.com")));
    }

    // Verifica que el método findByCompanyId devuelve lista vacía cuando no hay usuarios en la compañía.
    // Se buscan usuarios de una compañía inexistente y se verifica que la lista está vacía.
    @Test
    void findByCompanyId_shouldReturnEmptyList_whenNoUsersInCompany() {
        UUID nonExistentCompanyId = UUID.randomUUID();
        
        List<User> companyUsers = userRepository.findByCompanyId(nonExistentCompanyId);

        assertTrue(companyUsers.isEmpty());
    }

    //Tests para métodos de paginación

    // Verifica que el método findByCompanyId con paginación devuelve usuarios paginados de una compañía.
    // Se buscan usuarios de la compañía 1 con paginación y se verifica el resultado paginado.
    @Test
    void findByCompanyIdPaginated_shouldReturnPaginatedUsersOfCompany() {
        Pageable pageable = PageRequest.of(0, 1);
        
        Page<User> userPage = userRepository.findByCompanyId(companyId1, pageable);

        assertEquals(1, userPage.getContent().size());
        assertEquals(2, userPage.getTotalElements());
        assertEquals(2, userPage.getTotalPages());
        assertTrue(userPage.getContent().get(0).getCompanyId().equals(companyId1));
    }

    // Verifica que el método findByRoleId con paginación devuelve usuarios con un rol específico.
    // Se buscan usuarios con rol 1 y se verifica que se devuelven los usuarios correctos.
    @Test
    void findByRoleIdPaginated_shouldReturnUsersWithSpecificRole() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<User> userPage = userRepository.findByRoleId(1, pageable);

        assertEquals(2, userPage.getContent().size());
        assertTrue(userPage.getContent().stream().allMatch(u -> u.getRoleId().equals(1)));
        assertTrue(userPage.getContent().stream().anyMatch(u -> u.getEmail().equals("juan.perez@empresa1.com")));
        assertTrue(userPage.getContent().stream().anyMatch(u -> u.getEmail().equals("carlos.rodriguez@empresa2.com")));
    }

    // Verifica que el método findByActive con paginación devuelve usuarios activos o inactivos.
    // Se buscan usuarios activos y se verifica que se devuelven solo los usuarios activos.
    @Test
    void findByActivePaginated_shouldReturnActiveUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<User> activeUsers = userRepository.findByActive(true, pageable);

        assertEquals(2, activeUsers.getContent().size());
        assertTrue(activeUsers.getContent().stream().allMatch(User::getActive));
        assertTrue(activeUsers.getContent().stream().anyMatch(u -> u.getEmail().equals("juan.perez@empresa1.com")));
        assertTrue(activeUsers.getContent().stream().anyMatch(u -> u.getEmail().equals("carlos.rodriguez@empresa2.com")));
    }

    // Verifica que el método findByActive con paginación devuelve usuarios inactivos.
    // Se buscan usuarios inactivos y se verifica que se devuelve solo el usuario inactivo.
    @Test
    void findByActivePaginated_shouldReturnInactiveUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<User> inactiveUsers = userRepository.findByActive(false, pageable);

        assertEquals(1, inactiveUsers.getContent().size());
        assertFalse(inactiveUsers.getContent().get(0).getActive());
        assertEquals("maria.gonzalez@empresa1.com", inactiveUsers.getContent().get(0).getEmail());
    }

    //Tests para consultas personalizadas con @Query

    // Verifica que el método findUsersWithFilters funciona con todos los filtros aplicados.
    // Se filtran usuarios activos, con rol 1, de la compañía 1 y se verifica el resultado.
    @Test
    void findUsersWithFilters_shouldReturnFilteredUsers_whenAllFiltersApplied() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<User> filteredUsers = userRepository.findUsersWithFilters(true, 1, companyId1, pageable);

        assertEquals(1, filteredUsers.getContent().size());
        User user = filteredUsers.getContent().get(0);
        assertTrue(user.getActive());
        assertEquals(1, user.getRoleId());
        assertEquals(companyId1, user.getCompanyId());
        assertEquals("juan.perez@empresa1.com", user.getEmail());
    }

    // Verifica que el método findUsersWithFilters funciona con filtros nulos (sin filtros).
    // Se buscan usuarios sin aplicar filtros y se verifica que devuelve todos los usuarios.
    @Test
    void findUsersWithFilters_shouldReturnAllUsers_whenNoFiltersApplied() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<User> allUsers = userRepository.findUsersWithFilters(null, null, null, pageable);

        assertEquals(3, allUsers.getContent().size());
    }

    // Verifica que el método findUsersWithFilters funciona con filtro solo por estado activo.
    // Se filtran solo usuarios activos y se verifica que se devuelven 2 usuarios activos.
    @Test
    void findUsersWithFilters_shouldFilterByActiveOnly_whenOnlyActiveFilterApplied() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<User> activeUsers = userRepository.findUsersWithFilters(true, null, null, pageable);

        assertEquals(2, activeUsers.getContent().size());
        assertTrue(activeUsers.getContent().stream().allMatch(User::getActive));
    }

    // Verifica que el método findUsersWithFilters funciona con filtro solo por rol.
    // Se filtran usuarios con rol 2 y se verifica que se devuelve solo 1 usuario.
    @Test
    void findUsersWithFilters_shouldFilterByRoleOnly_whenOnlyRoleFilterApplied() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<User> roleUsers = userRepository.findUsersWithFilters(null, 2, null, pageable);

        assertEquals(1, roleUsers.getContent().size());
        assertEquals(2, roleUsers.getContent().get(0).getRoleId());
        assertEquals("maria.gonzalez@empresa1.com", roleUsers.getContent().get(0).getEmail());
    }

    // Verifica que el método findUsersWithFilters funciona con filtro solo por compañía.
    // Se filtran usuarios de la compañía 2 y se verifica que se devuelve solo 1 usuario.
    @Test
    void findUsersWithFilters_shouldFilterByCompanyOnly_whenOnlyCompanyFilterApplied() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<User> companyUsers = userRepository.findUsersWithFilters(null, null, companyId2, pageable);

        assertEquals(1, companyUsers.getContent().size());
        assertEquals(companyId2, companyUsers.getContent().get(0).getCompanyId());
        assertEquals("carlos.rodriguez@empresa2.com", companyUsers.getContent().get(0).getEmail());
    }

    // Verifica que el método searchUsers busca por nombre.
    // Se busca por "Juan" y se verifica que encuentra al usuario con ese nombre.
    @Test
    void searchUsers_shouldFindUsersByFirstName() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<User> searchResults = userRepository.searchUsers("Juan", pageable);

        assertEquals(1, searchResults.getContent().size());
        assertEquals("Juan", searchResults.getContent().get(0).getFirstName());
    }

    // Verifica que el método searchUsers busca por apellido.
    // Se busca por "González" y se verifica que encuentra al usuario con ese apellido.
    @Test
    void searchUsers_shouldFindUsersByLastName() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<User> searchResults = userRepository.searchUsers("González", pageable);

        assertEquals(1, searchResults.getContent().size());
        assertEquals("González", searchResults.getContent().get(0).getLastName());
    }

    // Verifica que el método searchUsers busca por email.
    // Se busca por parte del email y se verifica que encuentra al usuario correspondiente.
    @Test
    void searchUsers_shouldFindUsersByEmail() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<User> searchResults = userRepository.searchUsers("empresa1", pageable);

        assertEquals(2, searchResults.getContent().size());
        assertTrue(searchResults.getContent().stream()
            .allMatch(u -> u.getEmail().contains("empresa1")));
    }

    // Verifica que el método searchUsers es case-insensitive.
    // Se busca con texto en mayúsculas y se verifica que encuentra resultados.
    @Test
    void searchUsers_shouldBeCaseInsensitive() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<User> searchResults = userRepository.searchUsers("CARLOS", pageable);

        assertEquals(1, searchResults.getContent().size());
        assertEquals("Carlos", searchResults.getContent().get(0).getFirstName());
    }

    // Verifica que el método searchUsers devuelve resultados vacíos cuando no hay coincidencias.
    // Se busca texto que no existe y se verifica que no hay resultados.
    @Test
    void searchUsers_shouldReturnEmpty_whenNoMatches() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<User> searchResults = userRepository.searchUsers("NoExiste", pageable);

        assertEquals(0, searchResults.getContent().size());
    }

    // Verifica que el método searchUsers maneja cadenas vacías devolviendo todos los resultados.
    // Se busca con cadena vacía y se verifica el comportamiento.
    @Test
    void searchUsers_shouldReturnAllUsers_whenEmptyQuery() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Page<User> searchResults = userRepository.searchUsers("", pageable);

        assertEquals(3, searchResults.getContent().size());
    }

    //Tests para métodos de conteo

    // Verifica que el método countByActive cuenta correctamente usuarios activos.
    // Se cuentan usuarios activos y se verifica que el conteo es correcto.
    @Test
    void countByActive_shouldCountActiveUsers() {
        long activeCount = userRepository.countByActive(true);
        long inactiveCount = userRepository.countByActive(false);

        assertEquals(2, activeCount);
        assertEquals(1, inactiveCount);
    }

    // Verifica que el método countByRoleId cuenta correctamente usuarios por rol.
    // Se cuentan usuarios con roles específicos y se verifica que el conteo es correcto.
    @Test
    void countByRoleId_shouldCountUsersByRole() {
        long role1Count = userRepository.countByRoleId(1);
        long role2Count = userRepository.countByRoleId(2);
        long nonExistentRoleCount = userRepository.countByRoleId(999);

        assertEquals(2, role1Count);
        assertEquals(1, role2Count);
        assertEquals(0, nonExistentRoleCount);
    }

    // Verifica que el método countByCompanyId cuenta correctamente usuarios por compañía.
    // Se cuentan usuarios de compañías específicas y se verifica que el conteo es correcto.
    @Test
    void countByCompanyId_shouldCountUsersByCompany() {
        long company1Count = userRepository.countByCompanyId(companyId1);
        long company2Count = userRepository.countByCompanyId(companyId2);
        long nonExistentCompanyCount = userRepository.countByCompanyId(UUID.randomUUID());

        assertEquals(2, company1Count);
        assertEquals(1, company2Count);
        assertEquals(0, nonExistentCompanyCount);
    }

    //Tests para validaciones de constraints y unicidad

    // Verifica que el constraint de unicidad del email funciona correctamente.
    // Se intenta guardar un usuario con email duplicado y se verifica que falla.
    @Test
    void save_shouldFailWithDuplicateEmail() {
        User duplicateEmailUser = new User();
        duplicateEmailUser.setFirstName("Otro");
        duplicateEmailUser.setLastName("Usuario");
        duplicateEmailUser.setEmail("juan.perez@empresa1.com"); // Email duplicado
        duplicateEmailUser.setPassword("Password123!");
        duplicateEmailUser.setRoleId(1);
        duplicateEmailUser.setActive(true);

        assertThrows(Exception.class, () -> {
            userRepository.save(duplicateEmailUser);
            entityManager.flush();
        });
    }

    // Verifica que la entidad User tiene los campos requeridos no nulos.
    // Se intenta guardar un usuario sin campos requeridos y se verifica que falla.
    @Test
    void save_shouldFailWithNullRequiredFields() {
        User invalidUser = new User();
        // Dejamos campos requeridos como null

        assertThrows(Exception.class, () -> {
            userRepository.save(invalidUser);
            entityManager.flush();
        });
    }

    //Tests para operaciones de actualización

    // Verifica que los timestamps se actualizan correctamente al modificar un usuario.
    // Se modifica un usuario y se verifica que updatedAt se actualiza automáticamente.
    @Test
    void save_shouldUpdateTimestamp_whenUserIsModified() {
        User userToUpdate = userRepository.findById(testUser1.getId()).orElseThrow();
        OffsetDateTime originalUpdatedAt = userToUpdate.getUpdatedAt();
        
        // Esperamos un poco para asegurar que el timestamp sea diferente
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        userToUpdate.setFirstName("NuevoNombre");
        User updatedUser = userRepository.save(userToUpdate);
        entityManager.flush();

        assertTrue(updatedUser.getUpdatedAt().isAfter(originalUpdatedAt));
        assertEquals("NuevoNombre", updatedUser.getFirstName());
        assertEquals(originalUpdatedAt, updatedUser.getCreatedAt()); // createdAt no debe cambiar
    }

    //Tests para paginación avanzada

    // Verifica que la paginación funciona correctamente con diferentes tamaños de página.
    // Se prueban diferentes configuraciones de paginación y se verifican los resultados.
    @Test
    void findAll_shouldSupportPagination() {
        // Primera página con 2 elementos
        Pageable firstPage = PageRequest.of(0, 2);
        Page<User> firstPageResult = userRepository.findAll(firstPage);
        
        assertEquals(2, firstPageResult.getContent().size());
        assertEquals(3, firstPageResult.getTotalElements());
        assertEquals(2, firstPageResult.getTotalPages());
        assertTrue(firstPageResult.hasNext());
        assertFalse(firstPageResult.hasPrevious());

        // Segunda página con 2 elementos (debería tener 1 elemento)
        Pageable secondPage = PageRequest.of(1, 2);
        Page<User> secondPageResult = userRepository.findAll(secondPage);
        
        assertEquals(1, secondPageResult.getContent().size());
        assertEquals(3, secondPageResult.getTotalElements());
        assertEquals(2, secondPageResult.getTotalPages());
        assertFalse(secondPageResult.hasNext());
        assertTrue(secondPageResult.hasPrevious());
    }
}
