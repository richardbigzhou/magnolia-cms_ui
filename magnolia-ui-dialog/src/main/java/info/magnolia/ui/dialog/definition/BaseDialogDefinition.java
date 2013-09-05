package info.magnolia.ui.dialog.definition;

import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.dialog.DialogPresenter;

import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 8/29/13
 * Time: 9:33 PM
 * To change this template use File | Settings | File Templates.
 */
public interface BaseDialogDefinition {
    /**
     * Unique identifier for this dialog.
     */
    String getId();

    String getLabel();

    String getI18nBasename();

    List<SecondaryActionDefinition> getSecondaryActions();

    Map<String, ActionPresenterDefinition> getActionPresenters();

    Map<String, ActionDefinition> getActions();

    Class<? extends DialogPresenter> getPresenterClass();
}
