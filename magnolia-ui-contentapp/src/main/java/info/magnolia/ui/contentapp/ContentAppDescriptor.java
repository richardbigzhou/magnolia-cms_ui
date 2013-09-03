package info.magnolia.ui.contentapp;

import info.magnolia.ui.api.app.AppDescriptor;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;

/**
 * Extends {@link AppDescriptor} by providing definition of choose dialog.
 */
public interface ContentAppDescriptor extends AppDescriptor {

    ChooseDialogDefinition getChooseDialog();
}
