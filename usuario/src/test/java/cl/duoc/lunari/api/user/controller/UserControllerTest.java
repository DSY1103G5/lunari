package cl.duoc.lunari.api.user.controller;

import cl.duoc.lunari.api.user.assembler.UserModelAssembler;
import cl.duoc.lunari.api.user.assembler.PagedUserModelAssembler;
import cl.duoc.lunari.api.user.dto.UserRepresentation;
import cl.duoc.lunari.api.user.model.User;
import cl.duoc.lunari.api.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserModelAssembler userModelAssembler;

    @MockitoBean
    private PagedUserModelAssembler pagedUserModelAssembler;

    // Se usa para convertir objetos Java a JSON y viceversa en los tests de API
    private final ObjectMapper objectMapper = new ObjectMapper();

    private User testUser1;
    private User testUser2;
    private UserRepresentation testUserRepresentation1;
    private UserRepresentation testUserRepresentation2;
    private UUID userId1 = UUID.fromString("a1b2c3d4-e5f6-7890-1234-567890abcdef");
    private UUID userId2 = UUID.fromString("b2c3d4e5-f6a7-8901-2345-67890abcdef0");

    @BeforeEach
    void setUp() {
        testUser1 = new User();
        testUser1.setId(userId1);
        testUser1.setEmail("test1@example.com");
        testUser1.setFirstName("John");
        testUser1.setLastName("Doe");
        testUser1.setPassword("SecureP@ss1");
        testUser1.setRoleId(1); 
        testUser1.setActive(true);
        testUser1.setVerified(true);
        testUser1.setCreatedAt(OffsetDateTime.now()); 
        testUser1.setUpdatedAt(OffsetDateTime.now());

        testUser2 = new User();
        testUser2.setId(userId2);
        testUser2.setEmail("test2@example.com");
        testUser2.setFirstName("Jane");
        testUser2.setLastName("Smith");
        testUser2.setPassword("SecureP@ss2");
        testUser2.setRoleId(2);
        testUser2.setActive(true);
        testUser2.setVerified(false);
        testUser2.setCreatedAt(OffsetDateTime.now());
        testUser2.setUpdatedAt(OffsetDateTime.now());

        // UserRepresentation para HATEOAS
        testUserRepresentation1 = new UserRepresentation();
        testUserRepresentation1.setId(userId1);
        testUserRepresentation1.setEmail("test1@example.com");
        testUserRepresentation1.setFirstName("John");
        testUserRepresentation1.setLastName("Doe");
        testUserRepresentation1.setActive(true);
        testUserRepresentation1.setVerified(true);

        testUserRepresentation2 = new UserRepresentation();
        testUserRepresentation2.setId(userId2);
        testUserRepresentation2.setEmail("test2@example.com");
        testUserRepresentation2.setFirstName("Jane");
        testUserRepresentation2.setLastName("Smith");
        testUserRepresentation2.setActive(true);
        testUserRepresentation2.setVerified(false);
    }

    //Aquí comienza la zona de testeos. Los primeros test a aplicar corresponden a los endpoint que usan el método GET

    //Test que se encarga de comprobar que getAllUsers devuelve una lista de usuarios con HATEOAS.
    //Este test comprueba que el endpoint /api/v1/users devuelve una lista de usuarios
    //y que la respuesta es correcta, incluyendo los campos id y email de cada usuario.
    @Test
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        List<User> users = Arrays.asList(testUser1, testUser2);
        
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(users);
        when(userModelAssembler.toModel(testUser1)).thenReturn(testUserRepresentation1);
        when(userModelAssembler.toModel(testUser2)).thenReturn(testUserRepresentation2);

        mockMvc.perform(get("/api/v1/users")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response.content").isArray())
                .andExpect(jsonPath("$.response.content[0].id").value(userId1.toString()))
                .andExpect(jsonPath("$.response.content[0].email").value("test1@example.com"))
                .andExpect(jsonPath("$.response.content[1].id").value(userId2.toString()))
                .andExpect(jsonPath("$.response.content[1].email").value("test2@example.com"))
                .andExpect(jsonPath("$.response.links").exists());
    }

    //Test que comprueba que getUserById devuelve un usuario específico cuando existe con HATEOAS.
    //Este test verifica que el endpoint /api/v1/users/{id} devuelve un usuario
    //con el ID especificado y que la respuesta es correcta, incluyendo los campos id y email del usuario.
    @Test
    void getUserById_shouldReturnUser_whenUserExists() throws Exception {
        when(userService.getUserById(userId1)).thenReturn(Optional.of(testUser1));
        when(userModelAssembler.toModel(any(User.class))).thenReturn(testUserRepresentation1);

        mockMvc.perform(get("/api/v1/users/{id}", userId1)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response.id").value(userId1.toString()))
                .andExpect(jsonPath("$.response.email").value("test1@example.com"))
                .andExpect(jsonPath("$.response.fullName").value("John Doe"));
    }

    //Test que comprueba que getUserById devuelve un error 404 cuando el usuario no existe.
    //Este test verifica que el endpoint /api/v1/users/{id} devuelve un error 404 Not Found
    //cuando se intenta obtener un usuario con un ID que no existe,
    //y que la respuesta contiene un mensaje de error adecuado.
    @Test
    void getUserById_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        when(userService.getUserById(userId1)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/users/{id}", userId1)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));
    }

    //Testeando el endpoint que obtiene un usuario por su email con HATEOAS.
    //Este test verifica que el endpoint /api/v1/users/email devuelve un usuario
    //cuando se proporciona un email existente, y que la respuesta es correcta,
    //incluyendo los campos id y email del usuario.
    @Test
    void getUserByEmail_shouldReturnUser_whenEmailExists() throws Exception {
        String existingEmail = "test1@example.com";
        when(userService.getUserByEmail(eq(existingEmail))).thenReturn(Optional.of(testUser1));
        when(userModelAssembler.toModel(any(User.class))).thenReturn(testUserRepresentation1);

        mockMvc.perform(get("/api/v1/users/email")
                .param("email", existingEmail)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response.id").value(userId1.toString()))
                .andExpect(jsonPath("$.response.email").value(existingEmail));
    }

    //Testeando el endpoint que obtiene un usuario por su email.
    //Este test verifica que el endpoint /api/v1/users/email devuelve un error 404
    //cuando se intenta obtener un usuario con un email que no existe,
    //y que la respuesta contiene un mensaje de error adecuado.
    @Test
    void getUserByEmail_shouldReturnNotFound_whenEmailDoesNotExist() throws Exception {
        String nonExistingEmail = "nonexistent@example.com";
        when(userService.getUserByEmail(eq(nonExistingEmail))).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/users/email")
                .param("email", nonExistingEmail)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado con ese email"));
    }

    //Testeando el endpoint que obtiene un usuario por su email.
    //Este test verifica que el endpoint /api/v1/users/email devuelve un error 400
    //cuando se intenta obtener un usuario sin proporcionar un email,
    //y que la respuesta contiene un mensaje de error adecuado.
    @Test
    void getUserByEmail_shouldReturnBadRequest_whenEmailIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/users/email")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isBadRequest());
    }

    //Testeando el endpoint que obtiene un usuario por su email.
    //Este test verifica que el endpoint /api/v1/users/email devuelve un error 400
    //cuando se intenta obtener un usuario con un email vacío,
    //y que la respuesta contiene un mensaje de error adecuado.
    @Test
    void getUserByEmail_shouldReturnBadRequest_whenEmailIsEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/users/email")
                .param("email", "")
                .accept(MediaTypes.HAL_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false)); 
    }

    //Testeando el endpoint que obtiene un usuario por su email.
    //Este test verifica que el endpoint /api/v1/users/email devuelve un error 400
    //cuando se intenta obtener un usuario con un email con formato inválido,
    //y que la respuesta contiene un mensaje de error adecuado.
    //Si tu controlador usa @Email en el parámetro o tu servicio valida y lanza una excepción,
    //este test también lo contempla.
    //Si tu API devuelve un mensaje específico para formato inválido, puedes descomentar la línea correspondiente.
    @Test
    void getUserByEmail_shouldReturnBadRequest_whenEmailFormatIsInvalid() throws Exception {
        String invalidEmail = "invalid-email-format";
        mockMvc.perform(get("/api/v1/users/email")
                .param("email", invalidEmail)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    //Testeando los POST con HATEOAS.

    //Testeando el endpoint que registra un nuevo usuario con HATEOAS.
    //Este test verifica que el endpoint /api/v1/users/register crea un nuevo usuario
    //cuando se proporciona un objeto User válido, y que la respuesta es correcta,
    //incluyendo los campos id, email y otros del usuario creado.
    @Test
    void registerUser_shouldReturnCreatedUser_whenValidData() throws Exception {
        User newUser = new User();
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setEmail("new.user@example.com");
        newUser.setPassword("Password123!");
        newUser.setRoleId(1);

        User createdUser = new User();
        createdUser.setId(UUID.randomUUID()); 
        createdUser.setFirstName(newUser.getFirstName());
        createdUser.setLastName(newUser.getLastName());
        createdUser.setEmail(newUser.getEmail());
        createdUser.setRoleId(newUser.getRoleId());
        createdUser.setActive(true);
        createdUser.setVerified(false);
        createdUser.setCreatedAt(OffsetDateTime.now());
        createdUser.setUpdatedAt(OffsetDateTime.now());

        UserRepresentation createdUserRepresentation = new UserRepresentation();
        createdUserRepresentation.setId(createdUser.getId());
        createdUserRepresentation.setFirstName(newUser.getFirstName());
        createdUserRepresentation.setLastName(newUser.getLastName());
        createdUserRepresentation.setEmail(newUser.getEmail());
        createdUserRepresentation.setActive(true);
        createdUserRepresentation.setVerified(false);

        when(userService.createUser(any(User.class))).thenReturn(createdUser);
        when(userModelAssembler.toModel(any(User.class))).thenReturn(createdUserRepresentation);

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response.email").value(newUser.getEmail()))
                .andExpect(jsonPath("$.response.id").exists());
    }

    //Testeando el endpoint que registra un nuevo usuario.
    //Este test verifica que el endpoint /api/v1/users/register devuelve un error 409
    //cuando se intenta registrar un usuario con un email que ya está registrado,
    //y que la respuesta contiene un mensaje de error adecuado.
    //En este caso, se lanza una RuntimeException con un mensaje específico.
    @Test
    void registerUser_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
        User existingUserAttempt = new User();
        existingUserAttempt.setFirstName("Existing");
        existingUserAttempt.setLastName("User");
        existingUserAttempt.setEmail("test1@example.com");
        existingUserAttempt.setPassword("SomePass1!");
        existingUserAttempt.setRoleId(1);

        when(userService.createUser(any(User.class)))
                .thenThrow(new RuntimeException("Email 'test1@example.com' ya está registrado."));

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(existingUserAttempt)))
                .andExpect(status().isConflict()) 
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email 'test1@example.com' ya está registrado."));
    }

    //Testeando el endpoint que registra un nuevo usuario.
    //Este test verifica que el endpoint /api/v1/users/register devuelve un error 400
    //cuando se intenta registrar un usuario sin proporcionar un campo requerido,
    //y que la respuesta contiene un mensaje de error adecuado.
    @Test
    void registerUser_shouldReturnBadRequest_whenRequiredFieldIsMissing() throws Exception {
        User userMissingFirstName = new User();
        userMissingFirstName.setLastName("Missing");
        userMissingFirstName.setEmail("missing@example.com");
        userMissingFirstName.setPassword("ValidPass1!");
        userMissingFirstName.setRoleId(1);

        // Mock the service to throw a validation exception
        when(userService.createUser(any(User.class)))
                .thenThrow(new RuntimeException("El nombre es requerido"));

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(userMissingFirstName)))
                .andExpect(status().isBadRequest());
    }

    //Testeando el endpoint que registra un nuevo usuario.
    //Este test verifica que el endpoint /api/v1/users/register devuelve un error 400
    //cuando se intenta registrar un usuario con una contraseña demasiado corta,
    //y que la respuesta contiene un mensaje de error adecuado.
    @Test
    void registerUser_shouldReturnBadRequest_whenPasswordIsTooShort() throws Exception {
        User userShortPassword = new User();
        userShortPassword.setFirstName("Short");
        userShortPassword.setLastName("Pass");
        userShortPassword.setEmail("shortpass@example.com");
        userShortPassword.setPassword("short"); 
        userShortPassword.setRoleId(1);

        // Mock the service to throw a validation exception
        when(userService.createUser(any(User.class)))
                .thenThrow(new RuntimeException("La contraseña debe tener al menos 8 caracteres"));

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(userShortPassword)))
                .andExpect(status().isBadRequest());
    }

    //Testeando el endpoint que registra un nuevo usuario.
    //Este test verifica que el endpoint /api/v1/users/register devuelve un error 400
    //cuando se intenta registrar un usuario con un email con formato inválido,
    //y que la respuesta contiene un mensaje de error adecuado.
    @Test
    void registerUser_shouldReturnBadRequest_whenEmailFormatIsInvalid() throws Exception {
        User userInvalidEmail = new User();
        userInvalidEmail.setFirstName("Invalid");
        userInvalidEmail.setLastName("Email");
        userInvalidEmail.setEmail("invalid-email");
        userInvalidEmail.setPassword("Password123!");
        userInvalidEmail.setRoleId(1);

        // Mock the service to throw a validation exception
        when(userService.createUser(any(User.class)))
                .thenThrow(new RuntimeException("El formato del email es inválido"));

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(userInvalidEmail)))
                .andExpect(status().isBadRequest());
    }

    // Testeando los DELETE con HATEOAS

    //Testeando el endpoint que elimina un usuario.
    //Este test verifica que el endpoint /api/v1/users/{id} elimina un usuario
    //cuando se proporciona un ID de usuario existente, y que la respuesta es correcta,
    //incluyendo un status 204 No Content.
    @Test
    void deleteUser_shouldReturnNoContent_whenUserExists() throws Exception {
        UUID userIdToDelete = testUser1.getId();

        doNothing().when(userService).deleteUser(eq(userIdToDelete));

        mockMvc.perform(delete("/api/v1/users/{id}", userIdToDelete)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isNoContent()); 
    }

    //Testeando el endpoint que elimina un usuario.
    //Este test verifica que el endpoint /api/v1/users/{id} devuelve un error 404
    //cuando se intenta eliminar un usuario con un ID que no existe,
    //y que la respuesta contiene un mensaje de error adecuado.
    //En este caso, se lanza una RuntimeException con un mensaje específico.
    @Test
    void deleteUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        doThrow(new RuntimeException("Usuario no encontrado con ID: " + nonExistentId))
                .when(userService).deleteUser(eq(nonExistentId));

        mockMvc.perform(delete("/api/v1/users/{id}", nonExistentId)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));
    }    

    //Los test para los PUT con HATEOAS

    //Testeando el endpoint que actualiza un usuario con HATEOAS.
    //Este test verifica que el endpoint /api/v1/users/{id} actualiza un usuario
    //cuando se proporciona un ID de usuario existente y datos válidos,
    //y que la respuesta es correcta, incluyendo los campos actualizados del usuario.
    @Test
    void updateUser_shouldReturnUpdatedUser_whenUserExistsAndValidData() throws Exception {
        User updatedUserData = new User();
        updatedUserData.setFirstName("Updated");
        updatedUserData.setLastName("User");
        updatedUserData.setEmail("updated.email@example.com");
        updatedUserData.setPassword("NewSecureP@ss1!");
        updatedUserData.setPhone("1234567890");
        updatedUserData.setRoleId(3);

        User existingUser = testUser1;
        User returnedUpdatedUser = new User();
        returnedUpdatedUser.setId(existingUser.getId());
        returnedUpdatedUser.setFirstName(updatedUserData.getFirstName());
        returnedUpdatedUser.setLastName(updatedUserData.getLastName()); 
        returnedUpdatedUser.setEmail(updatedUserData.getEmail());
        returnedUpdatedUser.setPhone(updatedUserData.getPhone());
        returnedUpdatedUser.setRoleId(updatedUserData.getRoleId());
        returnedUpdatedUser.setActive(true);
        returnedUpdatedUser.setVerified(true);
        returnedUpdatedUser.setCreatedAt(existingUser.getCreatedAt());
        returnedUpdatedUser.setUpdatedAt(OffsetDateTime.now());

        UserRepresentation updatedUserRepresentation = new UserRepresentation();
        updatedUserRepresentation.setId(existingUser.getId());
        updatedUserRepresentation.setFirstName(updatedUserData.getFirstName());
        updatedUserRepresentation.setLastName(updatedUserData.getLastName());
        updatedUserRepresentation.setEmail(updatedUserData.getEmail());
        updatedUserRepresentation.setActive(true);
        updatedUserRepresentation.setVerified(true);

        when(userService.updateUser(eq(existingUser.getId()), any(User.class)))
            .thenReturn(returnedUpdatedUser);
        when(userModelAssembler.toModel(any(User.class))).thenReturn(updatedUserRepresentation);

        mockMvc.perform(put("/api/v1/users/{id}", existingUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(updatedUserData)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response.id").value(existingUser.getId().toString()))
                .andExpect(jsonPath("$.response.firstName").value(updatedUserData.getFirstName()))
                .andExpect(jsonPath("$.response.email").value(updatedUserData.getEmail()));
    }

    //Testeando el endpoint que actualiza un usuario.
    //Este test verifica que el endpoint /api/v1/users/{id} devuelve un error 404
    //cuando se intenta actualizar un usuario con un ID que no existe,
    //y que la respuesta contiene un mensaje de error adecuado.
    //En este caso, se lanza una RuntimeException con un mensaje específico.
    @Test
    void updateUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        User updatedUserData = new User();
        updatedUserData.setFirstName("NonExistent");
        updatedUserData.setLastName("User");
        updatedUserData.setEmail("nonexistent@example.com");
        updatedUserData.setPassword("Pass1234!");
        updatedUserData.setRoleId(1);

        when(userService.updateUser(eq(nonExistentId), any(User.class)))
            .thenThrow(new RuntimeException("Usuario no encontrado"));

        mockMvc.perform(put("/api/v1/users/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(updatedUserData)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));
    }

    //Testeando el endpoint que actualiza un usuario.
    //Este test verifica que el endpoint /api/v1/users/{id} devuelve un error 400
    //cuando se intenta actualizar un usuario con datos inválidos, como un campo requerido
    //faltante, y que la respuesta contiene un mensaje de error adecuado.
    @Test
    void updateUser_shouldReturnBadRequest_whenInvalidEmailFormat() throws Exception {
        UUID userIdToUpdate = testUser1.getId();
        User updatedUserData = new User();
        updatedUserData.setFirstName("Invalid");
        updatedUserData.setEmail("invalid-email-format");
        updatedUserData.setPassword("ValidPass123!");
        updatedUserData.setRoleId(1);

        // Mock the service to throw a validation exception
        when(userService.updateUser(eq(userIdToUpdate), any(User.class)))
                .thenThrow(new RuntimeException("El formato del email es inválido"));

        mockMvc.perform(put("/api/v1/users/{id}", userIdToUpdate)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(updatedUserData)))
                .andExpect(status().isNotFound());
    }

    //Testeando el endpoint que actualiza un usuario.
    //Este test verifica que el endpoint /api/v1/users/{id} devuelve un error 400
    //cuando se intenta actualizar un usuario con una contraseña demasiado corta,
    //y que la respuesta contiene un mensaje de error adecuado.
    @Test
    void updateUser_shouldReturnBadRequest_whenRequiredFieldIsEmpty() throws Exception {
        UUID userIdToUpdate = testUser1.getId();
        User updatedUserData = new User();
        updatedUserData.setFirstName("");
        updatedUserData.setLastName("Updated");
        updatedUserData.setEmail("valid@email.com");
        updatedUserData.setPassword("ValidPass123!");
        updatedUserData.setRoleId(1);

        mockMvc.perform(put("/api/v1/users/{id}", userIdToUpdate)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaTypes.HAL_JSON)
                .content(objectMapper.writeValueAsString(updatedUserData)));
    }

    // Tests específicos de HATEOAS

    @Test
    public void testGetUserByIdReturnsHateoasLinks() throws Exception {
        // Arrange
        when(userService.getUserById(userId1)).thenReturn(Optional.of(testUser1));
        when(userModelAssembler.toModel(any(User.class))).thenReturn(testUserRepresentation1);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/{id}", userId1)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response.id").value(userId1.toString()))
                .andExpect(jsonPath("$.response.firstName").value("John"))
                .andExpect(jsonPath("$.response.lastName").value("Doe"))
                .andExpect(jsonPath("$.response.email").value("test1@example.com"))
                .andExpect(jsonPath("$.response.fullName").value("John Doe"))
                .andExpect(jsonPath("$.response.status").value("active"));
    }

    @Test
    public void testApiRootEndpointReturnsNavigationLinks() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/users/api")
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response.message").value("User API - HATEOAS habilitado"))
                .andExpect(jsonPath("$.response.links.getAllUsers").exists())
                .andExpect(jsonPath("$.response.links.getPaginatedUsers").exists())
                .andExpect(jsonPath("$.response.links.searchUsers").exists())
                .andExpect(jsonPath("$.response.links.getUserById").exists())
                .andExpect(jsonPath("$.response.links.getUserByEmail").exists())
                .andExpect(jsonPath("$.response.links.registerUser").exists())
                .andExpect(jsonPath("$.response.links.getAllRoles").exists())
                .andExpect(jsonPath("$.response.links.getUserStats").exists());
    }

    @Test
    public void testUserNotFoundReturns404() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userService.getUserById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/{id}", userId)
                .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Usuario no encontrado"));
    }
}
