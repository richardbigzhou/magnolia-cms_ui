package info.magnolia.ui.dialog.registry;

import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.dialog.definition.BaseDialogDefinition;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 8/30/13
 * Time: 12:38 AM
 * To change this template use File | Settings | File Templates.
 */
public interface BaseDialogDefinitionProvider<T extends BaseDialogDefinition> {

    String getId();

    T getDialogDefinition() throws RegistrationException;
}
