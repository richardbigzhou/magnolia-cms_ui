/**
 * This file Copyright (c) 2010-2016 Magnolia International
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
package info.magnolia.ui.contentapp.choosedialog;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.i18nsystem.I18nizer;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.contentapp.browser.BrowserSubAppDescriptor;
import info.magnolia.ui.contentapp.field.WorkbenchFieldDefinition;
import info.magnolia.ui.dialog.actionarea.DialogActionExecutor;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogPresenterImpl;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogView;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredChooseDialogDefinition;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rits.cloning.Cloner;

/**
 * Extension of {@link ChooseDialogPresenterImpl} capable of restoring choose dialog out of {@link BrowserSubAppDescriptor}
 * of an app.
 */
public class ContentAppChooseDialogPresenter extends ChooseDialogPresenterImpl {

    private Logger log = LoggerFactory.getLogger(getClass());

    private AppContext appContext;

    private Cloner cloner;

    @Inject
    public ContentAppChooseDialogPresenter(FieldFactoryFactory fieldFactoryFactory, ComponentProvider componentProvider, I18nContentSupport i18nContentSupport, DialogActionExecutor executor, AppContext appContext, ChooseDialogView view, I18nizer i18nizer, SimpleTranslator simpleTranslator) {
        super(fieldFactoryFactory, componentProvider, i18nContentSupport, executor, view, i18nizer, simpleTranslator);
        this.appContext = appContext;
        this.cloner = new Cloner();
    }

    @Override
    public ChooseDialogView start(ChooseDialogCallback callback, ChooseDialogDefinition definition, UiContext uiContext, String selectedItemId) {
        ChooseDialogDefinition dialogDefinition = ensureChooseDialogField(definition);
        ChooseDialogView chooseDialogView = super.start(callback, dialogDefinition, uiContext, selectedItemId);
        return chooseDialogView;
    }

    private ChooseDialogDefinition ensureChooseDialogField(ChooseDialogDefinition definition) {
        if (definition.getField() != null) {
            return definition;
        }

        ConfiguredChooseDialogDefinition result = (ConfiguredChooseDialogDefinition) definition;
        SubAppDescriptor subAppContext = appContext.getDefaultSubAppDescriptor();
        if (!(subAppContext instanceof BrowserSubAppDescriptor)) {
            log.error("Cannot start workbench choose dialog since targeted app is not a content app");
            return definition;
        }

        result = cloner.deepClone(result);

        BrowserSubAppDescriptor subApp = (BrowserSubAppDescriptor) subAppContext;

        ConfiguredWorkbenchDefinition workbench = (ConfiguredWorkbenchDefinition) (cloner.deepClone(subApp.getWorkbench()));
        // mark definition as a dialog workbench so that workbench presenter can disable drag n drop
        workbench.setDialogWorkbench(true);
        workbench.setIncludeProperties(false);
        // Create the Choose Dialog Title

        ImageProviderDefinition imageProvider = cloner.deepClone(subApp.getImageProvider());

        WorkbenchFieldDefinition wbFieldDefinition = new WorkbenchFieldDefinition();
        wbFieldDefinition.setName("workbenchField");
        wbFieldDefinition.setWorkbench(workbench);
        wbFieldDefinition.setImageProvider(imageProvider);
        result.setField(wbFieldDefinition);
        result.setPresenterClass(getClass());
        return result;
    }
}
