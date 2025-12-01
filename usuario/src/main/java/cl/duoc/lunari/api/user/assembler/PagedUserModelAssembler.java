package cl.duoc.lunari.api.user.assembler;

import cl.duoc.lunari.api.user.controller.RoleController;
import cl.duoc.lunari.api.user.controller.UserController;
import cl.duoc.lunari.api.user.dto.PagedUserRepresentation;
import cl.duoc.lunari.api.user.dto.UserRepresentation;
import cl.duoc.lunari.api.user.repository.DynamoDbPage;
import cl.duoc.lunari.api.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * Assembler para convertir DynamoDbPage<User> a PagedUserRepresentation con HATEOAS links.
 *
 * Cambios desde JPA:
 * - Page<User> → DynamoDbPage<User>
 * - Paginación basada en tokens en lugar de números de página
 * - Removido companyId de filtros
 * - roleId → roleName
 * - Links de navegación usando tokens en lugar de números de página
 */
@Component
public class PagedUserModelAssembler {

    @Autowired
    private UserModelAssembler userModelAssembler;

    public PagedUserRepresentation toModel(@NonNull DynamoDbPage<User> userPage,
                                          Boolean active,
                                          String roleName) {
        PagedUserRepresentation representation = new PagedUserRepresentation();

        // Convert users to representations
        List<UserRepresentation> userRepresentations = userPage.getItems()
                .stream()
                .map(userModelAssembler::toModel)
                .toList();
        representation.setUsers(userRepresentations);

        // Set page information (DynamoDB style)
        PagedUserRepresentation.PageInfo pageInfo = new PagedUserRepresentation.PageInfo();
        pageInfo.setCount(userPage.getCount());
        pageInfo.setLimit(userPage.getLimit());
        pageInfo.setNextPaginationToken(userPage.getNextPaginationToken());
        pageInfo.setHasMore(userPage.isHasMoreResults());
        representation.setPage(pageInfo);

        // Add navigation links
        addNavigationLinks(representation, userPage, active, roleName);

        return representation;
    }

    private void addNavigationLinks(PagedUserRepresentation representation,
                                   DynamoDbPage<User> userPage,
                                   Boolean active,
                                   String roleName) {
        int limit = userPage.getLimit();
        String nextToken = userPage.getNextPaginationToken();

        // Self link (current page)
        representation.add(linkTo(methodOn(UserController.class)
                .getUsersPaginated(limit, null, active, roleName))
                .withSelfRel());

        // First page link
        representation.add(linkTo(methodOn(UserController.class)
                .getUsersPaginated(limit, null, active, roleName))
                .withRel("first"));

        // Next page link (only if there are more results)
        if (userPage.isHasMoreResults() && nextToken != null) {
            representation.add(linkTo(methodOn(UserController.class)
                    .getUsersPaginated(limit, nextToken, active, roleName))
                    .withRel("next"));
        }

        // Additional useful links
        representation.add(linkTo(RoleController.class).withRel("roles"));

        representation.add(linkTo(methodOn(UserController.class)
                .getUserStats()).withRel("stats"));

        representation.add(linkTo(methodOn(UserController.class)
                .searchUsers(null, limit, null)).withRel("search"));
    }
}
