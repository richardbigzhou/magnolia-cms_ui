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
package info.magnolia.ui.app.pages;

import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.framework.app.AppView;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;
import info.magnolia.ui.widget.actionbar.Actionbar;
import info.magnolia.ui.widget.editor.PageEditorView;
import org.apache.commons.lang.StringUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * PageEditorTabView.
 * TODO: make this a component with a split layout to accomodate the page editor on the left and its related actions on the right.
*/
@SuppressWarnings("serial")
public class PageEditorTabView implements AppView, IsVaadinComponent {

    private Actionbar actionbar;

    private final HorizontalLayout root = new HorizontalLayout();

    private final VerticalLayout container = new VerticalLayout();
    
    private final String caption;

    public PageEditorTabView(ComponentProvider componentProvider, Node pageNode, final Actionbar actionbar) throws RepositoryException {

        this.actionbar = actionbar;
        // same root as ContentWorkbenchView
        root.setSizeFull();
        root.setStyleName("mgnl-app-root");
        root.addComponent(container);
        root.setExpandRatio(container, 1f);
        root.setMargin(false);
        root.setSpacing(true);

        Object[] combinedParameters = new Object[1];

        combinedParameters[0] = pageNode;
        PageEditorView.Presenter pageEditorPresenter = componentProvider.newInstance(PageEditorPresenter.class, combinedParameters);

       

        container.setSizeFull();
        container.setStyleName("mgnl-app-view");
        container.addComponent(pageEditorPresenter.getView().asVaadinComponent());
        caption = StringUtils.defaultIfEmpty(PropertyUtil.getString(pageNode, "title"), pageNode.getName());
        root.addComponent(actionbar);
    }

    public Actionbar getActionbar() {
        return actionbar;
    }

    public void setActionbar(Actionbar actionbar) {
        root.replaceComponent(this.actionbar, actionbar);
        this.actionbar = actionbar;
    }

    @Override
    public String getCaption() {
        return caption;
    }

    @Override
    public Component asVaadinComponent() {
        return root;
    }

    /**
     * Presenter.
     */
    public interface Presenter {

    }
}
