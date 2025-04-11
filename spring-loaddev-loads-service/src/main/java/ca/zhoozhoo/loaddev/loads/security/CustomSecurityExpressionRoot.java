package ca.zhoozhoo.loaddev.loads.security;

import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import ca.zhoozhoo.loaddev.loads.dao.GroupRepository;
import ca.zhoozhoo.loaddev.loads.dao.LoadRepository;
import ca.zhoozhoo.loaddev.loads.dao.ShotRepository;
import lombok.Setter;
import reactor.core.publisher.Mono;

public class CustomSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

    private Object filterObject;
    private Object returnObject;

    @Setter
    private GroupRepository groupRepository;

    @Setter
    private LoadRepository loadRepository;

    @Setter
    private ShotRepository shotRepository;

    public CustomSecurityExpressionRoot(Authentication authentication) {
        super(authentication);
    }

    public Mono<Boolean> isGroupOwner(Long groupId) {
        String currentUserId = ((JwtAuthenticationToken) getAuthentication()).getToken().getSubject();
        return groupRepository.findById(groupId)
                .map(group -> group.ownerId().equals(currentUserId))
                .defaultIfEmpty(false);
    }

    public Mono<Boolean> isLoadOwner(Long loadId) {
        String currentUserId = ((JwtAuthenticationToken) getAuthentication()).getToken().getSubject();
        return loadRepository.findById(loadId)
                .map(load -> load.ownerId().equals(currentUserId))
                .defaultIfEmpty(false);
    }

    public Mono<Boolean> isShotOwner(Long shotId) {
        String currentUserId = ((JwtAuthenticationToken) getAuthentication()).getToken().getSubject();
        return shotRepository.findById(shotId)
                .map(shot -> shot.ownerId().equals(currentUserId))
                .defaultIfEmpty(false);
    }

    @Override
    public Object getFilterObject() {
        return this.filterObject;
    }

    @Override
    public Object getReturnObject() {
        return this.returnObject;
    }

    @Override
    public void setFilterObject(Object obj) {
        this.filterObject = obj;
    }

    @Override
    public void setReturnObject(Object obj) {
        this.returnObject = obj;
    }

    @Override
    public Object getThis() {
        return this;
    }
}