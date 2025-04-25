package com.company.jmixpmsecurity.security;

import com.company.jmixpmsecurity.entity.Document;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.ResourceRole;

@ResourceRole(name = "DemoRole", code = DemoRole.CODE, scope = "UI")
public interface DemoRole {
    String CODE = "demo-role";

    @EntityAttributePolicy(entityClass = Document.class, attributes = "*", action = EntityAttributePolicyAction.MODIFY)
    @EntityPolicy(entityClass = Document.class, actions = EntityPolicyAction.ALL)
    void document();
}