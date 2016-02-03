/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.vaadin.editor;

import info.magnolia.ui.vaadin.editor.preview.PageEditorPreviewWrapper;
import info.magnolia.ui.vaadin.gwt.client.shared.PageEditorParameters;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

/**
 * Implements {@link PageEditorView}. Depending on current status defined in {@link info.magnolia.ui.vaadin.gwt.client.shared.PageEditorParameters#isPreview()}
 * the view will wrap the {@link PageEditor} inside the {@link PageEditorPreviewWrapper} for styling the preview.
 */
public class PageEditorViewImpl extends CssLayout implements PageEditorView {

    private PageEditor pageEditor = new PageEditor();

    private PageEditorPreviewWrapper previewChrome = new PageEditorPreviewWrapper();

    public PageEditorViewImpl() {
        super();
        addStyleName("pageEditorView");
        setSizeFull();
    }

    @Override
    public void setListener(PageEditorListener listener) {
        pageEditor.setListener(listener);
    }

    @Override
    public void refresh() {
        pageEditor.refresh();
    }

    @Override
    public void load(PageEditorParameters parameters) {
        boolean isPreview = parameters.isPreview();
        if (!isPreview) {
            removeStyleName("previewMode");
            removeComponent(previewChrome);
            addComponent(pageEditor);
        } else {
            addStyleName("previewMode");
            previewChrome.setCurrentPlatform(parameters.getPlatformType());
            previewChrome.setContent(pageEditor);
            addComponent(previewChrome);
        }
        pageEditor.load(parameters);
    }

    @Override
    public void update(PageEditorParameters parameters) {
        pageEditor.update(parameters);
    }

    @Override
    public void startMoveComponent() {
        pageEditor.startMoveComponent();
    }

    @Override
    public void cancelMoveComponent() {
        pageEditor.cancelMoveComponent();
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }
}
