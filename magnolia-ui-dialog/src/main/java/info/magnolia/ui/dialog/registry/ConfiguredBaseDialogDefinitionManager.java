package info.magnolia.ui.dialog.registry;

import info.magnolia.cms.util.ModuleConfigurationObservingManager;
import info.magnolia.jcr.predicate.NodeTypePredicate;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.ui.dialog.definition.BaseDialogDefinition;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 8/30/13
 * Time: 12:45 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ConfiguredBaseDialogDefinitionManager<T extends BaseDialogDefinition> extends ModuleConfigurationObservingManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Set<String> registeredIds = new HashSet<String>();

    private String configNodeName;

    private final BaseDialogDefinitionRegistry<T> dialogDefinitionRegistry;

    public ConfiguredBaseDialogDefinitionManager(String configNodeName, ModuleRegistry moduleRegistry, BaseDialogDefinitionRegistry<T> dialogDefinitionRegistry) {
        super(configNodeName, moduleRegistry);
        this.configNodeName = configNodeName;
        this.dialogDefinitionRegistry = dialogDefinitionRegistry;
    }

    @Override
    protected void reload(List<Node> nodes) throws RepositoryException {

        final List<BaseDialogDefinitionProvider<T>> providers = new ArrayList<BaseDialogDefinitionProvider<T>>();

        for (Node node : nodes) {

            NodeUtil.visit(node, new NodeVisitor() {

                @Override
                public void visit(Node current) throws RepositoryException {
                    for (Node dialogNode : NodeUtil.getNodes(current, NodeTypes.ContentNode.NAME)) {
                        if (isDialog(dialogNode)) {
                            // Handle as dialog only if it has sub nodes indicating that it is actually representing a dialog.
                            // This will filter the fields in dialogs used by the extends mechanism.
                            BaseDialogDefinitionProvider<T> provider = createProvider(dialogNode);
                            if (provider != null) {
                                providers.add(provider);
                            }
                        } else {
                            log.warn("node " + dialogNode.getName() + " will not be handled as Dialog.");
                        }
                    }
                }
            }, new NodeTypePredicate(NodeTypes.Content.NAME));
        }

        this.registeredIds = dialogDefinitionRegistry.unregisterAndRegister(registeredIds, providers);
    }

    /**
     * Check if this node can be handle as a ConfiguredDialogDefinition.
     */
    protected abstract boolean isDialog(Node dialogNode) throws RepositoryException;

    protected abstract BaseDialogDefinitionProvider<T> createProvider(Node dialogNode) throws RepositoryException;

    protected String createId(Node configNode) throws RepositoryException {
        final String path = configNode.getPath();
        final String[] pathElements = path.split("/");
        final String moduleName = pathElements[2];
        return moduleName + ":" + StringUtils.removeStart(path, "/modules/" + moduleName + "/" + configNodeName + "/");
    }
}
