package info.magnolia.ui.admincentral.workbench;

import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;

import javax.inject.Inject;

import com.vaadin.ui.Component;

@SuppressWarnings("serial")
public class Workbench implements IsVaadinComponent, WorkbenchView.Presenter {

    private WorkbenchView view; 
    
    @Inject
    public Workbench(final WorkbenchView view) {
        super();
        this.view = view;
        view.setPresenter(this);        
    }
    
    public void initWorkbench(final String id) {
        view.initWorkbench(id);
    }
    
    @Override
    public Component asVaadinComponent() {
        return view;
    }

}
