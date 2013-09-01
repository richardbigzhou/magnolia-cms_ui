package info.magnolia.ui.dialog.registry;

import info.magnolia.jcr.node2bean.Node2BeanException;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.objectfactory.Components;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredChooseDialogDefinition;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 8/30/13
 * Time: 2:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfiguredChooseDialogDefinitionProvider implements ChooseDialogDefinitionProvider {

    private final String id;

    private final ConfiguredChooseDialogDefinition dialogDefinition;

    public ConfiguredChooseDialogDefinitionProvider(String id, Node configNode) throws RepositoryException, Node2BeanException {
        this.id = id;
        this.dialogDefinition = (ConfiguredChooseDialogDefinition) Components.getComponent(Node2BeanProcessor.class).toBean(configNode, ChooseDialogDefinition.class);
        if (this.dialogDefinition != null) {
            this.dialogDefinition.setId(id);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ChooseDialogDefinition getDialogDefinition() throws RegistrationException {
        return dialogDefinition;
    }
}
