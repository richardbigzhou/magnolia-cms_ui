package info.magnolia.ui.contentapp.movedialog.view;

import info.magnolia.ui.dialog.actionpresenter.view.DialogActionView;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/7/13
 * Time: 8:21 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MoveDialogActionView extends DialogActionView {

    void setActionEnabled(String actionName, boolean isEnabled);

}
