package info.magnolia.ui.dialog.definition;

import info.magnolia.ui.api.action.ActionPresenter;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/5/13
 * Time: 12:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfiguredActionPresenterDefinition implements ActionPresenterDefinition {

    private Class<? extends ActionPresenter> presenterClass;

    @Override
    public Class<? extends ActionPresenter> getPresenterClass() {
        return presenterClass;
    }

    public void setPresenterClass(Class<? extends ActionPresenter> presenterClass) {
        this.presenterClass = presenterClass;
    }
}
