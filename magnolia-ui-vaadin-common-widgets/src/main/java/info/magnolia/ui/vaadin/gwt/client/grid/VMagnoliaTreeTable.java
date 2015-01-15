/**
 * This file Copyright (c) 2012-2015 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.grid;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.vaadin.client.UIDL;
import com.vaadin.client.ui.VTreeTablePatched;

/**
 * VMagnoliaTreeTable extends VTreeTable by ways that patching is required to expose the necessary private fields.
 */
public class VMagnoliaTreeTable extends VTreeTablePatched {

    @Override
    protected VScrollTableBody createScrollBody() {
        scrollBody = new VMagnoliaTreeTableScrollBody();
        return scrollBody;
    }

    @Override
    protected String buildCaptionHtmlSnippet(UIDL uidl) {
        return (uidl.getTag().equals("column")) ? super.buildCaptionHtmlSnippet(uidl) : uidl.getStringAttribute("caption");
    }

    /**
     * Extension for Scroll body.
     */
    public class VMagnoliaTreeTableScrollBody extends VTreeTableScrollBody {

        protected VMagnoliaTreeTableScrollBody() {
            super();
        }

        @Override
        protected VScrollTableRow createRow(UIDL uidl, char[] aligns2) {
            if (uidl.hasAttribute("gen_html")) {
                // This is a generated row.
                return new VTreeTableGeneratedRow(uidl, aligns2);
            }
            return new VMagnoliaTreeTableRow(uidl, aligns2);
        }

        /**
         * Extension for table row.
         */
        class VMagnoliaTreeTableRow extends VTreeTableRow {

            public VMagnoliaTreeTableRow(UIDL uidl, char[] aligns2) {
                super(uidl, aligns2);
            }

            /*
             * Forked from VTreeTable.
             */
            @Override
            protected boolean addTreeSpacer(UIDL rowUidl) {
                if (cellShowsTreeHierarchy(getElement().getChildCount() - 1)) {
                    Element container = (Element) getElement().getLastChild().getChild(0);

                    if (rowUidl.hasAttribute("icon")) {
                        // icons are in first content cell in TreeTable
                        ImageElement icon = Document.get().createImageElement();
                        icon.setClassName("v-icon");
                        icon.setAlt("icon");
                        icon.setSrc(client.translateVaadinUri(rowUidl.getStringAttribute("icon")));
                        container.insertFirst(icon);
                    }

                    String classname = "v-treetable-treespacer";
                    if (rowUidl.getBooleanAttribute("ca")) {
                        canHaveChildren = true;
                        open = rowUidl.getBooleanAttribute("open");
                        classname += open ? " v-treetable-node-open" : " v-treetable-node-closed";
                        classname += open ? " icon-arrow1_s" : " icon-arrow1_e";
                    }

                    treeSpacer = Document.get().createDivElement();
                    treeSpacer.getStyle().setDisplay(Display.INLINE_BLOCK);
                    treeSpacer.setClassName(classname);
                    container.insertAfter(treeSpacer, container.getFirstChild());
                    depth = rowUidl.hasAttribute("depth") ? rowUidl.getIntAttribute("depth") : 0;
                    setIndent();
                    isTreeCellAdded = true;
                    return true;
                }
                return false;
            }

            @Override
            public void onBrowserEvent(Event event) {
                if (event.getEventTarget().cast() == treeSpacer &&
                        treeSpacer.getClassName().contains("node")) {
                    if (event.getTypeInt() == Event.ONMOUSEDOWN || event.getTypeInt() == Event.ONTOUCHSTART) {
                        sendToggleCollapsedUpdate(getKey());
                        event.stopPropagation();
                        event.preventDefault();
                    }
                    return;
                }
                super.onBrowserEvent(event);
            }

            @Override
            protected void setIndent() {
                if (getIndentWidth() > 0) {
                    treeSpacer.getStyle().setWidth(getIndent(), Unit.PX);
                }
            }

            @Override
            protected void setCellWidth(int cellIx, int width) {
                super.setCellWidth(cellIx, width);
                // MGNLUI-1170: first column need separate width calculation due to tree spacer.
                if (cellIx == 0) {
                    Element cell = DOM.getChild(getElement(), cellIx);
                    Style wrapperStyle = cell.getFirstChildElement().getStyle();
                    wrapperStyle.setPropertyPx("width", width);
                    cell.getStyle().setPropertyPx("width", width);
                }
            }

            @Override
            protected boolean isRenderHtmlInCells() {
                return true;
            }
        }
    }
}
