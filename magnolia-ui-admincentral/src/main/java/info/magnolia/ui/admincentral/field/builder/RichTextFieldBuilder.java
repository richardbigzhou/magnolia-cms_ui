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
package info.magnolia.ui.admincentral.field.builder;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.ui.admincentral.app.content.AbstractContentApp;
import info.magnolia.ui.admincentral.dialog.ChooseDialogPresenter;
import info.magnolia.ui.admincentral.dialog.ValueChosenListener;
import info.magnolia.ui.framework.app.App;
import info.magnolia.ui.framework.app.AppController;
import info.magnolia.ui.framework.location.DefaultLocation;
import info.magnolia.ui.model.field.definition.FieldDefinition;
import info.magnolia.ui.model.field.definition.RichTextFieldDefinition;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemAdapter;
import info.magnolia.ui.vaadin.richtext.MagnoliaRichTextField;
import info.magnolia.ui.vaadin.richtext.MagnoliaRichTextFieldConfig;
import info.magnolia.ui.vaadin.richtext.MagnoliaRichTextFieldConfig.ToolbarGroup;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.vaadin.data.Item;
import com.vaadin.ui.Field;

/**
 * Creates and initializes an edit field based on a field definition.
 */
public class RichTextFieldBuilder extends
        AbstractFieldBuilder<RichTextFieldDefinition> {

    private final AppController appController;
    private MagnoliaRichTextField richtexteditor;
    private static final Logger log = LoggerFactory
            .getLogger(LinkFieldBuilder.class);

    @Inject
    public RichTextFieldBuilder(RichTextFieldDefinition definition,
            Item relatedFieldItem, AppController appController) {
        super(definition, relatedFieldItem);
        this.appController = appController;
    }

    @Override
    protected Field buildField() {
        // RichTextFieldDefinition editDefinition = definition;
        MagnoliaRichTextFieldConfig config = new MagnoliaRichTextFieldConfig();

        List<ToolbarGroup> toolbars = new ArrayList<ToolbarGroup>();
        toolbars.add(new ToolbarGroup("basictyles", new String[] { "Bold",
                "Italic", "Underline", "SpecialChar" }));
        toolbars.add(new ToolbarGroup("paragraph", new String[] {
                "NumberedList", "BulletedList" }));
        toolbars.add(new ToolbarGroup("insert", new String[] { "Link", 
                "InternalLink", "Unlink" }));
        toolbars.add(new ToolbarGroup("clipboard", new String[] { "Cut",
                "Copy", "Paste", "PasteText", "PasteFromWord" }));
        toolbars.add(new ToolbarGroup("objects", new String[] { "Image",
                "Table" }));
        toolbars.add(new ToolbarGroup("special",
                new String[] { "Undo", "Redo" }));
        config.addToolbarLine(toolbars);
        config.addListenedEvent("reqMagnoliaLink");
        
        richtexteditor = new MagnoliaRichTextField(config);
        richtexteditor.addListener(new MagnoliaRichTextField.PluginListener() {

            @Override
            public void onPluginEvent(String eventName, String value) {
                if (eventName.equals("reqMagnoliaLink")) {
                    openLinkDialog(value);
                }
            }
        });
        

        return richtexteditor;
    }

    private void openLinkDialog(String path) {
        // Get the property name to propagate.
        App targetApp = appController.startIfNotAlreadyRunning("pages",
                new DefaultLocation(DefaultLocation.LOCATION_TYPE_APP, "pages",
                        "", ""));
        if (targetApp != null && targetApp instanceof AbstractContentApp) {
            ChooseDialogPresenter<Item> pickerPresenter = ((AbstractContentApp) targetApp)
                    .openChooseDialog(path);
            pickerPresenter.getView().setCaption("Select a page");
            pickerPresenter
                    .addValuePickListener(new ValueChosenListener<Item>() {
                        @Override
                        public void onValueChosen(Item pickedValue) {
                            javax.jcr.Item jcrItem = ((JcrItemAdapter) pickedValue)
                                    .getJcrItem();
                            if (jcrItem.isNode()) {
                                final Node selected = (Node) jcrItem;
                                try {                                    
                                    Gson gson = new Gson();
                                    MLink mlink = new MLink();
                                    mlink.identifier = selected.getIdentifier();
                                    mlink.repository = selected.getSession().getWorkspace().getName();
                                    mlink.path = selected.getPath();
                                    if(selected.hasProperty("title")) {
                                        mlink.caption = selected.getProperty("title").getString();
                                    } else {
                                        mlink.caption = selected.getName();
                                    }
                                                                        
                                    richtexteditor.firePluginEvent(
                                            "sendMagnoliaLink",
                                            gson.toJson(mlink)
                                    );
                                } catch (RepositoryException e) {
                                    log.error(
                                            "Not able to access the configured property. Value will not be set.",
                                            e);
                                }
                            }
                        }

                        @Override
                        public void selectionCanceled() {
                            richtexteditor.firePluginEvent("cancelLink");
                        }
                    });
        }
    }
    
    private static class MLink {
        @SuppressWarnings("unused")
        public String identifier;
        @SuppressWarnings("unused")
        public String repository;
        @SuppressWarnings("unused")
        public String path;
        @SuppressWarnings("unused")
        public String caption;
    }

    @Override
    protected Class<?> getDefaultFieldType(FieldDefinition fieldDefinition) {
        return String.class;
    }
}
