package info.magnolia.ui.dialog.actionpresenter.definition;

import info.magnolia.ui.dialog.actionpresenter.renderer.ActionRenderer;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/5/13
 * Time: 12:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfiguredActionRendererDefinition implements ActionRendererDefinition {

    private Class<? extends ActionRenderer> presenterClass;

    @Override
    public Class<? extends ActionRenderer> getPresenterClass() {
        return presenterClass;
    }

    public void setPresenterClass(Class<? extends ActionRenderer> presenterClass) {
        this.presenterClass = presenterClass;
    }
}
