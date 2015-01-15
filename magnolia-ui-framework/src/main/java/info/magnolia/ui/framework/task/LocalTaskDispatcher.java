/**
 * This file Copyright (c) 2014-2015 Magnolia International
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
package info.magnolia.ui.framework.task;

import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SimpleContext;
import info.magnolia.context.SystemContext;
import info.magnolia.event.EventBus;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.task.event.TaskEvent;
import info.magnolia.ui.api.event.AdmincentralEventBus;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import com.vaadin.server.VaadinSession;

/**
 * Dispatches Task events on an {@link EventBus} for a certain user.
 */
public class LocalTaskDispatcher implements TaskEventDispatcher {

    private final User user;
    private final Locale locale;
    private EventBus eventBus;
    private VaadinSession vaadinSession;
    private ComponentProvider componentProvider;

    @Inject
    public LocalTaskDispatcher(@Named(AdmincentralEventBus.NAME) final EventBus eventBus, VaadinSession vaadinSession, Context context, ComponentProvider componentProvider) {
        this.eventBus = eventBus;
        this.vaadinSession = vaadinSession;
        this.componentProvider = componentProvider;
        this.user = context.getUser();
        this.locale = context.getLocale();
    }

    @Override
    public void onTaskEvent(final TaskEvent taskEvent) {
        vaadinSession.access(new Runnable() {
            @Override
            public void run() {
                boolean hasContext = MgnlContext.hasInstance();
                if (!hasContext) {
                    MgnlContext.setInstance(new SimpleContext(componentProvider.getComponent(SystemContext.class)) {
                        @Override
                        public User getUser() {
                            return LocalTaskDispatcher.this.user;
                        }

                        @Override
                        public Locale getLocale() {
                            return LocalTaskDispatcher.this.locale;
                        }
                    });
                }
                try {
                    eventBus.fireEvent(taskEvent);
                } catch (Exception ignore) {
                } finally {
                    if (!hasContext) {
                        MgnlContext.setInstance(null);
                    }
                }
            }
        });
    }
}
