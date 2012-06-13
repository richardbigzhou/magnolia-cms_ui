/**
 * This file Copyright (c) 2012 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.admincentral.workbench;

import info.magnolia.ui.admincentral.jcr.view.JcrView;
import info.magnolia.ui.admincentral.jcr.view.JcrView.ViewType;
import info.magnolia.ui.admincentral.jcr.view.builder.JcrViewBuilderProvider;
import info.magnolia.ui.model.action.ActionDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarGroupDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarItemDefinition;
import info.magnolia.ui.model.actionbar.definition.ActionbarSectionDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.widget.actionbar.ActionButton;
import info.magnolia.ui.widget.actionbar.Actionbar;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;


/**
 * Implementation of {@link WorkbenchView}.
 * @version $Id$
 */
@SuppressWarnings("serial")
public class WorkbenchViewImpl extends CustomComponent implements WorkbenchView {

    private static final Logger log = LoggerFactory.getLogger(WorkbenchViewImpl.class);

    private final JcrViewBuilderProvider jcrViewBuilderProvider;
    
    private final VerticalLayout root = new VerticalLayout();

    private final HorizontalLayout split = new HorizontalLayout();

    private final HorizontalLayout toolbar = new HorizontalLayout();

    private final Map<ViewType, JcrView> jcrViews = new EnumMap<ViewType, JcrView>(ViewType.class);
    
    private ViewType currentViewType = ViewType.TREE; 
            
    private Presenter presenter;
    
    protected String path = "/";

    private JcrView jcrView;

    private final Map<String, ActionbarItemDefinition> actions = new LinkedHashMap<String, ActionbarItemDefinition>();

    private final JcrView.Presenter jcrPresenter = new JcrView.Presenter() {

        @Override
        public void onItemSelection(javax.jcr.Item item) {
            presenter.onItemSelected(item);
        };
    };

    @Inject
    public WorkbenchViewImpl(final JcrViewBuilderProvider jcrViewBuilderProvider) {
        super();
        this.jcrViewBuilderProvider = jcrViewBuilderProvider;
        setSizeFull();
        root.setSizeFull();
        setCompositionRoot(root);
        construct();
    }

    @Override
    public void initWorkbench(final WorkbenchDefinition workbenchDefinition) {
        
        for (final ViewType type : ViewType.values() ) {
            final JcrView jcrView = jcrViewBuilderProvider.getBuilder().build(workbenchDefinition, type);
            jcrView.setPresenter(jcrPresenter);
            jcrView.select(path);
            jcrViews.put(type, jcrView);
        }
        
        setGridType(ViewType.TREE);
        if(workbenchDefinition == null) {
            throw new IllegalArgumentException("Trying to init a workbench but got null definition.");
        }

        log.debug("Initializing workbench {}...", workbenchDefinition.getName());

        if(StringUtils.isBlank(workbenchDefinition.getWorkspace())) {
            throw new IllegalStateException(workbenchDefinition.getName() + " workbench definition must specify a workspace to connect to. Please, check your configuration.");
        }

        jcrView = jcrViewBuilderProvider.getBuilder().build(workbenchDefinition, ViewType.TREE);
        jcrView.setPresenter(jcrPresenter);
        jcrView.select(StringUtils.defaultIfEmpty(workbenchDefinition.getPath(), "/"));
        jcrView.asVaadinComponent();
        split.addComponent(jcrView.asVaadinComponent());

        final Actionbar actionBar = buildActionbar(workbenchDefinition.getActionbar());

        split.addComponent(actionBar);
        split.setExpandRatio(jcrView.asVaadinComponent(), 1f);
    }

    private Actionbar buildActionbar(final ActionbarDefinition actionbarDefinition) {
        Actionbar actionbar = new Actionbar();

        if(actionbarDefinition == null) {
            log.warn("No actionbar definition found. This will result in an empty action bar. Is that intended?");
            return actionbar;
        }

        for (ActionbarSectionDefinition section : actionbarDefinition.getSections()) {
            for (ActionbarGroupDefinition group : section.getGroups()) {
                for (ActionbarItemDefinition item : group.getItems()) {

                    ActionButton button = new ActionButton(item.getLabel());
                    button.setIcon(new ThemeResource(item.getIcon()));

                    final String actionName = item.getName();
                    button.setActionName(actionName);
                    button.setGroupName(group.getName());
                    button.setSectionTitle(section.getTitle());

                    button.addListener(new ClickListener() {

                        @Override
                        public void buttonClick(ClickEvent event) {
                            ActionDefinition actionDefinition = getActionDefinition(actionName);
                            if(actionDefinition == null) {
                                log.warn("No action definition found for {}", actionName);
                                return;
                            }
                            getPresenter().onActionbarItemClicked(actionDefinition);
                        }
                    });
                    actionbar.addComponent(button);
                    actions.put(actionName, item);
                }
            }
        }

        // actionbar.setDefinition(actionbarDefinition);
        return actionbar;
    }

    private ActionDefinition getActionDefinition(final String actionName) {
        ActionbarItemDefinition actionbarItemDefinition = actions.get(actionName);
        if (actionbarItemDefinition != null) {
            ActionDefinition actionDefinition = actionbarItemDefinition.getActionDefinition();
            return actionDefinition;
        }
        return null;
    }

    private void construct() {
        split.setSizeFull();
        toolbar.setSizeUndefined();
        toolbar.addComponent(new Button("Tree", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                setGridType(ViewType.TREE);
            }
        }));
        toolbar.addComponent(new Button("List", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                setGridType(ViewType.LIST);
            }
        }));
        root.addComponent(toolbar);
        root.addComponent(split);
        root.setExpandRatio(split, 1f);
    }

    public Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void refreshNode(Node node) {
        jcrViews.get(ViewType.TREE).refreshNode(node);
    }

    @Override
    public void setGridType(ViewType type) {
        split.removeComponent(jcrViews.get(currentViewType).asVaadinComponent());
        final Component c = jcrViews.get(type).asVaadinComponent();
        split.addComponent(c);
        split.addComponentAsFirst(c);
        split.setExpandRatio(c, 1f);
        this.currentViewType = type;
    }
    
    @Override
    public void refresh() {
        jcrView.refresh();
    }

}
