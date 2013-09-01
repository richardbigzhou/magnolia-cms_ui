package info.magnolia.ui.admincentral.dialog.action;

import com.vaadin.data.Item;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.action.ConfiguredActionDefinition;
import info.magnolia.ui.dialog.ChooseDialogCallback;

import javax.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 8/29/13
 * Time: 10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChooseDialogAction extends AbstractAction<CallbackDialogActionDefinition> {

    private Item item;

    @Inject
    public ChooseDialogAction(ConfiguredActionDefinition definition, ChooseDialogCallback callback, Item item) {
        super(definition);
        this.item = item;
    }

    @Inject
    public ChooseDialogAction(ConfiguredActionDefinition definition, ChooseDialogCallback callback) {
        this(definition, callback, null);
    }

    @Override
    public void execute() throws ActionExecutionException {
        if (getDefinition().isCallSuccess()) {
            callback.onSuccess(getDefinition().getSuccessActionName());
        } else {
            callback.onCancel();
        }
    }
}
