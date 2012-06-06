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

import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.admincentral.jcr.view.JcrView;
import info.magnolia.ui.admincentral.jcr.view.JcrView.ViewType;
import info.magnolia.ui.admincentral.jcr.view.builder.JcrViewBuilderProvider;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.model.workbench.registry.WorkbenchDefinitionRegistry;
import info.magnolia.ui.widget.actionbar.Actionbar;

import javax.inject.Inject;

import com.vaadin.ui.Button;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * TODO write javadoc.
 * @version $Id$
 */
@SuppressWarnings("serial")
public class WorkbenchViewImpl extends CustomComponent implements WorkbenchView {

    private VerticalLayout root = new VerticalLayout();

    private Presenter presenter;

    private HorizontalLayout split = new HorizontalLayout();

    private HorizontalLayout toolbar = new HorizontalLayout();

    private JcrView jcrView;

    private JcrViewBuilderProvider jcrViewBuilderProvider;

    private WorkbenchDefinitionRegistry workbenchRegistry;

    protected String path = "/";

    private JcrView.Presenter jcrPresenter = new JcrView.Presenter() {
        public void onItemSelection(javax.jcr.Item item) {

        };
    };

    @Inject
    public WorkbenchViewImpl(WorkbenchDefinitionRegistry workbenchRegistry, Shell shell, JcrViewBuilderProvider jcrViewBuilderProvider) {
        super();
        setSizeFull();
        root.setSizeFull();
        construct();
        setCompositionRoot(root);

        this.jcrViewBuilderProvider = jcrViewBuilderProvider;
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
        jcrView = jcrViewBuilderProvider.getBuilder().build(workbenchDefinition, ViewType.TREE);


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
