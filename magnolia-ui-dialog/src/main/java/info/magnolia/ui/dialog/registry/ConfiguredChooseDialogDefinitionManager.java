package info.magnolia.ui.dialog.registry;

import info.magnolia.module.ModuleRegistry;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredChooseDialogDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 8/30/13
 * Time: 12:54 AM
 * To change this template use File | Settings | File Templates.
 */
@Singleton
public class ConfiguredChooseDialogDefinitionManager extends ConfiguredBaseDialogDefinitionManager<ChooseDialogDefinition> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String DIALOG_CONFIG_NODE_NAME = "chooseDialogs";

    @Inject
    public ConfiguredChooseDialogDefinitionManager(ModuleRegistry moduleRegistry, ChooseDialogDefinitionRegistry dialogDefinitionRegistry) {
        super(DIALOG_CONFIG_NODE_NAME, moduleRegistry, dialogDefinitionRegistry);
    }

    @Override
    protected boolean isDialog(Node dialogNode) throws RepositoryException {
        return dialogNode.hasNode(ConfiguredChooseDialogDefinition.FIELD_NODE_NAME);
    }

    @Override
    protected ChooseDialogDefinitionProvider createProvider(Node dialogNode) throws RepositoryException {
        final String id = createId(dialogNode);
        try {
            return new ConfiguredChooseDialogDefinitionProvider(id, dialogNode);
        } catch (IllegalArgumentException e) {
            // TODO dlipp - suppress stacktrace as long as SCRUM-1749 is not fixed
            log.error("Unable to create provider for dialog [" + id + "]: " + e);
        } catch (Exception e) {
            log.error("Unable to create provider for dialog [" + id + "]", e);
        }
        return null;
    }
}
