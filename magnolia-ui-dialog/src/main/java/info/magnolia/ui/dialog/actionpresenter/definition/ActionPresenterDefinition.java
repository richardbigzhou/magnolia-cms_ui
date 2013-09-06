package info.magnolia.ui.dialog.actionpresenter.definition;

import info.magnolia.ui.dialog.actionpresenter.ActionPresenter;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/5/13
 * Time: 11:18 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ActionPresenterDefinition {

    Class<? extends ActionPresenter> getPresenterClass();

    Map<String, ActionRendererDefinition> getActionRenderers();
}
