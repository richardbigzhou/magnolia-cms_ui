/**
 * This file Copyright (c) 2012-2016 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.jquerywrapper;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Wrapper for JQuery Callbacks API.
 */
public class Callbacks extends JavaScriptObject {

    protected Callbacks() {
    }

    public static Callbacks create(final JQueryCallback... callbacks) {
        final Callbacks result = create();
        for (final JQueryCallback callback : callbacks) {
            result.add(callback);
        }
        return result;
    }

    public static native Callbacks create() /*-{
        return $wnd.jQuery.Callbacks();
    }-*/;

    public final void add(final JQueryCallback callback) {
        doAdd(JQueryFunction.create(callback));
    }

    public final void remove(final JQueryCallback callback) {
        doRemove(JQueryFunction.create(callback));
    }

    private final native void doRemove(final JQueryFunction callback) /*-{
        this.remove(callback);
    }-*/;

    private final native void doAdd(final JQueryFunction callback) /*-{
        this.add(callback);
    }-*/;

    public final native void fire(JQueryWrapper jQueryContext) /*-{
        this.fire(jQueryContext);
    }-*/;

    public final native void fire() /*-{
        this.fire();
    }-*/;
}
