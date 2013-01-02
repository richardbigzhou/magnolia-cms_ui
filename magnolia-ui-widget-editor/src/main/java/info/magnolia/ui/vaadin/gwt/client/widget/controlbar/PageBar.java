/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.vaadin.gwt.client.widget.controlbar;

import info.magnolia.ui.vaadin.gwt.client.editor.dom.MgnlElement;

import com.google.gwt.user.client.ui.Label;

/**
 * Page bar. The HTML output by this widget contains an empty <code>span</code> element with an id
 * called <code>mgnlEditorMainbarPlaceholder</code> as a convenience which can be used by other
 * modules to inject their own DOM elements into the main bar, <strong>once the page editor is
 * loaded (see {@link VPageEditor} and <code>mgnl.PageEditor.onReady(..)</code>)</strong>.
 * <p>
 * I.e., assuming usage of jQuery, a module's own javascript could do something like this
 * <p>
 * {@code jQuery('#mgnlEditorMainbarPlaceholder').append('
 * <p>Blah</p>
 * ')
 * }
 * <p>
 * The placeholder is styled to be automatically centered in the main bar. See this module's editor.css file (id selector #mgnlEditorMainbarPlaceholder).
 * 
 * Note: This class is no longer used. Functionality provided by actionbar etc.
 * 
 * 
 */
public class PageBar extends AbstractBar {

    private final String dialog;

    public PageBar(MgnlElement mgnlElement) {
        super(null, null);

        setWorkspace(mgnlElement.getAttribute("workspace"));
        setPath(mgnlElement.getAttribute("path"));

        dialog = mgnlElement.getAttribute("dialog");

        this.addStyleName("page");

    }

    public void setPageTitle(String title) {
        title += " - " + getPath();

        Label areaName = new Label(title);
        // tooltip. Nice to have when area label is truncated because too long.
        areaName.setTitle(title);
        areaName.setStylePrimaryName("mgnlEditorBarLabel");
        add(areaName);
    }

    @Override
    public String getDialog() {
        return dialog;
    }

}
