package info.magnolia.ui.dialog.actionpresenter.view;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import info.magnolia.ui.api.view.View;

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

    public DialogActionViewImpl() {
        footer.addComponent(secondaryActionsContainer);
        footer.addComponent(primaryActionsContainer);

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
    public void addPrimaryAction(View actionView) {
        primaryActionsContainer.addComponentAsFirst(actionView.asVaadinComponent());
    }

    @Override
    public void addSecondaryAction(View actionView) {
        secondaryActionsContainer.addComponentAsFirst(actionView.asVaadinComponent());
    }

    @Override
    public void removeAllActions() {
        primaryActionsContainer.removeAllComponents();
        secondaryActionsContainer.removeAllComponents();
    }
}
