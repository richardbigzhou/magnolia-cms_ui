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
package info.magnolia.ui.admincentral.shellapp.pulse;

import info.magnolia.context.MgnlContext;
import info.magnolia.ui.admincentral.MagnoliaShell;
import info.magnolia.ui.admincentral.shellapp.pulse.PulseMessageCategoryNavigator.MessageCategory;
import info.magnolia.ui.framework.message.Message;
import info.magnolia.ui.framework.message.MessageType;
import info.magnolia.ui.framework.message.MessagesManager;
import info.magnolia.ui.widget.magnoliashell.gwt.client.VMainLauncher;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import com.vaadin.data.Container;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Container.Filterable;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;

/**
 * Presenter of {@link PulseMessagesView}.
 */
@SuppressWarnings("serial")
public class PulseMessagesPresenter implements Serializable {

    private static final String[] order = new String[] { "CB", "new", "type", "text", "sender", "date", "quickdo" };

    private Filterable container = null;

    private MessagesManager messagesManager;

    private MagnoliaShell shell;

    @Inject
    public PulseMessagesPresenter(final MagnoliaShell magnoliaShell, final MessagesManager messagesManager) {
        this.shell = magnoliaShell;
        this.messagesManager = messagesManager;

        messagesManager.registerMessagesListener(MgnlContext.getUser().getName(), new MessagesManager.MessageListener() {

            @Override
            public void messageSent(Message message) {
                addMessageAsItem(message);
                if (message.getType().isSignificant()) {
                    shell.updateShellAppIndication(VMainLauncher.ShellAppType.PULSE, 1);   
                }
            }

            @Override
            public void messageCleared(Message message) {
                assignPropertiesFromMessage(message, container.getItem(message.getId()));
                if (message.getType().isSignificant() ) {
                    shell.updateShellAppIndication(VMainLauncher.ShellAppType.PULSE, -1); 
                }
            }
        });
    }

    public void setInitialUnreadMessagesIndicator() {
        // TODO -tobias- Calling this here results in an NPE because
        // MagnoliaShell doesn't have an Application instance to synchronize on
        // yet
        shell.updateShellAppIndication(VMainLauncher.ShellAppType.PULSE,
        messagesManager.getNumberOfUnclearedMessagesForUser(MgnlContext.getUser().getName()));    
    }
    
    public Container getMessageDataSource() {
        if (container == null) {
            createMessageDataSource();
        }
        return container;
    }

    private Filterable createMessageDataSource() {
        container = new IndexedContainer();
        container.addContainerProperty("new", String.class, null);
        container.addContainerProperty("type", MessageType.class, MessageType.UNKNOWN);
        container.addContainerProperty("text", String.class, null);
        container.addContainerProperty("sender", String.class, null);
        container.addContainerProperty("date", String.class, null);
        container.addContainerProperty("quickdo", String.class, null);
        for (Message message : messagesManager.getMessagesForUser(MgnlContext.getUser().getName())) {
            addMessageAsItem(message);
        }
        return container;
    }

    private void addMessageAsItem(Message message) {
        final Item item = container.addItem(message.getId());
        assignPropertiesFromMessage(message, item);
    }

    private void assignPropertiesFromMessage(Message message, final Item item) {
        if (item != null && message != null) {
            item.getItemProperty("new").setValue(message.isCleared() ? "No" : "Yes");
            item.getItemProperty("type").setValue(message.getType());
            item.getItemProperty("text").setValue(message.getMessage());
            item.getItemProperty("date").setValue(new SimpleDateFormat().format(new Date(message.getTimestamp())));   
        }
    }

    public Object[] getColumnOrder() {
        return order;
    }

    public void filterByMessageCategory(MessageCategory category) {
        if (container != null) {
            container.removeAllContainerFilters();
            applyCategoryFilter(category);
        }
    }

    private void applyCategoryFilter(final MessageCategory category) {
        final Filter filter = new Filter() {

            @Override
            public boolean passesFilter(Object itemId, Item item) throws UnsupportedOperationException {
                final MessageType type = (MessageType)item.getItemProperty("type").getValue();
                switch (category) {
                case WORK_ITEM:
                    return type == MessageType.WARNING;
                case PROBLEM:
                    return type == MessageType.ERROR;
                case INFO:
                    return type == MessageType.INFO;
                default:
                    return true;
                }
            }

            @Override
            public boolean appliesToProperty(Object propertyId) {
                return "type".equals(propertyId);
            }
            
        };
        container.addContainerFilter(filter);
    }
}
