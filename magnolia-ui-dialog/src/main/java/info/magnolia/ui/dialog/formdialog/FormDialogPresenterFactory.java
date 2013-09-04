package info.magnolia.ui.dialog.formdialog;

import info.magnolia.ui.dialog.definition.FormDialogDefinition;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/3/13
 * Time: 1:47 PM
 * To change this template use File | Settings | File Templates.
 */
public interface FormDialogPresenterFactory {

    FormDialogPresenter createFormDialogPresenterByName(String dialogName);

    FormDialogPresenter createFormDialogPresenter(FormDialogDefinition definition);
}
