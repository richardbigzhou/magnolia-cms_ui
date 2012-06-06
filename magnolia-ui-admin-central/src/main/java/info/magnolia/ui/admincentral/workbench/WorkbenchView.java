package info.magnolia.ui.admincentral.workbench;

import com.vaadin.ui.ComponentContainer;

public interface WorkbenchView extends ComponentContainer {

    void setPresenter(final Presenter presenter);
    
    void initWorkbench(final String id);
    
    public interface Presenter {
        
    }

}
