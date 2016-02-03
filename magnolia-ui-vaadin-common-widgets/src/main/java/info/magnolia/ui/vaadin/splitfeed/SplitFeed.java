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
package info.magnolia.ui.vaadin.splitfeed;

import java.util.Iterator;

import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Split panel that displays two column feed.
 */
public class SplitFeed extends HorizontalSplitPanel {

    private final FeedSection leftContainer = new FeedSection();

    private final FeedSection rightContainer = new FeedSection();

    public SplitFeed() {
        super();
        addStyleName("v-split-feed");
        setSizeFull();
        setSplitPosition(50);
        setLocked(true);
        construct();
    }

    private void construct() {
        leftContainer.setSizeFull();
        rightContainer.setSizeFull();
        // leftContainer.setMargin(true);
        // rightContainer.setMargin(true);
        setFirstComponent(leftContainer);
        setSecondComponent(rightContainer);
    }

    public FeedSection getLeftContainer() {
        return leftContainer;
    }

    public FeedSection getRightContainer() {
        return rightContainer;
    }

    /**
     * Title for the feed section.
     */
    public static class FeedTitle extends Label {

        public FeedTitle(final String caption) {
            super();
            addStyleName("v-feed-title");
            setContentMode(Label.CONTENT_XHTML);
            setValue(caption);
        }
    }

    /**
     * Feed section (column).
     */
    public static class FeedSection extends CssLayout {

        private final NativeButton link = new NativeButton();

        public FeedSection() {
            super();
            setSizeFull();
            addStyleName("v-feed-section");
            link.setStyleName(BaseTheme.BUTTON_LINK);
            link.addStyleName("icon-rssfeed");
        }

        public void setTitleLinkEnabled(boolean enabled) {
            if (!enabled) {
                removeComponent(link);
            } else {
                addComponent(link);
            }
        }

        @Override
        public void addComponent(Component component) {
            if (component instanceof FeedTitle) {
                final Iterator<Component> it = getComponentIterator();
                while (it.hasNext()) {
                    final Component c = it.next();
                    if (c instanceof FeedTitle) {
                        removeComponent(c);
                        break;
                    }
                }
            }
            super.addComponent(component);
        }

        public void setTitle(final String caption) {
            addComponentAsFirst(new FeedTitle(caption));
        }
    }
}
