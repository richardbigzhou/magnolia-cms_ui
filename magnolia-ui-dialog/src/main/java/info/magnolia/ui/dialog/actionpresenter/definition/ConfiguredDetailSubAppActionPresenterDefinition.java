package info.magnolia.ui.dialog.actionpresenter.definition;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/6/13
 * Time: 9:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfiguredDetailSubAppActionPresenterDefinition extends ConfiguredEditorActionPresenterDefinition implements DetailSubAppActionPresenterDefinition {

    private List<FormActionItemDefinition> actions = new LinkedList<FormActionItemDefinition>();

    @Override
    public List<FormActionItemDefinition> getActions() {
        return actions;
    }

    public void setActions(List<FormActionItemDefinition> actions) {
        this.actions = actions;
    }
}
