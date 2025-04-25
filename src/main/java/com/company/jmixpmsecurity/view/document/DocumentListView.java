package com.company.jmixpmsecurity.view.document;

import com.company.jmixpmsecurity.entity.Document;
import com.company.jmixpmsecurity.view.main.MainView;
import com.vaadin.flow.router.Route;
import io.jmix.core.LoadContext;
import io.jmix.core.UnconstrainedDataManager;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "documents", layout = MainView.class)
@ViewController("Document.list")
@ViewDescriptor("document-list-view.xml")
@LookupComponent("documentsDataGrid")
@DialogMode(width = "64em")
public class DocumentListView extends StandardListView<Document> {
//    @Autowired
//    private UnconstrainedDataManager unconstrainedDataManager;
//
//    @Install(to = "documentsDl", target = Target.DATA_LOADER)
//    private List<Document> documentsDlLoadDelegate(final LoadContext<Document> loadContext) {
//        return unconstrainedDataManager.loadList(loadContext);
//    }
}