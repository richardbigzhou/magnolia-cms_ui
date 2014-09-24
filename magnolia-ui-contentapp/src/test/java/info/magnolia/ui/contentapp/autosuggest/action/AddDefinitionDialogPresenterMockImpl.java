/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.contentapp.autosuggest.action;

import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.ui.api.autosuggest.AutoSuggester;
import info.magnolia.ui.contentapp.autosuggest.AddDefinitionActionCallback;
import info.magnolia.ui.contentapp.autosuggest.AddDefinitionDialogPresenter;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.workbench.autosuggest.AutoSuggesterUtil;

import java.util.Collection;

import javax.jcr.Node;

/**
 * AddDefinitionDialogPresenterMockImpl.
 */
public class AddDefinitionDialogPresenterMockImpl implements AddDefinitionDialogPresenter {

    private Collection<String> subNodeNames;
    private Collection<String> subFolderNames;
    private Collection<String> propertyNames;

    private DialogView dialogView;

    public AddDefinitionDialogPresenterMockImpl(DialogView dialogView) {
        this.dialogView = dialogView;
    }

    @Override
    public DialogView start(JcrItemAdapter selectedItem, AutoSuggester autoSuggester, AddDefinitionActionCallback callback) {
        JcrItemId itemId = selectedItem.getItemId();
        Node node = SessionUtil.getNode(itemId.getWorkspace(), itemId.getUuid());

        this.subNodeNames = AutoSuggesterUtil.getSuggestedSubContentNodeNames(autoSuggester, node);
        this.subFolderNames = AutoSuggesterUtil.getSuggestedSubContentNames(autoSuggester, node);
        this.propertyNames = AutoSuggesterUtil.getSuggestedSubPropertyNames(autoSuggester, node);

        return dialogView;
    }

    public Collection<String> getSubNodeNames() {
        return subNodeNames;
    }

    public Collection<String> getPropertyNames() {
        return propertyNames;
    }

    public Collection<String> getSubFolderNames() {
        return subFolderNames;
    }

}
