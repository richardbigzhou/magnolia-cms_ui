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
package info.magnolia.ui.admincentral.app.dialog;

import info.magnolia.ui.admincentral.app.AbstractAppView;
import info.magnolia.ui.widget.actionbar.Actionbar;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;


/**
 * View implementation for the Pages app.
 * 
 * @version $Id$
 */
@SuppressWarnings("serial")
public class DialogTestViewImpl extends AbstractAppView<DialogTestPresenter> implements DialogTestView {

    private final HorizontalLayout content = new HorizontalLayout();

    public DialogTestViewImpl() {

        // TODO mgeljic 20120530 define api for assigning an actionbar to an app
        content.addComponent(new Actionbar());

        Button dialog = new Button("Open Dialog", new DialogListener());
        content.addComponent(dialog);
        addTab(content, "Dialog");
    }

    /**
     * Handler of Dialog events.
     * 
     */
    class DialogListener implements ClickListener {

        @Override
        public void buttonClick(ClickEvent event) {
            getPresenter().openDialog();
        }

    }
}
