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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.jcr.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;


/**
 * TODO write javadoc.
 * @version $Id$
 */
@SuppressWarnings("serial")
public class WorkbenchViewImpl extends CustomComponent implements WorkbenchView {

    private static final Logger log = LoggerFactory.getLogger(WorkbenchViewImpl.class);

    private final VerticalLayout root = new VerticalLayout();

    private final HorizontalLayout split = new HorizontalLayout();

    private final HorizontalLayout toolbar = new HorizontalLayout();

    private Presenter presenter;

    private JcrView jcrView;

    private final JcrViewBuilderProvider jcrViewBuilderProvider;

    protected String path = "/";

    private final Map<String, ActionbarItemDefinition> actions = new LinkedHashMap<String, ActionbarItemDefinition>();

    private final JcrView.Presenter jcrPresenter = new JcrView.Presenter() {

        @Override
        public void onItemSelection(javax.jcr.Item item) {
            presenter.onItemSelected(item);
        };
    };

    @Inject
    public WorkbenchViewImpl(JcrViewBuilderProvider jcrViewBuilderProvider) {
        super();
        this.jcrViewBuilderProvider = jcrViewBuilderProvider;

        setSizeFull();
        root.setSizeFull();
        construct();
        setCompositionRoot(root);
    }

    @Override
    public void initWorkbench(final WorkbenchDefinition workbenchDefinition) {
        jcrView = jcrViewBuilderProvider.getBuilder().build(workbenchDefinition, ViewType.TREE);
        jcrView.setPresenter(jcrPresenter);
        jcrView.select(path);
        jcrView.asVaadinComponent();
        split.addComponent(jcrView.asVaadinComponent());

        Actionbar bar = buildActionbar(workbenchDefinition.getActionbar());

        split.addComponent(bar);
        split.setExpandRatio(jcrView.asVaadinComponent(), 1f);
    }

    private Actionbar buildActionbar(ActionbarDefinition actionbarDefinition) {
        Actionbar actionbar = new Actionbar();

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
                            System.out.println("actionbar item clicked");
                            ActionDefinition actionDefinition = getActionDefinition(actionName);
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
        toolbar.addComponent(new Button("Tree"));
        toolbar.addComponent(new Button("List"));
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
        jcrView.refreshNode(node);
    }

}
