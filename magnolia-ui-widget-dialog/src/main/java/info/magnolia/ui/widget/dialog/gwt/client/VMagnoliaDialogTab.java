/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.widget.dialog.gwt.client;

import java.util.List;

import info.magnolia.ui.vaadin.widget.tabsheet.client.VMagnoliaTab;
import info.magnolia.ui.widget.dialog.gwt.client.dialoglayout.DialogFieldWrapper;
import info.magnolia.ui.widget.dialog.gwt.client.dialoglayout.VDialogTabLayout;

import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.Util;

/**
 * Dialog tab.
 */
public class VMagnoliaDialogTab extends VMagnoliaTab {

    private VDialogTabLayout content;
    
    @Override
    public void setWidget(Widget w) {
        if (!(w instanceof VDialogTabLayout)) {
            throw new RuntimeException("Invalid type of tab content. Must be VDialogLayout. You have used: " + w.getClass());
        }
        content = (VDialogTabLayout)w;
        super.setWidget(w);
    }
    
    public VDialogTabLayout getFields() {
        return content;
    }

    public void setDescriptionVisible(boolean visible) {
        if (content != null) {
            content.setDescriptionVisible(visible);
        }
    }

    public void setValidationVisible(boolean isVisible) {
        if (content != null) {
            content.setValidationVisible(isVisible);
        }
    }

    @Override
    public void setHasError(boolean hasError) {
        super.setHasError(hasError);
        final VMagnoliaDialog dialog = getDialog();
        if (dialog != null) {
            dialog.updateErrorAmount();
        }
    }
   
    private VMagnoliaDialog getDialog() {
        return Util.findWidget(getElement(), VMagnoliaDialog.class);
    }

    public int getErorAmount() {
        return content.getErrorAmount();
    }

    public List<DialogFieldWrapper> getProblematicFields() {
        return content.getProblematicFields();
    }

}
