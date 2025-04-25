package com.company.jmixpmsecurity.view.project;

import com.company.jmixpmsecurity.entity.Project;
import com.company.jmixpmsecurity.security.specific.JmixPmProjectArchiveContext;
import com.company.jmixpmsecurity.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.AccessManager;
import io.jmix.core.DataManager;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionLoader;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "projects", layout = MainView.class)
@ViewController("Project.list")
@ViewDescriptor("project-list-view.xml")
@LookupComponent("projectsDataGrid")
@DialogMode(width = "64em")
public class ProjectListView extends StandardListView<Project> {
    @ViewComponent
    private CollectionLoader<Project> projectsDl;
    @ViewComponent
    private DataGrid<Project> projectsDataGrid;
    @Autowired
    private DataManager dataManager;

    @Autowired
    private Notifications notifications;
    private boolean hideArchived;
    @Autowired
    private AccessManager accessManager;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        if (hideArchived) {
            projectsDl.setQuery("select e from Project e where e.archived = false");
        }
    }

    @Subscribe("projectsDataGrid.archive")
    public void onProjectsTableArchive(ActionPerformedEvent event) {
        Project project = projectsDataGrid.getSingleSelectedItem();
        if (project == null) {
            return;
        }
        project.setArchived(true);
        dataManager.save(project);

        projectsDl.load();

        notifications.create("Project " + project.getName() + " has been archived")
                .show();
    }

    public void setHideArchived(boolean hideArchived) {
        this.hideArchived = hideArchived;
    }

    @Install(to = "projectsDataGrid.archive", subject = "enabledRule")
    private boolean projectsDataGridArchiveEnabledRule() {
        JmixPmProjectArchiveContext context = new JmixPmProjectArchiveContext();
        accessManager.applyRegisteredConstraints(context);
        return context.isPermitted();
    }
}