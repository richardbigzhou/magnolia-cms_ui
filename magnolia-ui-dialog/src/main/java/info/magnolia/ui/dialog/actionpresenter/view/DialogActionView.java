package info.magnolia.ui.dialog.actionpresenter.view;

import com.vaadin.ui.Component;
import info.magnolia.ui.api.view.View;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/6/13
 * Time: 12:37 AM
 * To change this template use File | Settings | File Templates.
 */
public interface DialogActionView extends View {

    void addPrimaryAction(View actionView, String actionName);

    void addSecondaryAction(View actionView, String actionName);

    void removeAllActions();

    void setToolbarComponent(Component toolbar);

    View getViewForAction(String actionName);
}
