package info.magnolia.ui.dialog.actionpresenter.view;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import info.magnolia.ui.api.view.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/6/13
 * Time: 12:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class DialogActionViewImpl implements DialogActionView {

    private HorizontalLayout footer = new HorizontalLayout();

    private CssLayout primaryActionsContainer = new CssLayout();

    private CssLayout secondaryActionsContainer = new CssLayout();

    private CssLayout toolbarContainer = new CssLayout();

    private Map<String, View> actionNameToView = new HashMap<String, View>();

    public DialogActionViewImpl() {
        footer.addStyleName("footer");
        footer.addComponent(toolbarContainer);
        footer.addComponent(secondaryActionsContainer);
        footer.addComponent(primaryActionsContainer);
        footer.setExpandRatio(primaryActionsContainer, 1f);
        footer.setExpandRatio(secondaryActionsContainer, 1f);


        footer.setWidth("100%");
        secondaryActionsContainer.addStyleName("secondary-actions");
        primaryActionsContainer.addStyleName("primary-actions");
        secondaryActionsContainer.setWidth("100%");
        primaryActionsContainer.setWidth("100%");
    }

    @Override
    public Component asVaadinComponent() {
        return footer;
    }

    @Override
    public void addPrimaryAction(View actionView, String actionName) {
        actionNameToView.put(actionName, actionView);
        primaryActionsContainer.addComponentAsFirst(actionView.asVaadinComponent());
    }

    @Override
    public void addSecondaryAction(View actionView, String actionName) {
        actionNameToView.put(actionName, actionView);
        secondaryActionsContainer.addComponentAsFirst(actionView.asVaadinComponent());
    }

    @Override
    public void removeAllActions() {
        primaryActionsContainer.removeAllComponents();
        secondaryActionsContainer.removeAllComponents();
        actionNameToView.clear();
    }

    @Override
    public void setToolbarComponent(Component toolbar) {
        toolbarContainer.removeAllComponents();
        toolbarContainer.addComponent(toolbar);
    }

    @Override
    public View getViewForAction(String actionName) {
        return actionNameToView.get(actionName);
    }
}
