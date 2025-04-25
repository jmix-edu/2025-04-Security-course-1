package com.company.jmixpmsecurity.security;

import com.company.jmixpmsecurity.entity.Project;
import com.company.jmixpmsecurity.entity.Task;
import com.company.jmixpmsecurity.entity.dashboard.DashboardProject;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "AnonymousRole", code = AnonymousRole.CODE, scope = "UI")
public interface AnonymousRole {
    String CODE = "anonymous-role";

    @MenuPolicy(menuIds = "Task_.list")
    @ViewPolicy(viewIds = {"Task_.list", "MainView", "ProjectsDashboardView"})
    void screens();

    @EntityAttributePolicy(entityClass = Project.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Project.class, actions = EntityPolicyAction.READ)
    void project();

    @EntityAttributePolicy(entityClass = Task.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = Task.class, actions = EntityPolicyAction.READ)
    void task();

    @EntityAttributePolicy(entityClass = DashboardProject.class, attributes = "*", action = EntityAttributePolicyAction.VIEW)
    @EntityPolicy(entityClass = DashboardProject.class, actions = EntityPolicyAction.READ)
    void dashboardProject();
}