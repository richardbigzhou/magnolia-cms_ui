package info.magnolia.ui.dialog.formdialog;

import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.registry.DialogDefinitionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/3/13
 * Time: 1:50 PM
 * To change this template use File | Settings | File Templates.
 */
@Singleton
public class FormDialogPresenterFactoryImpl implements FormDialogPresenterFactory {

    private Logger log = LoggerFactory.getLogger(getClass());

    private DialogDefinitionRegistry registry;

    private ComponentProvider componentProvider;

    @Inject
    public FormDialogPresenterFactoryImpl(DialogDefinitionRegistry registry, ComponentProvider componentProvider) {
        this.registry = registry;
        this.componentProvider = componentProvider;
    }

    @Override
    public FormDialogPresenter createFormDialogPresenterByName(String dialogName) {
        try {
            return createFormDialogPresenter(registry.get(dialogName));
        } catch (RegistrationException e) {
            log.error("Failed to retrieve form dialog definition from registry:", e);
        }
        return null;
    }

    @Override
    public FormDialogPresenter createFormDialogPresenter(FormDialogDefinition definition) {
        return componentProvider.getComponent(definition.getPresenterClass());
    }
}
