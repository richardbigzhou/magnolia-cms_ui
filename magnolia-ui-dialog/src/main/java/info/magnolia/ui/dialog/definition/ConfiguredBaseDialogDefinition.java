package info.magnolia.ui.dialog.definition;

import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.dialog.DialogPresenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 8/29/13
 * Time: 9:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfiguredBaseDialogDefinition implements BaseDialogDefinition {

    public static final String ACTIONS_NODE_NAME = "actions";
    public static final String EXTEND_PROPERTY_NAME = "extends";

    private String id;

    private String label;

    private String i18nBasename;

    private List<SecondaryActionDefinition> secondaryActions = new ArrayList<SecondaryActionDefinition>();

    private Map<String, ActionDefinition> actions = new LinkedHashMap<String, ActionDefinition>();

    private Map<String, ActionPresenterDefinition> actionPresenters = new HashMap<String, ActionPresenterDefinition>();

    private Class<? extends DialogPresenter> presenterClass;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getI18nBasename() {
        return i18nBasename;
    }

    public void setI18nBasename(String i18nBasename) {
        this.i18nBasename = i18nBasename;
    }


    public Map<String, ActionDefinition> getActions() {
        return actions;
    }

    @Override
    public Class<? extends DialogPresenter> getPresenterClass() {
        return presenterClass;
    }

    public void setActions(Map<String, ActionDefinition> actions) {
        this.actions = actions;
    }

    public void addAction(ActionDefinition actionDefinition) {
        actions.put(actionDefinition.getName(), actionDefinition);
    }

    public void setPresenterClass(Class<? extends DialogPresenter> presenterClass) {
        this.presenterClass = presenterClass;
    }

    public List<SecondaryActionDefinition> getSecondaryActions() {
        return secondaryActions;
    }

    @Override
    public Map<String, ActionPresenterDefinition> getActionPresenters() {
        return actionPresenters;
    }

    public void setActionPresenters(Map<String, ActionPresenterDefinition> actionPresenters) {
        this.actionPresenters = actionPresenters;
    }

    public void setSecondaryActions(List<SecondaryActionDefinition> secondaryActions) {
        this.secondaryActions = secondaryActions;
    }
}
