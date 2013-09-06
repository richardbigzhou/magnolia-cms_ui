package info.magnolia.ui.dialog.actionpresenter;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.action.AbstractActionExecutor;
import info.magnolia.ui.api.action.ActionDefinition;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/6/13
 * Time: 11:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class EditorActionExecutor extends AbstractActionExecutor {

    private Map<String, ActionDefinition> actionMap = new HashMap<String, ActionDefinition>();

    @Inject
    public EditorActionExecutor(ComponentProvider componentProvider) {
        super(componentProvider);
    }

    public void setActions(Iterable<ActionDefinition> actions) {
        for (ActionDefinition action : actions) {
            actionMap.put(action.getName(), action);
        }
    }

    @Override
    public ActionDefinition getActionDefinition(String actionName) {
        return actionMap.get(actionName);
    }
}
