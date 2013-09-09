package info.magnolia.ui.dialog.actionarea.renderer;

import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.actionarea.ActionListener;

/**
 * ActionRenderer is capable of providing UI controls for actions other than a mere button.
 */
public interface ActionRenderer {

    View start(ActionDefinition action, ActionListener listener);

}
