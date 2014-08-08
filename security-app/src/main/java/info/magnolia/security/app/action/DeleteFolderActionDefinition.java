package info.magnolia.security.app.action;

import info.magnolia.ui.framework.action.DeleteActionDefinition;

/**
 * Used to configure a {@link DeleteFolderAction}.
 *
 * @see DeleteFolderAction
 */
public class DeleteFolderActionDefinition extends DeleteActionDefinition {

    public DeleteFolderActionDefinition() {
        setImplementationClass(DeleteFolderAction.class);
    }
}