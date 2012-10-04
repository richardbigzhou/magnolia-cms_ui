package info.magnolia.ui.app.showcase.main;

import com.google.inject.Inject;

import info.magnolia.ui.framework.view.View;

public class VaadinPresenter implements VaadinView.Listener {

    private VaadinView view;
    
    @Inject
    public VaadinPresenter(VaadinView vaadinView) {
        this.view = vaadinView;
    }
    
    public View start() {
        return view;
    }

}
