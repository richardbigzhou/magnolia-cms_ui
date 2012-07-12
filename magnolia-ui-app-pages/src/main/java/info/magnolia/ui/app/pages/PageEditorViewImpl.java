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
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.vaadin.integration.view.IsVaadinComponent;
import info.magnolia.ui.widget.actionbar.Actionbar;
import info.magnolia.ui.widget.editor.PageEditor;

/**
 * PageEditorViewImpl.
 * TODO: make this a component with a split layout to accomodate the page editor on the left and its related actions on the right.
*/
@SuppressWarnings("serial")
public class PageEditorViewImpl implements PageEditorView, IsVaadinComponent {

    private Actionbar actionbar;

    private final HorizontalLayout root = new HorizontalLayout();

    private final VerticalLayout container = new VerticalLayout();

    private Listener listener;
    private PageEditor pageEditor;
    private String caption;

    public PageEditorViewImpl() {

        //this.actionbar = actionbar;
        // same root as ContentWorkbenchView
        root.setSizeFull();
        root.setStyleName("mgnl-app-root");
        root.addComponent(container);
        root.setExpandRatio(container, 1f);
        root.setMargin(false);
        root.setSpacing(true);

        container.setSizeFull();
        container.setStyleName("mgnl-app-view");
        container.setImmediate(true);
        caption = "meeeh"; //StringUtils.defaultIfEmpty(PropertyUtil.getString(pageNode, "title"), pageNode.getName());
        //root.addCompon;ent(actionbar);
    }

    public void setActionbar(Actionbar actionbar) {
        root.replaceComponent(this.actionbar, actionbar);
        this.actionbar = actionbar;
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public void initPageEditor(String nodePath) {
        String contextPath = MgnlContext.getContextPath();
        pageEditor = new PageEditor(contextPath, nodePath);
        container.addComponent(pageEditor);
    }


    @Override
    public Component asVaadinComponent() {
        return root;
    }
}
