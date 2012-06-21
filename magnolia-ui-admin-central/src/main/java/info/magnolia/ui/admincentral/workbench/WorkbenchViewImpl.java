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

import info.magnolia.ui.admincentral.actionbar.builder.ActionbarBuilder;
import info.magnolia.ui.admincentral.jcr.view.JcrView;
import info.magnolia.ui.admincentral.jcr.view.JcrView.ViewType;
import info.magnolia.ui.admincentral.jcr.view.builder.JcrViewBuilderProvider;
import info.magnolia.ui.model.actionbar.definition.ActionbarDefinition;
import info.magnolia.ui.model.workbench.definition.WorkbenchDefinition;
import info.magnolia.ui.widget.actionbar.Actionbar;

import javax.inject.Inject;
import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Button;
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

    private Presenter presenter;

    private final HorizontalLayout root = new HorizontalLayout();

    private final VerticalLayout workbenchContainer = new VerticalLayout();

    private JcrView jcrView;

    private final JcrViewBuilderProvider jcrViewBuilderProvider;

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

        setCompositionRoot(root);
        setSizeFull();

        root.setSizeFull();
        root.setStyleName("mgnl-app-root");
        root.addComponent(workbenchContainer);
        root.setExpandRatio(workbenchContainer, 1f);
        root.setMargin(false);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setSizeUndefined();
        toolbar.setStyleName("mgnl-workbench-toolbar");
        toolbar.addComponent(new Button("Tree"));
        toolbar.addComponent(new Button("List"));

        workbenchContainer.setStyleName("mgnl-workbench");
        workbenchContainer.addComponent(toolbar);
    }

    @Override
    public void initWorkbench(final WorkbenchDefinition workbenchDefinition) {
        if (workbenchDefinition == null) {
            throw new IllegalArgumentException("Trying to init a workbench but got null definition.");
        }

        log.debug("Initializing workbench {}...", workbenchDefinition.getName());

        if (StringUtils.isBlank(workbenchDefinition.getWorkspace())) {
            throw new IllegalStateException(workbenchDefinition.getName()
                + " workbench definition must specify a workspace to connect to. Please, check your configuration.");
        }

        jcrView = jcrViewBuilderProvider.getBuilder().build(workbenchDefinition, ViewType.TREE);
        jcrView.setPresenter(jcrPresenter);
        jcrView.select(StringUtils.defaultIfEmpty(workbenchDefinition.getPath(), "/"));
        workbenchContainer.addComponent(jcrView.asVaadinComponent());
    }

    @Override
    public void initActionbar(final ActionbarDefinition definition) {
        if (definition == null) {
            throw new IllegalArgumentException("Trying to init an action bar but got null definition.");
        }
        final Actionbar actionbar = ActionbarBuilder.build(definition, getPresenter());
        root.addComponent(actionbar);
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

    @Override
    public void refresh() {
        jcrView.refresh();
    }

}
