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
package info.magnolia.m5admincentral.shellapp.favorites;

import info.magnolia.m5admincentral.components.SplitFeed;
import info.magnolia.m5admincentral.components.SplitFeed.FeedSection;
import info.magnolia.ui.widget.magnoliashell.IsVaadinComponent;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMainLauncher.ShellAppType;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

/**
 * Default view implementation for favorites.
 *
 * @version $Id$
 */
@SuppressWarnings("serial")
public class FavoritesViewImpl extends CustomComponent implements FavoritesView, IsVaadinComponent {

    private SplitFeed splitPanel = new SplitFeed();
    
    public FavoritesViewImpl() {
        super();
        setHeight("100%");
        setWidth("900px");
        setCompositionRoot(splitPanel);
        construct();
    }

    private void construct() {
        final FeedSection newPagesConatiner = splitPanel.getLeftContainer();
        final FeedSection shortcutContainer = splitPanel.getRightContainer();
        
        newPagesConatiner.setTitle("New Pages");
        shortcutContainer.setTitle("Shortcuts");    
        
        newPagesConatiner.addComponent(new FavoriteEntry(FavoriteEntry.EntryType.ET_NEWPAGES));
        newPagesConatiner.addComponent(new FavoriteEntry(FavoriteEntry.EntryType.ET_NEWPAGES));
        
        shortcutContainer.addComponent(new FavoriteEntry(FavoriteEntry.EntryType.ET_SHORTCUTS));
        shortcutContainer.addComponent(new FavoriteEntry(FavoriteEntry.EntryType.ET_SHORTCUTS));
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }
    
    private static class FavoriteEntry extends Label {
        
        public enum EntryType {
            ET_NEWPAGES("newpages"),
            ET_SHORTCUTS("shortcuts");
            
            private final String id;
            
            private EntryType(final String id) {
                this.id = id;
            }
            
            public String getId() {
                return id;
            }
        }
        
        private EntryType type;
        
        public FavoriteEntry(final EntryType type) {
            super();
            this.type = type;
            addStyleName("v-feed-entry");
            setSizeUndefined();
            setContentMode(Label.CONTENT_XHTML);
            setValue("Add news article");
        }
        
        @Override
        public void setValue(Object newValue) {
            final StringBuilder sb = new StringBuilder();
            sb.append("<div class=\"v-entry-content\">");
            sb.append("<div class=\"v-entry-icon ");
            sb.append(type.getId());
            sb.append("\"");
            sb.append("></div>");
            sb.append(newValue);
            sb.append("</div>");
            super.setValue(sb.toString());
        }
    }

    @Override
    public String getAppName() {
        return ShellAppType.FAVORITE.name();
    }
}
