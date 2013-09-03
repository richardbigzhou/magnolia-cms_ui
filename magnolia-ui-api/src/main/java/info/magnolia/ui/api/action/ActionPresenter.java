package info.magnolia.ui.api.action;

import info.magnolia.ui.api.view.View;

/**
 * ActionPresenter is capable of providing UI controls for actions other than a mere button.
 */
public interface ActionPresenter {

    View start(ActionDefinition definition, ActionListener listener);

}
