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

import java.util.EnumMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
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

    private final HorizontalLayout root = new HorizontalLayout();

    private final VerticalLayout workbenchContainer = new VerticalLayout();

    private final Map<ViewType, JcrView> jcrViews = new EnumMap<ViewType, JcrView>(ViewType.class);

    private ViewType currentViewType = ViewType.TREE;

    private Presenter presenter;

    private final JcrView.Presenter jcrPresenter = new JcrView.Presenter() {

        @Override
        public void onItemSelection(Item item) {
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
        setCompositionRoot(root);
        root.setStyleName("mgnl-app-root");
        root.addComponent(workbenchContainer);
        root.setExpandRatio(workbenchContainer, 1f);
        root.setMargin(false);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setSizeUndefined();
        toolbar.setStyleName("mgnl-workbench-toolbar");
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

        workbenchContainer.setStyleName("mgnl-workbench");
        workbenchContainer.addComponent(toolbar);
    }

    @Override
    public void initWorkbench(final WorkbenchDefinition workbenchDefinition) {
        if (workbenchDefinition == null) {
            throw new IllegalArgumentException("Trying to init a workbench but got null definition.");
        }
        log.debug("Initializing workbench {}...", workbenchDefinition.getName());

        for (final ViewType type : ViewType.values()) {
            final JcrView jcrView = jcrViewBuilderProvider.getBuilder().build(workbenchDefinition, type);
            jcrView.setPresenter(jcrPresenter);
            jcrView.select(StringUtils.defaultIfEmpty(workbenchDefinition.getPath(), "/"));
            jcrViews.put(type, jcrView);
        }

        if (StringUtils.isBlank(workbenchDefinition.getWorkspace())) {
            throw new IllegalStateException(workbenchDefinition.getName()
                + " workbench definition must specify a workspace to connect to. Please, check your configuration.");
        }

        setGridType(ViewType.TREE);
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
    public void setGridType(ViewType type) {
        workbenchContainer.removeComponent(jcrViews.get(currentViewType).asVaadinComponent());
        final Component c = jcrViews.get(type).asVaadinComponent();

        workbenchContainer.addComponent(c);
        workbenchContainer.setExpandRatio(c, 1f);

        // split.addComponentAsFirst(c);
        this.currentViewType = type;
    }

    @Override
    public void refreshItem(Item item) {
        jcrViews.get(currentViewType).refreshItem(item);
    }

    @Override
    public void refresh() {
        jcrViews.get(currentViewType).refresh();
    }

}
