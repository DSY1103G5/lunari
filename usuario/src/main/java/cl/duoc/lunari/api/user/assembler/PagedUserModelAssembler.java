package cl.duoc.lunari.api.user.assembler;

import cl.duoc.lunari.api.user.controller.UserController;
import cl.duoc.lunari.api.user.dto.PagedUserRepresentation;
import cl.duoc.lunari.api.user.dto.UserRepresentation;
import cl.duoc.lunari.api.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class PagedUserModelAssembler {

    @Autowired
    private UserModelAssembler userModelAssembler;

    public PagedUserRepresentation toModel(@NonNull Page<User> userPage, 
                                          Boolean active, 
                                          Integer roleId, 
                                          UUID companyId,
                                          String sort) {
        PagedUserRepresentation representation = new PagedUserRepresentation();
        
        // Convert users to representations
        List<UserRepresentation> userRepresentations = userPage.getContent()
                .stream()
                .map(userModelAssembler::toModel)
                .toList();
        representation.setUsers(userRepresentations);
        
        // Set page information
        PagedUserRepresentation.PageInfo pageInfo = new PagedUserRepresentation.PageInfo();
        pageInfo.setTotalElements(userPage.getTotalElements());
        pageInfo.setTotalPages(userPage.getTotalPages());
        pageInfo.setCurrentPage(userPage.getNumber());
        pageInfo.setPageSize(userPage.getSize());
        pageInfo.setHasNext(userPage.hasNext());
        pageInfo.setHasPrevious(userPage.hasPrevious());
        pageInfo.setFirst(userPage.isFirst());
        pageInfo.setLast(userPage.isLast());
        representation.setPage(pageInfo);
        
        // Add navigation links
        addNavigationLinks(representation, userPage, active, roleId, companyId, sort);
        
        return representation;
    }

    private void addNavigationLinks(PagedUserRepresentation representation, 
                                   Page<User> userPage,
                                   Boolean active, 
                                   Integer roleId, 
                                   UUID companyId,
                                   String sort) {
        int currentPage = userPage.getNumber();
        int pageSize = userPage.getSize();
        
        // Self link
        representation.add(linkTo(methodOn(UserController.class)
                .getUsersPaginated(currentPage, pageSize, sort, active, roleId, companyId))
                .withSelfRel());
        
        // First page link
        representation.add(linkTo(methodOn(UserController.class)
                .getUsersPaginated(0, pageSize, sort, active, roleId, companyId))
                .withRel("first"));
        
        // Last page link
        representation.add(linkTo(methodOn(UserController.class)
                .getUsersPaginated(userPage.getTotalPages() - 1, pageSize, sort, active, roleId, companyId))
                .withRel("last"));
        
        // Previous page link
        if (userPage.hasPrevious()) {
            representation.add(linkTo(methodOn(UserController.class)
                    .getUsersPaginated(currentPage - 1, pageSize, sort, active, roleId, companyId))
                    .withRel("previous"));
        }
        
        // Next page link
        if (userPage.hasNext()) {
            representation.add(linkTo(methodOn(UserController.class)
                    .getUsersPaginated(currentPage + 1, pageSize, sort, active, roleId, companyId))
                    .withRel("next"));
        }
        
        // Additional useful links
        representation.add(linkTo(methodOn(UserController.class)
                .getAllRoles()).withRel("roles"));
        
        representation.add(linkTo(methodOn(UserController.class)
                .getUserStats()).withRel("stats"));
        
        representation.add(linkTo(methodOn(UserController.class)
                .searchUsers(null, 0, pageSize)).withRel("search"));
    }
}
