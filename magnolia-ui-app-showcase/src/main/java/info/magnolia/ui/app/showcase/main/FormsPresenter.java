package info.magnolia.ui.app.showcase.main;

import com.google.inject.Inject;

import info.magnolia.ui.framework.view.View;

public class FormsPresenter implements FormsView.Listener{

    private FormsView view;
    
    @Inject
    public FormsPresenter(FormsView formsView) {        
        this.view = formsView;
    }
    
    public View start() {        
        return view;
    }
}
