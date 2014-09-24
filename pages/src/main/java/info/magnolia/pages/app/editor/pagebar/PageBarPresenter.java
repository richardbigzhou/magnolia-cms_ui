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
package info.magnolia.pages.app.editor.pagebar;

import info.magnolia.pages.app.editor.PagesEditorSubAppDescriptor;
import info.magnolia.pages.app.editor.extension.Extension;
import info.magnolia.pages.app.editor.extension.ExtensionContainer;
import info.magnolia.pages.app.editor.extension.ExtensionFactory;
import info.magnolia.pages.app.editor.extension.definition.ExtensionDefinition;
import info.magnolia.pages.app.editor.pagebar.definition.PageBarDefinition;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.contentapp.detail.DetailView;
import info.magnolia.ui.vaadin.editor.pagebar.PageBarView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Presenter for the page bar displayed on top of the page editor. Acts as container for {@link Extension}s defined under
 * the pageBar.
 */
public class PageBarPresenter implements ExtensionContainer, PageBarView.Listener {

    private static final Logger log = LoggerFactory.getLogger(PageBarPresenter.class);

    private final PageBarView view;
    private final SubAppContext subAppContext;
    private final ExtensionFactory extensionFactory;

    private List<Extension> extensions = new ArrayList<Extension>();

    @Inject
    public PageBarPresenter(PageBarView view, SubAppContext subAppContext, ExtensionFactory extensionFactory) {
        this.view = view;
        this.subAppContext = subAppContext;
        this.extensionFactory = extensionFactory;
    }

    @Override
    public PageBarView start() {
        SubAppDescriptor subAppDescriptor = subAppContext.getSubAppDescriptor();

        if (subAppDescriptor instanceof PagesEditorSubAppDescriptor) {
            PageBarDefinition definition =  ((PagesEditorSubAppDescriptor) subAppDescriptor).getPageBar();
            if (definition != null) {
                Map<String, ExtensionDefinition> extensionDefinitions = definition.getExtensions();
                extensions = extensionFactory.createExtensions(extensionDefinitions);
            } else {
                log.error("No pageBar definition defined for pages detail app, no extensions will be loaded.");
            }
        } else {
            log.error("Expected an instance of {} but got {}. No extensions will be loaded.", PagesEditorSubAppDescriptor.class.getSimpleName(), subAppDescriptor.getClass().getName());
        }

        for (Extension extension : extensions) {
            view.addPageBarComponent(extension.start());
        }
        view.setListener(this);
        return view;
    }

    public void setPageName(String caption, String nodePath) {
        String pageName = caption.toUpperCase() + "  -  " + nodePath;
        view.setPageName(pageName);
    }

    public void setPageName(String pageName) {
        view.setPageName(pageName);
    }

    private void togglePreviewMode(boolean preview) {
        view.togglePreviewMode(preview);
    }

    public PageBarView getView() {
        return view;
    }

    @Override
    public void onLocationUpdate(DetailLocation location) {
        for (Extension extension : extensions) {
            extension.onLocationUpdate(location);
        }

        boolean isPreview = DetailView.ViewType.VIEW.equals(location.getViewType());
        togglePreviewMode(isPreview);
    }

    @Override
    public void deactivateExtensions() {
        for (Extension extension : extensions) {
            extension.deactivate();
        }
    }

}
