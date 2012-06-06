package info.magnolia.ui.admincentral.workbench;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.objectfactory.configuration.InstanceConfiguration;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.admincentral.column.Column;
import info.magnolia.ui.admincentral.jcr.view.JcrView;
import info.magnolia.ui.admincentral.jcr.view.JcrView.ViewType;
import info.magnolia.ui.admincentral.jcr.view.builder.ConfiguredJcrViewBuilder;
import info.magnolia.ui.admincentral.jcr.view.builder.JcrViewBuilderProvider;
import info.magnolia.ui.admincentral.tree.model.TreeModel;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.column.definition.AbstractColumnDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.model.workbench.registry.WorkbenchDefinitionRegistry;
import info.magnolia.ui.widget.actionbar.Actionbar;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class WorkbenchViewImpl extends CustomComponent implements WorkbenchView {

    private VerticalLayout root = new VerticalLayout();
    
    private Presenter presenter;
    
    private HorizontalLayout split = new HorizontalLayout();
    
    private HorizontalLayout toolbar = new HorizontalLayout();
    
    private JcrView jcrView;
    
    private JcrViewBuilderProvider jcrViewBuilderProvider;

    private ConfiguredJcrViewBuilder configuredJcrViewBuilder;

    private WorkbenchDefinitionRegistry workbenchRegistry;
    
    protected String path = "/";
    
    private JcrView.Presenter jcrPresenter = new JcrView.Presenter() {
        public void onItemSelection(javax.jcr.Item item) {
            
        };
    };

    @Inject
    public WorkbenchViewImpl(WorkbenchDefinitionRegistry workbenchRegistry, Shell shell, ComponentProvider componentProvider) {
        super();
        setSizeFull();
        root.setSizeFull();
        construct();
        setCompositionRoot(root);
        
        this.jcrViewBuilderProvider = componentProvider.getComponent(JcrViewBuilderProvider.class);
        this.configuredJcrViewBuilder = (ConfiguredJcrViewBuilder) jcrViewBuilderProvider.getBuilder();
        this.workbenchRegistry = workbenchRegistry;
    }
    
    @Override
    public void initWorkbench(final String id) {
        // load the workbench specific configuration if existing
        final WorkbenchDefinition workbenchDefinition;
        try {
            // FIXME: stub workbench name!
            workbenchDefinition = workbenchRegistry.get(id);
        } catch (RegistrationException e) {
            throw new RuntimeException(e);
        }
        
        ComponentProviderConfiguration componentProviderConfiguration = new ComponentProviderConfiguration();

        if (workbenchDefinition.getComponents() != null) {
            componentProviderConfiguration.combine(workbenchDefinition.getComponents());
        }

        Map<String, Column<?>> columns = new LinkedHashMap<String, Column<?>>();
        for (AbstractColumnDefinition columnDefinition : workbenchDefinition.getColumns()) {
            Column<?> column = configuredJcrViewBuilder.createTreeColumn(columnDefinition);
            if (column != null) {
                columns.put(columnDefinition.getName(), column);
            }
        }
        
        final TreeModel treeModel = new TreeModel(workbenchDefinition, columns);
        jcrView = jcrViewBuilderProvider.getBuilder().build(workbenchDefinition, ViewType.TREE);
        
        componentProviderConfiguration.addComponent(InstanceConfiguration.valueOf(TreeModel.class, treeModel));
        componentProviderConfiguration.addComponent(InstanceConfiguration.valueOf(WorkbenchDefinition.class, workbenchDefinition));
        
        jcrView.setPresenter(jcrPresenter);
        jcrView.select(path);
        jcrView.asVaadinComponent();
        split.addComponent(jcrView.asVaadinComponent());
        Actionbar bar = new Actionbar();
        split.addComponent(bar);
        split.setExpandRatio(jcrView.asVaadinComponent(), 1f);
    }

    private void construct() {
        split.setSizeFull();
        toolbar.setSizeUndefined();
        toolbar.addComponent(new Button("Tree"));
        toolbar.addComponent(new Button("List"));
        root.addComponent(toolbar);
        root.addComponent(split);
        root.setExpandRatio(split, 1f);
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

}
