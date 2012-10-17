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
package info.magnolia.ui.app.showcase.main;

import java.util.Date;

import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Table;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Implementation for unsupported components view.
 */
public class UnsupportedViewImpl implements UnsupportedView {

    private static final long serialVersionUID = 4074959561953183899L;

    VerticalLayout layout = new VerticalLayout();

    public UnsupportedViewImpl() {
        layout.setMargin(true, true, false, true);
        layout.setSpacing(true);
        layout.addComponent(new Label("The UI elements in the Vaadin framework" +
            " that are not recommended for use with Magnolia. These elements" +
            " may not perform well on touch devices, and the look and feel" +
            " may not match the Magnolia interface. Use of these elements" +
            " is not supported by Magnolia."));

        layout.addComponent(getAccordionPreviews());
        layout.addComponent(getMenuBarPreviews());
        layout.addComponent(getWindowPreviews());
        layout.addComponent(getTableAndTreeTable());
    }

    Layout getTableAndTreeTable() {
        Layout grid = getPreviewLayout("Tables and trees");
        Table table = new Table();
        table.addContainerProperty("first", String.class, "first");
        table.addContainerProperty("second", Integer.class, 1);
        table.addContainerProperty("thid", Date.class, new Date());
        for (int loop = 0; loop < 3; loop++) {
            table.addItem();
        }
        table.setHeight("200px");
        grid.addComponent(table);

        TreeTable tree = new TreeTable();
        HierarchicalContainer container = new HierarchicalContainer();

        container.addContainerProperty("first", String.class, "first");
        container.addContainerProperty("second", Integer.class, 1);
        container.addContainerProperty("thid", Date.class, new Date());

        tree.setContainerDataSource(container);
        Object root = container.addItem();
        for (int loop = 0; loop < 3; loop++) {
            Object itemId = container.addItem();
            container.setParent(itemId, root);
        }
        tree.setCollapsed(root, false);
        tree.setHeight("200px");
        grid.addComponent(tree);

        return grid;
    }

    Layout getWindowPreviews() {
        final Layout grid = getPreviewLayout("Windows");

        Button win = new Button("Open normal sub-window",
            new Button.ClickListener() {

                /**
                     *
                     */
                private static final long serialVersionUID = -5643160479156903516L;

                @Override
                public void buttonClick(ClickEvent event) {
                    grid.getApplication().getMainWindow().addWindow(new Window("Normal window"));
                }
            });
        grid.addComponent(win);
        win.setDescription("new Window()");

        return grid;
    }

    private Layout getAccordionPreviews() {
        Layout grid = getPreviewLayout("Accordions");

        Accordion tabs = new DemoAccordion(false);
        grid.addComponent(tabs);

        return grid;
    }

    Layout getMenuBarPreviews() {
        Layout grid = getPreviewLayout("Menu bars");

        MenuBar menubar = new MenuBar();
        final MenuBar.MenuItem file = menubar.addItem("File", null);
        final MenuBar.MenuItem newItem = file.addItem("New", null);
        file.addItem("Open file...", null);
        file.addSeparator();

        newItem.addItem("File", null);
        newItem.addItem("Folder", null);
        newItem.addItem("Project...", null);

        file.addItem("Close", null);
        file.addItem("Close All", null);
        file.addSeparator();

        file.addItem("Save", null);
        file.addItem("Save As...", null);
        file.addItem("Save All", null);

        final MenuBar.MenuItem edit = menubar.addItem("Edit", null);
        edit.addItem("Undo", null);
        edit.addItem("Redo", null).setEnabled(false);
        edit.addSeparator();

        edit.addItem("Cut", null);
        edit.addItem("Copy", null);
        edit.addItem("Paste", null);
        edit.addSeparator();

        final MenuBar.MenuItem find = edit.addItem("Find/Replace", null);

        // Actions can be added inline as well, of course
        find.addItem("Google Search", null);
        find.addSeparator();
        find.addItem("Find/Replace...", null);
        find.addItem("Find Next", null);
        find.addItem("Find Previous", null);

        final MenuBar.MenuItem view = menubar.addItem("View", null);
        view.addItem("Show/Hide Status Bar", null);
        view.addItem("Customize Toolbar...", null);
        view.addSeparator();

        view.addItem("Actual Size", null);
        view.addItem("Zoom In", null);
        view.addItem("Zoom Out", null);

        grid.addComponent(menubar);

        return grid;
    }

    /**
     * Private class to display custom Accordion.
     */
    private class DemoAccordion extends Accordion {

        private static final long serialVersionUID = 4615829029016072624L;

        DemoAccordion(boolean closable) {
            super();
            setWidth("70%");
            setHeight("160px");
            for (int i = 1; i < 5; i++) {
                VerticalLayout l = new VerticalLayout();
                l.setMargin(true);
                Tab t = addTab(l);
                t.setCaption("Sheet " + i);
                t.setClosable(closable);
                if (i == 1) {
                    l
                        .addComponent(new Label(
                            "<h4>Accordion content</h4>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin malesuada volutpat vestibulum. Quisque elementum quam sed sem ultrices lobortis. Pellentesque non ligula ac dolor posuere tincidunt sed eu mi. Integer mattis fringilla nulla, ut cursus mauris scelerisque eu. Etiam bibendum placerat euismod. Nam egestas adipiscing orci sed tristique. Sed vitae enim nisi. Sed ac vehicula ipsum. Nulla quis quam nisi. Proin interdum lacus ipsum, at tristique nibh. Curabitur at ipsum sem. Donec venenatis aliquet neque, sit amet cursus lectus condimentum et. In mattis egestas erat, non cursus metus consectetur ac. Pellentesque eget nisl tellus.",
                            Label.CONTENT_XHTML));
                }
                if (i == 3) {
                    t.setIcon(new ThemeResource("../runo/icons/16/document.png"));
                }
            }
        }

        @Override
        public void setStyleName(String style) {
            super.setStyleName(style);
            Label l = new Label(
                "<h4>"
                    + style
                    + " accordion content</h4>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Proin malesuada volutpat vestibulum.",
                Label.CONTENT_XHTML);
            l.setDescription("Accordion.setStyleName(\"" + style + "\")");
            ((VerticalLayout) getSelectedTab()).removeAllComponents();
            ((VerticalLayout) getSelectedTab()).addComponent(l);
        }
    }

    @Override
    public Component asVaadinComponent() {
        return layout;
    }

    private Layout getPreviewLayout(String caption) {
        Layout grid = new HorizontalLayout();
        grid.setWidth("100%");
        grid.setMargin(true);
        grid.setCaption(caption);
        return grid;
    }
}
