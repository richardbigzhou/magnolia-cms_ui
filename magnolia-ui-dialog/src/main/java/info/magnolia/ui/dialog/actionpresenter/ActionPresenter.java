package info.magnolia.ui.dialog.actionpresenter;

import com.vaadin.event.ShortcutListener;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.actionpresenter.definition.EditorActionPresenterDefinition;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/5/13
 * Time: 9:07 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ActionPresenter {

    View start(Iterable<ActionDefinition> actions, EditorActionPresenterDefinition definition, ActionParameterProvider parameterProvider, UiContext uiContext);

    ShortcutListener bindShortcut(String actionName, int keyCode, int... modifiers);
}
