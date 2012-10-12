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

import javax.jcr.Node;
import javax.jcr.Session;

import com.google.inject.Inject;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.MagnoliaShell;
import info.magnolia.ui.admincentral.dialog.FormDialogPresenter;
import info.magnolia.ui.admincentral.dialog.FormDialogPresenterFactory;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.framework.view.View;
import info.magnolia.ui.vaadin.integration.jcr.DefaultProperty;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

/**
 * Presenter for form showcase.
 */
public class FormsPresenter implements FormsView.Listener {

    private FormsView view;
    private MagnoliaShell shell;
    private FormDialogPresenterFactory formFactory;

    @Inject
    public FormsPresenter(FormsView formsView, Shell shell, FormDialogPresenterFactory formFactory) {
        this.view = formsView;
        this.shell = (MagnoliaShell) shell;
        this.formFactory = formFactory;
    }

    public View start() {
        view.setListener(this);
        return view;
    }

    @Override
    public void onViewInDialog() {
        try {
        String workspace = "website";
        String path = "/";
        FormDialogPresenter formPresenter = formFactory.createDialogPresenterByName("ui-showcase-app:showcasedialog");
        Session session = MgnlContext.getJCRSession(workspace);

        Node parentNode = session.getNode(path);

        final JcrNodeAdapter item = new JcrNewNodeAdapter(parentNode, MgnlNodeType.NT_COMPONENT);
        DefaultProperty property = new DefaultProperty(JcrNodeAdapter.JCR_NAME, "0");
        item.addItemProperty(JcrNodeAdapter.JCR_NAME, property);
        
        formPresenter.start(item, new FormDialogPresenter.Callback() {

            @Override
            public void onCancel() {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onSuccess(String actionName) {
                // TODO Auto-generated method stub
                
            }
            
        });
        } catch(Exception e) {
            
        }
        //shell.addDialog(view.asBaseDialog().asVaadinComponent());
    }

    @Override
    public void onCloseDialog() {
        shell.removeDialog(view.asBaseDialog());
    }
}

