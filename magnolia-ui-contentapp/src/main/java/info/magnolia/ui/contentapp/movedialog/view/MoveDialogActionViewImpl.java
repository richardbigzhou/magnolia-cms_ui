package info.magnolia.ui.contentapp.movedialog.view;

import com.vaadin.ui.Component;
import info.magnolia.ui.dialog.actionpresenter.view.DialogActionViewImpl;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/7/13
 * Time: 8:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class MoveDialogActionViewImpl extends DialogActionViewImpl implements MoveDialogActionView {

    @Override
    public void setActionEnabled(String actionName, boolean isEnabled) {
        Component actionComponent = getViewForAction(actionName).asVaadinComponent();
        actionComponent.setEnabled(isEnabled);
    }
}
