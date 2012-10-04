package info.magnolia.ui.app.showcase.main;

import com.google.inject.Inject;

import info.magnolia.ui.framework.view.View;

public class UnsupportedPresenter implements UnsupportedView.Listener {

    private UnsupportedView view;
    
    @Inject
    public UnsupportedPresenter(UnsupportedView unsupportedView) {
        this.view = unsupportedView;
    }
        
    public View start() {
        return view;
    }

}
