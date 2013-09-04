package info.magnolia.ui.contentapp.choosedialog;

import com.rits.cloning.Cloner;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.AppContext;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.app.SubAppDescriptor;
import info.magnolia.ui.api.overlay.OverlayLayer;
import info.magnolia.ui.contentapp.browser.BrowserSubAppDescriptor;
import info.magnolia.ui.contentapp.field.WorkbenchFieldDefinition;
import info.magnolia.ui.dialog.action.DialogActionExecutor;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogPresenterImpl;
import info.magnolia.ui.dialog.choosedialog.ChooseDialogView;
import info.magnolia.ui.dialog.choosedialog.action.ChooseDialogActionDefinition;
import info.magnolia.ui.dialog.definition.ChooseDialogDefinition;
import info.magnolia.ui.dialog.definition.ConfiguredChooseDialogDefinition;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.imageprovider.definition.ImageProviderDefinition;
import info.magnolia.ui.vaadin.dialog.BaseDialog;
import info.magnolia.ui.workbench.definition.ConfiguredWorkbenchDefinition;
import info.magnolia.ui.workbench.definition.WorkbenchDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/3/13
 * Time: 6:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContentAppChooseDialogPresenter extends ChooseDialogPresenterImpl {

    private Logger log = LoggerFactory.getLogger(getClass());

    private AppContext appContext;

    @Inject
    public ContentAppChooseDialogPresenter(FieldFactoryFactory fieldFactoryFactory, ComponentProvider componentProvider, I18nContentSupport i18nContentSupport, DialogActionExecutor actionExecutor, AppContext appContext) {
        super(fieldFactoryFactory, componentProvider, i18nContentSupport, actionExecutor);
        this.appContext = appContext;
    }

    @Override
    public ChooseDialogView start(ChooseDialogCallback callback, ChooseDialogDefinition definition, OverlayLayer overlayLayer, String selectedItemId) {
        ChooseDialogDefinition dialogDefinition = ensureChooseActions(ensureChooseDialogField(definition));
        return super.start(callback, dialogDefinition, overlayLayer, selectedItemId);
    }

    private ChooseDialogDefinition ensureChooseActions(ChooseDialogDefinition definition) {
        ChooseDialogDefinition result = definition;
        if (!definition.getActions().containsKey(BaseDialog.COMMIT_ACTION_NAME) || !definition.getActions().containsKey(BaseDialog.COMMIT_ACTION_NAME)) {
            result = new Cloner().deepClone(definition);

            if (!result.getActions().containsKey(BaseDialog.COMMIT_ACTION_NAME)) {
                ChooseDialogActionDefinition commitAction = new ChooseDialogActionDefinition();
                commitAction.setCallSuccess(true);
                commitAction.setName("commit");
                commitAction.setLabel("commit");
                result.getActions().put(BaseDialog.COMMIT_ACTION_NAME, commitAction);
            }
        }

        if (!result.getActions().containsKey(BaseDialog.CANCEL_ACTION_NAME)) {
            ChooseDialogActionDefinition cancelAction = new ChooseDialogActionDefinition();
            cancelAction.setCallSuccess(false);
            cancelAction.setName("cancel");
            cancelAction.setLabel("cancel");
            result.getActions().put(BaseDialog.CANCEL_ACTION_NAME, cancelAction);
        }

        return result;
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

        result = new Cloner().deepClone(result);

        BrowserSubAppDescriptor subApp = (BrowserSubAppDescriptor) subAppContext;

        WorkbenchDefinition workbench = new Cloner().deepClone(subApp.getWorkbench());
        // mark definition as a dialog workbench so that workbench presenter can disable drag n drop
        ((ConfiguredWorkbenchDefinition) workbench).setDialogWorkbench(true);

        // Create the Choose Dialog Title
        String chooserLabel = appContext.getLabel() + " chooser";

        ((ConfiguredWorkbenchDefinition) workbench).setName(chooserLabel);
        ImageProviderDefinition imageProvider = new Cloner().deepClone(subApp.getImageProvider());

        WorkbenchFieldDefinition wbFieldDefinition = new WorkbenchFieldDefinition();
        wbFieldDefinition.setWorkbench(workbench);
        wbFieldDefinition.setImageProvider(imageProvider);
        result.setField(wbFieldDefinition);

        result.setPresenterClass(getClass());
        return result;
    }
}
