/**
 * This file Copyright (c) 2014-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.editor.jsni.scroll;

import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlComponent;
import info.magnolia.ui.vaadin.gwt.client.jquerywrapper.JQueryWrapper;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;

/**
 * Tracks the initial offset top position of an element and is capable of restoring it later.
 */
public class ElementScrollPositionPreserver {

    private Element element = null;

    private int currentOffsetTop = -1;

    public ElementScrollPositionPreserver(MgnlComponent component) {
        this.element = component.getControlBar() != null ? component.getControlBar().getElement() : component.getFirstElement();
        currentOffsetTop = getOffsetTop();
    }

    public void restorePosition() {
        int newBarTop = getOffsetTop();
        int delta = newBarTop - currentOffsetTop;
        final JQueryWrapper parent = getScrollParent(element);
        parent.setScrollTop(parent.getScrollTop() + delta);
    }

    private int getOffsetTop() {
        final Style style = element.getStyle();
        final String displayValue = style.getDisplay();

        style.clearDisplay();
        int offsetTop = element.getOffsetTop();
        if ("none".equalsIgnoreCase(displayValue)) {
            offsetTop -= element.getOffsetHeight();
        }
        style.setProperty("display", displayValue);
        return offsetTop;
    }

    /**
     * Locates the nearest scrollable parent. If there's none - returns body element.
     * Almost verbatim copy of JQuery UI getScrollParent() function.
     * @see <a href="http://api.jqueryui.com/scrollParent/">getScrollParent()</a>
     */
    private native JQueryWrapper getScrollParent(Element e) /*-{
        var jq = $wnd.$(e);
        jq.scrollParent = function( includeHidden ) {
            var position = this.css( "position" ),
                excludeStaticParent = position === "absolute",
                overflowRegex = includeHidden ? /(auto|scroll|hidden)/ : /(auto|scroll)/,
                scrollParent = this.parents().filter( function() {
                    var parent = $wnd.$( this );
                    if ( excludeStaticParent && parent.css( "position" ) === "static" ) {
                        return false;
                    }
                    return overflowRegex.test( parent.css( "overflow" ) + parent.css( "overflow-y" ) + parent.css( "overflow-x" ) );
                }).eq( 0 );
            return position === "fixed" || !scrollParent.length ? $wnd.$( this[ 0 ].ownerDocument || document) : scrollParent;
        };
        return jq.scrollParent(false);
    }-*/;
}
