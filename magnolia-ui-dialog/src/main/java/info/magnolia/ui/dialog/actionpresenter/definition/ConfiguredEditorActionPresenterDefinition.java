package info.magnolia.ui.dialog.actionpresenter.definition;

import info.magnolia.ui.dialog.actionpresenter.DialogActionPresenter;
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
public class ConfiguredEditorActionPresenterDefinition extends ConfiguredActionPresenterDefinition implements EditorActionPresenterDefinition {

    private List<SecondaryActionDefinition> secondaryActions = new ArrayList<SecondaryActionDefinition>();

    public ConfiguredEditorActionPresenterDefinition() {
        setPresenterClass(DialogActionPresenter.class);
    }

    @Override
    public List<SecondaryActionDefinition> getSecondaryActions() {
        return secondaryActions;
    }

    public void setSecondaryActions(List<SecondaryActionDefinition> secondaryActions) {
        this.secondaryActions = secondaryActions;
    }

    @Override
    public Class<? extends DialogActionPresenter> getPresenterClass() {
        return (Class<? extends DialogActionPresenter>) super.getPresenterClass();
    }
}
