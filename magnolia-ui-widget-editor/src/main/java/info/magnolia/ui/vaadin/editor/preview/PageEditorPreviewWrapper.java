/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.ui.vaadin.editor.preview;

import info.magnolia.ui.vaadin.editor.gwt.shared.PlatformType;

import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

/**
 * A simple component that adjusts the user experience of a page preview to the look and feel of the
 * target platform. The component is capable of switching between the portrait and landscape modes.
 * The actual UI is driven with css.
 */
public class PageEditorPreviewWrapper extends CssLayout {

    private PlatformType currentPlatform = PlatformType.DESKTOP;

    private boolean isPortrait = false;

    public PageEditorPreviewWrapper() {
        super();
        setSizeFull();
        addLayoutClickListener(new LayoutEvents.LayoutClickListener() {
            @Override
            public void layoutClick(LayoutEvents.LayoutClickEvent event) {
                setPortraitMode(!isPortrait);
            }
        });
    }

    public void setContent(Component content) {
        addComponent(content);
        content.addStyleName("content");
    }

    public void setCurrentPlatform(PlatformType currentPlatform) {
        this.currentPlatform = currentPlatform;
        updateStyles();
    }

    private void setPortraitMode(boolean isPortrait) {
        this.isPortrait = isPortrait;
        updateStyles();
    }

    private void updateStyles() {
        setStyleName("preview iframe-preview-loading-indicator");
        addStyleName(currentPlatform.getStyleName(isPortrait));
    }
}
