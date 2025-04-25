package com.company.jmixpmsecurity.view.project;

import com.company.jmixpmsecurity.entity.Project;
import com.company.jmixpmsecurity.entity.User;
import com.company.jmixpmsecurity.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.usersubstitution.CurrentUserSubstitution;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "projects/:id", layout = MainView.class)
@ViewController("Project.detail")
@ViewDescriptor("project-detail-view.xml")
@EditedEntityContainer("projectDc")
public class ProjectDetailView extends StandardDetailView<Project> {
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private CurrentUserSubstitution currentUserSubstitution;

    @Subscribe
    public void onInitEntity(InitEntityEvent<Project> event) {
//        User user = (User) currentAuthentication.getUser();
        final User user = (User) currentUserSubstitution.getEffectiveUser();
        event.getEntity().setManager(user);
    }
}