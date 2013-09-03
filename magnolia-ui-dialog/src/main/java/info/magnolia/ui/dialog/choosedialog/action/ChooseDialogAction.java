package info.magnolia.ui.dialog.choosedialog.action;

import com.vaadin.data.Item;
import info.magnolia.ui.api.action.AbstractAction;
import info.magnolia.ui.api.action.ActionExecutionException;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogPresenter;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 8/29/13
 * Time: 10:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChooseDialogAction extends AbstractAction<ChooseDialogActionDefinition> {

    private ChooseDialogPresenter presenter;

    private ChooseDialogCallback callback;

    private Item item;

    public ChooseDialogAction(ChooseDialogPresenter presenter, ChooseDialogActionDefinition definition, ChooseDialogCallback callback, Item item) {
        super(definition);
        this.presenter = presenter;
        this.callback = callback;
        this.item = item;
    }

    public ChooseDialogAction(ChooseDialogPresenter presenter, ChooseDialogActionDefinition definition, ChooseDialogCallback callback) {
        this(presenter, definition, callback, null);
    }

    @Override
    public void execute() throws ActionExecutionException {
        if (getDefinition().isCallSuccess() && item != null) {
            callback.onItemChosen(getDefinition().getSuccessActionName(), item);
        } else {
            callback.onCancel();
        }
        presenter.closeDialog();
    }
}
