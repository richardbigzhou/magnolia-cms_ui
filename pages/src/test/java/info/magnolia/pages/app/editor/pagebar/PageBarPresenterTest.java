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

import static org.mockito.Mockito.*;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.pages.app.editor.PagesEditorSubAppDescriptor;
import info.magnolia.pages.app.editor.extension.DefaultExtensionFactory;
import info.magnolia.pages.app.editor.extension.Extension;
import info.magnolia.pages.app.editor.extension.ExtensionFactory;
import info.magnolia.pages.app.editor.extension.definition.ExtensionDefinition;
import info.magnolia.pages.app.editor.pagebar.definition.ConfiguredPageBarDefinition;
import info.magnolia.pages.app.editor.pagebar.definition.PageBarDefinition;
import info.magnolia.ui.api.app.SubAppContext;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.contentapp.detail.DetailLocation;
import info.magnolia.ui.vaadin.editor.pagebar.PageBarView;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link PageBarPresenter}.
 */
public class PageBarPresenterTest {

    private PageBarPresenter presenter;
    private PageBarView view;
    private Extension extension;
    private SubAppContext subAppContext;
    private PagesEditorSubAppDescriptor subAppDescriptor;
    private ExtensionFactory extensionFactory;

    @Before
    public void setUp() throws Exception {
        this.extension = mock(Extension.class);
        ComponentProvider componentProvider = mock(ComponentProvider.class);
        this.extensionFactory = new DefaultExtensionFactory(componentProvider);
        this.view = mock(PageBarView.class);
        this.subAppContext = mock(SubAppContext.class);
        this.subAppDescriptor = new PagesEditorSubAppDescriptor();

        when(componentProvider.newInstance(eq(Extension.class), any(ExtensionDefinition.class))).thenReturn(extension);
        when(extension.start()).thenReturn(mock(View.class));
        when(subAppContext.getSubAppDescriptor()).thenReturn(subAppDescriptor);

    }

    @Test
    public void testExtensionLifecycle() throws Exception {
        // GIVEN
        subAppDescriptor.setPageBar(createDefinition());

        this.presenter = new PageBarPresenter(view, subAppContext, extensionFactory);

        // WHEN
        presenter.start();
        presenter.onLocationUpdate(mock(DetailLocation.class));
        presenter.deactivateExtensions();

        // THEN
        verify(view, times(1)).addPageBarComponent(any(View.class));
        verify(extension).start();
        verify(extension).onLocationUpdate(any(DetailLocation.class));
        verify(extension).deactivate();
    }

    @Test
    public void testMissingPageBarDefinition() throws Exception {
        // GIVEN
        this.presenter = new PageBarPresenter(view, subAppContext, extensionFactory);

        // WHEN
        presenter.start();
        presenter.onLocationUpdate(mock(DetailLocation.class));
        presenter.deactivateExtensions();

        // THEN
        verify(view, times(0)).addPageBarComponent(any(View.class));
    }

    private PageBarDefinition createDefinition() {
        Map<String, ExtensionDefinition> extensions = new HashMap<String, ExtensionDefinition>();

        extensions.put("languageSelector", new ExtensionDefinition() {
            @Override
            public Class<? extends Extension> getExtensionClass() {
                return Extension.class;
            }
        });

        ConfiguredPageBarDefinition definition = new ConfiguredPageBarDefinition();
        definition.setExtensions(extensions);

        return definition;
    }
}
