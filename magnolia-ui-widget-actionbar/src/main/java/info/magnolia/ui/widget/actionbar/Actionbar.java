/**
 * This file Copyright (c) 2010-2012 Magnolia International
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
package info.magnolia.ui.widget.actionbar;

import com.google.gwt.user.client.ui.RootPanel;
import com.vaadin.terminal.Sizeable;
import info.magnolia.ui.vaadin.integration.widget.serializer.ResourceSerializer;
import info.magnolia.ui.widget.actionbar.gwt.client.VActionbar;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.rpc.ServerSideHandler;
import org.vaadin.rpc.ServerSideProxy;
import org.vaadin.rpc.client.Method;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;


/**
 * The Actionbar widget, consisting of sections and groups of actions.
 */
@SuppressWarnings("serial")
@ClientWidget(value = VActionbar.class, loadStyle = LoadStyle.EAGER)
public class Actionbar extends AbstractComponent implements ActionbarView, ServerSideHandler {

    private static final Logger log = LoggerFactory.getLogger(Actionbar.class);

    private boolean isAttached = false;

    private final Map<String, ActionbarSection> sections = new LinkedHashMap<String, ActionbarSection>();

    private ActionbarView.Listener listener;

    private final ServerSideProxy proxy = new ServerSideProxy(this) {

        {
            register("actionTriggered", new Method() {

                @Override
                public void invoke(String methodName, Object[] params) {
                    final String actionToken = String.valueOf(params[0]);
                    listener.onActionbarItemClicked(actionToken);
                }
            });
        }
    };

    public Actionbar() {
        setSizeFull();
        //setWidth("270px");
        setWidth(Sizeable.SIZE_UNDEFINED, 0);
        setImmediate(true);

    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        proxy.paintContent(target);
        for (ActionbarSection section : sections.values()) {
            if (section.getPreview() != null) {
                target.startTag(section.getName() + ":" + "preview");
                section.getPreview().paint(target);
                target.endTag(section.getName() + ":" + "preview");
            }
        }
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        proxy.changeVariables(source, variables);
        for (ActionbarSection section : sections.values()) {
            if (section.getPreview() != null && section.getPreview() instanceof AbstractComponent) {
                ((AbstractComponent) section.getPreview()).changeVariables(source, variables);
            }
        }
    }

    @Override
    public Object[] initRequestFromClient() {
        for (final ActionbarSection section : sections.values()) {
            doAddSection(section);
            for (final ActionbarItem action : section.getActions().values()) {
                doAddAction(action, section.getName());
            }
        }
        return new Object[]{};
    }

    private void doAddSection(final ActionbarSection section) {
        proxy.call("addSection", new Gson().toJson(section));
    }

    private void doRemoveSection(final String sectionName) {
        proxy.call("removeSection", sectionName);
    }

    private void doAddAction(final ActionbarItem action, String sectionName) {
        GsonBuilder gson = new GsonBuilder()
            .registerTypeAdapter(Resource.class, new ResourceSerializer());
        proxy.call("addAction", gson.create().toJson(action), action.getGroupName(), sectionName);
    }

    @Override
    public void attach() {
        super.attach();
        isAttached = true;
    }

    @Override
    public void detach() {
        super.detach();
        clear();
    }

    public void clear() {
        isAttached = false;
        for (final ActionbarSection section : sections.values()) {
            section.getActions().clear();
        }
        sections.clear();
    }

    @Override
    public void callFromClient(String method, Object[] params) {
        throw new RuntimeException("Unknown call from client: " + method);
    }

    @Override
    public Component asVaadinComponent() {
        return this;
    }

    @Override
    public void setListener(ActionbarView.Listener listener) {
        this.listener = listener;
    }

    // ACTION BAR API ///////////////////////////

    @Override
    public void addSection(String sectionName, String caption) {
        final ActionbarSection section = new ActionbarSection(sectionName, caption);
        sections.put(sectionName, section);
        if (isAttached) {
            doAddSection(section);
        }
    }

    @Override
    public void removeSection(String sectionName) {
        sections.remove(sectionName);
        doRemoveSection(sectionName);
    }

    @Override
    public void addAction(String actionName, String label, Resource icon, String groupName, String sectionName) {
        ActionbarSection section = sections.get(sectionName);
        if (section != null) {
            final ActionbarItem action = new ActionbarItem(actionName, label, icon, groupName);
            section.addAction(action);
            if (isAttached) {
                doAddAction(action, sectionName);
            }
        } else {
            log.warn("Action was not added: no section found with name '" + sectionName + "'.");
        }
    }

    @Override
    public void setPreview(Component preview, String sectionName) {
        ActionbarSection section = sections.get(sectionName);
        if (section != null) {
            final VerticalLayout previewContainer = new VerticalLayout();
            previewContainer.setWidth("100%");
            previewContainer.addComponent(preview);
            previewContainer.setStyleName("v-actionbar-preview");
            section.setPreview(previewContainer);
            if (isAttached) {
                requestRepaint();
            }
        } else {
            log.warn("Preview was not added: no section found with name '" + sectionName + "'.");
        }
    }

    public Map<String, ActionbarSection> getSections() {
        return sections;
    }

    // ENABLE / DISABLE /////////////////////////

    @Override
    public void enable(String actionName) {
        for (ActionbarSection section : sections.values()) {
            ActionbarItem action = section.getActions().get(actionName);
            if (action != null) {
                action.setEnabled(true);
            }
        }
        if (isAttached) {
            proxy.call("setActionEnabled", true, actionName);
        }
    }

    @Override
    public void enable(String actionName, String sectionName) {
        ActionbarSection section = sections.get(sectionName);
        if (section != null) {
            ActionbarItem action = section.getActions().get(actionName);
            if (action != null) {
                action.setEnabled(true);
            }
        }
        if (isAttached) {
            proxy.call("setActionEnabled", true, actionName, sectionName);
        }
    }

    @Override
    public void enableGroup(String groupName) {
        for (ActionbarSection section : sections.values()) {
            for (ActionbarItem action : section.getActions().values()) {
                if (groupName.equals(action.getGroupName())) {
                    action.setEnabled(true);
                }
            }
        }
        if (isAttached) {
            proxy.call("setGroupEnabled", true, groupName);
        }
    }

    @Override
    public void enableGroup(String groupName, String sectionName) {
        ActionbarSection section = sections.get(sectionName);
        if (section != null) {
            for (ActionbarItem action : section.getActions().values()) {
                if (groupName.equals(action.getGroupName())) {
                    action.setEnabled(true);
                }
            }
        }
        if (isAttached) {
            proxy.call("setGroupEnabled", true, groupName, sectionName);
        }
    }

    @Override
    public void disable(String actionName) {
        for (ActionbarSection section : sections.values()) {
            ActionbarItem action = section.getActions().get(actionName);
            if (action != null) {
                action.setEnabled(false);
            }
        }
        if (isAttached) {
            proxy.call("setActionEnabled", false, actionName);
        }
    }

    @Override
    public void disable(String actionName, String sectionName) {
        ActionbarSection section = sections.get(sectionName);
        if (section != null) {
            ActionbarItem action = section.getActions().get(actionName);
            if (action != null) {
                action.setEnabled(false);
            }
        }
        if (isAttached) {
            proxy.call("setActionEnabled", false, actionName, sectionName);
        }
    }

    @Override
    public void disableGroup(String groupName) {
        for (ActionbarSection section : sections.values()) {
            for (ActionbarItem action : section.getActions().values()) {
                if (groupName.equals(action.getGroupName())) {
                    action.setEnabled(false);
                }
            }
        }
        if (isAttached) {
            proxy.call("setGroupEnabled", false, groupName);
        }
    }

    @Override
    public void disableGroup(String groupName, String sectionName) {
        ActionbarSection section = sections.get(sectionName);
        if (section != null) {
            for (ActionbarItem action : section.getActions().values()) {
                if (groupName.equals(action.getGroupName())) {
                    action.setEnabled(false);
                }
            }
        }
        if (isAttached) {
            proxy.call("setGroupEnabled", false, groupName, sectionName);
        }
    }

    // SHOW / HIDE SECTIONS /////////////////////

    @Override
    public void showSection(String sectionName) {
        ActionbarSection section = sections.get(sectionName);
        if (section != null) {
            section.setVisible(true);
        }
        if (isAttached) {
            proxy.call("setSectionVisible", true, sectionName);
        }
    }

    @Override
    public void hideSection(String sectionName) {
        ActionbarSection section = sections.get(sectionName);
        if (section != null) {
            section.setVisible(false);
        }
        if (isAttached) {
            proxy.call("setSectionVisible", false, sectionName);
        }
    }

    // SUPPORTING CLASSES ///////////////////////

    /**
     * A section of actions in the action bar.
     */
    public static class ActionbarSection implements Serializable {

        private transient Map<String, ActionbarItem> actions = new LinkedHashMap<String, ActionbarItem>();

        private transient Component preview;

        private final String name;

        private final String caption;

        private boolean visible;

        public ActionbarSection(String name, String caption) {
            this.name = name;
            this.caption = caption;
            this.visible = true;
        }

        public String getName() {
            return name;
        }

        public String getCaption() {
            return caption;
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }

        public Map<String, ActionbarItem> getActions() {
            return actions;
        }

        public void addAction(ActionbarItem action) {
            actions.put(action.getName(), action);
        }

        public Component getPreview() {
            return preview;
        }

        public void setPreview(Component preview) {
            this.preview = preview;
        }
    }

    /**
     * A group of actions in a section of the action bar.
     */
    public static class ActionbarItem implements Serializable {

        private transient String groupName;

        private final String name;

        private final String label;

        private final Resource icon;

        private boolean enabled;

        public ActionbarItem(String name, String label, Resource icon, String groupName) {
            this.name = name;
            this.label = label;
            this.icon = icon;
            this.groupName = groupName;
            this.enabled = true;
        }

        public String getName() {
            return name;
        }

        public String getLabel() {
            return label;
        }

        public Resource getIcon() {
            return icon;
        }

        public String getGroupName() {
            return groupName;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
