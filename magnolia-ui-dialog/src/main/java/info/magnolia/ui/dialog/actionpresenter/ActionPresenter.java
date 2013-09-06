package info.magnolia.ui.dialog.actionpresenter;

import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.actionpresenter.definition.ActionPresenterDefinition;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/5/13
 * Time: 9:07 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ActionPresenter<T extends ActionPresenterDefinition> {

     View start(Iterable<ActionDefinition> actions, T definition, ActionListener listener);
}
