package info.magnolia.ui.dialog.choosedialog.action;

import info.magnolia.ui.dialog.action.CallbackDialogActionDefinition;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 8/30/13
 * Time: 4:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChooseDialogActionDefinition extends CallbackDialogActionDefinition {

    public ChooseDialogActionDefinition() {
        setImplementationClass(ChooseDialogAction.class);
    }
}
