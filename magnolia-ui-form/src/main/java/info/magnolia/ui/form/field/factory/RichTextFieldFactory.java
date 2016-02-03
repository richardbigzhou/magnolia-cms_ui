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
package info.magnolia.ui.form.field.factory;

import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.form.field.definition.RichTextFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.richtext.MagnoliaRichTextField;
import info.magnolia.ui.vaadin.richtext.MagnoliaRichTextFieldConfig;
import info.magnolia.ui.vaadin.richtext.MagnoliaRichTextFieldConfig.ToolbarGroup;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.vaadin.data.Item;
import com.vaadin.server.VaadinService;
import com.vaadin.server.WebBrowser;
import com.vaadin.ui.Field;

/**
 * Creates and initializes an edit field based on a field definition.
 */
public class RichTextFieldFactory extends AbstractFieldFactory<RichTextFieldDefinition, String> {

    private static final String PLUGIN_NAME_MAGNOLIALINK = "magnolialink";

    private static final String PLUGIN_PATH_MAGNOLIALINK = "/VAADIN/js/magnolialink/";

    /**
     * Event is emit from server to client when link has been selected.
     */
    public static final String EVENT_SEND_MAGNOLIA_LINK = "mgnlLinkSelected";

    /**
     * Event is emit from server to client when link dialog has been
     * canceled or exception has occurred. In case of exception
     * the event will carry error message.
     */
    public static final String EVENT_CANCEL_LINK = "mgnlLinkCancel";

    /**
     * Event is emit from client to server when user requests a link dialog.
     * Event carries optional link that should be treated as default link value.
     */
    public static final String EVENT_GET_MAGNOLIA_LINK = "mgnlGetLink";

    private final AppController appController;
    private MagnoliaRichTextField richTextEditor;
    private static final Logger log = LoggerFactory.getLogger(LinkFieldFactory.class);

    private final UiContext uiContext;
    private final SimpleTranslator i18n;

    @Inject
    public RichTextFieldFactory(RichTextFieldDefinition definition, Item relatedFieldItem, AppController appController, UiContext uiContext, SimpleTranslator i18n) {
        super(definition, relatedFieldItem);
        this.appController = appController;
        this.uiContext = uiContext;
        this.i18n = i18n;
    }

    @Override
    protected Field<String> createFieldComponent() {
        // RichTextFieldDefinition editDefinition = definition;
        final MagnoliaRichTextFieldConfig config = new MagnoliaRichTextFieldConfig();

        List<ToolbarGroup> toolbars = new ArrayList<ToolbarGroup>();
        toolbars.add(new ToolbarGroup("basictyles", new String[] { "Bold", "Italic", "Underline", "SpecialChar" }));
        toolbars.add(new ToolbarGroup("paragraph", new String[] { "NumberedList", "BulletedList" }));
        toolbars.add(new ToolbarGroup("insert", new String[] { "Link", "InternalLink", "DamLink", "Unlink" }));
        toolbars.add(new ToolbarGroup("clipboard", new String[] { "Cut", "Copy", "Paste", "PasteText", "PasteFromWord" }));
        toolbars.add(new ToolbarGroup("objects", new String[] { "Table" }));
        toolbars.add(new ToolbarGroup("special", new String[] { "Undo", "Redo" }));
        config.addToolbarLine(toolbars);
        config.addListenedEvent(EVENT_GET_MAGNOLIA_LINK);
        config.setResizeEnabled(false);

        richTextEditor = new MagnoliaRichTextField(config) {
            @Override
            public void attach() {
                super.attach();
                String path = VaadinService.getCurrentRequest().getContextPath();
                config.addPlugin(PLUGIN_NAME_MAGNOLIALINK, path + PLUGIN_PATH_MAGNOLIALINK);

                WebBrowser browser = getSession().getBrowser();
                if (browser.isTouchDevice()) {
                    // MGNLUI-1528: Workaround.
                    richTextEditor.setEnabled(false);
                    richTextEditor.setReadOnly(true);
                    richTextEditor.addStyleName("richtextfield-disabled");
                }

            }
        };

        richTextEditor.addListener(new MagnoliaRichTextField.PluginListener() {

            @Override
            public void onPluginEvent(String eventName, String value) {

                if (eventName.equals(EVENT_GET_MAGNOLIA_LINK)) {
                    try {
                        Gson gson = new Gson();
                        PluginData pluginData = gson.fromJson(value, PluginData.class);
                        openLinkDialog(pluginData.path, pluginData.workspace);
                    } catch (Exception e) {
                        log.error("openLinkDialog failed", e);
                        richTextEditor.firePluginEvent(EVENT_CANCEL_LINK, i18n.translate("ui-form.richtexteditorexception.opentargetappfailure"));
                    }
                }
            }
        });

        return richTextEditor;
    }

    private static class PluginData {
        public String workspace;
        public String path;
    }

    private String mapWorkSpaceToApp(String workspace) {
        if (workspace.equalsIgnoreCase("dam")) {
            return "assets";
        } else if (workspace.equalsIgnoreCase("website")) {
            return "pages";
        }

        return "";
    }

    private void openLinkDialog(String path, String workspace) {

        appController.openChooseDialog(mapWorkSpaceToApp(workspace), uiContext, null, new ChooseDialogCallback() {

            @Override
            public void onItemChosen(String actionName, Item chosenValue) {
                if (!(chosenValue instanceof JcrItemAdapter)) {
                    richTextEditor
                            .firePluginEvent(EVENT_CANCEL_LINK);
                    return;
                }

                try {

                    javax.jcr.Item jcrItem = ((JcrItemAdapter) chosenValue).getJcrItem();

                    if (!jcrItem.isNode()) {
                        return;
                    }

                    final Node selected = (Node) jcrItem;
                    Gson gson = new Gson();
                    MagnoliaLink mlink = new MagnoliaLink();
                    mlink.identifier = selected.getIdentifier();
                    mlink.repository = selected.getSession().getWorkspace().getName();
                    mlink.path = selected.getPath();
                    if (selected.hasProperty("title")) {
                        mlink.caption = selected.getProperty("title").getString();
                    } else {
                        mlink.caption = selected.getName();
                    }

                    richTextEditor.firePluginEvent(EVENT_SEND_MAGNOLIA_LINK, gson.toJson(mlink));
                } catch (Exception e) {
                    String error = i18n.translate("ui-form.richtexteditorexception.cannotaccessselecteditem");
                    log.error(error, e);
                    richTextEditor.firePluginEvent(EVENT_CANCEL_LINK, error);
                }
            }

            @Override
            public void onCancel() {
                richTextEditor.firePluginEvent(EVENT_CANCEL_LINK);
            }
        });
    }

    private static class MagnoliaLink {
        @SuppressWarnings("unused")
        public String identifier;
        @SuppressWarnings("unused")
        public String repository;
        @SuppressWarnings("unused")
        public String path;
        @SuppressWarnings("unused")
        public String caption;
    }
}
