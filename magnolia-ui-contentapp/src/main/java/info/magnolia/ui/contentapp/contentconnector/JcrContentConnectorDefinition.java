package info.magnolia.ui.contentapp.contentconnector;

import java.io.Serializable;

/**
 * Defines a JcrContentConnector. Contains all elements which defines a JcrContentConnector configuration.
 */
public interface JcrContentConnectorDefinition extends Serializable {

    /**
     *
     * @return the name of the workspace the JcrContentConnector is bound to
     */
    String getWorkspace();

    /**
     * @return the path configured as root for this workspace. If not specified, defaults to root ("/").
     */
    String getPath();


}
