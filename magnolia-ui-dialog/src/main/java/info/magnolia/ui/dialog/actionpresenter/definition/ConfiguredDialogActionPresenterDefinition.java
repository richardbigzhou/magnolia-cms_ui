package info.magnolia.ui.dialog.actionpresenter.definition;

import info.magnolia.ui.dialog.actionpresenter.DialogActionPresenterImpl;
import info.magnolia.ui.dialog.definition.SecondaryActionDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/5/13
 * Time: 11:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfiguredDialogActionPresenterDefinition extends ConfiguredActionPresenterDefinition implements DialogActionPresenterDefinition {

    private List<SecondaryActionDefinition> secondaryActions = new ArrayList<SecondaryActionDefinition>();

    public ConfiguredDialogActionPresenterDefinition() {
        setPresenterClass(DialogActionPresenterImpl.class);
    }

    @Override
    public List<SecondaryActionDefinition> getSecondaryActions() {
        return secondaryActions;
    }

    public void setSecondaryActions(List<SecondaryActionDefinition> secondaryActions) {
        this.secondaryActions = secondaryActions;
    }

    @Override
    public Class<? extends DialogActionPresenterImpl> getPresenterClass() {
        return (Class<? extends DialogActionPresenterImpl>) super.getPresenterClass();
    }
}
