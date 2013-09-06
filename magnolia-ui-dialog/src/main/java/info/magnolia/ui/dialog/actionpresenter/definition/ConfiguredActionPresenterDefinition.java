package info.magnolia.ui.dialog.actionpresenter.definition;

import info.magnolia.ui.dialog.actionpresenter.ActionPresenter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/5/13
 * Time: 11:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConfiguredActionPresenterDefinition implements ActionPresenterDefinition {

    private Map<String, ActionRendererDefinition> actionRenderers = new HashMap<String, ActionRendererDefinition>();

    private Class<? extends ActionPresenter> presenterClass;

    public Map<String, ActionRendererDefinition> getActionRenderers() {
        return actionRenderers;
    }

    public void setActionRenderers(Map<String, ActionRendererDefinition> actionRenderers) {
        this.actionRenderers = actionRenderers;
    }

    public void setPresenterClass(Class<? extends ActionPresenter> presenterClass) {
        this.presenterClass = presenterClass;
    }

    public Class<? extends ActionPresenter> getPresenterClass() {
        return presenterClass;
    }
}
