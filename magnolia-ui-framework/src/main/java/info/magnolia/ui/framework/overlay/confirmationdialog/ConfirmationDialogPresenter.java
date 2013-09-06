package info.magnolia.ui.framework.overlay.confirmationdialog;

import info.magnolia.ui.api.overlay.ConfirmationCallback;
import info.magnolia.ui.api.view.View;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/6/13
 * Time: 4:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfirmationDialogPresenter {

    public ConfirmationDialogView start(View content, boolean confirmByDefault, ConfirmationCallback callback) {
        return new ConfirmationDialogView(callback);
    }
}


