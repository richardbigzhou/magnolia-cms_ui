package info.magnolia.ui.api.app;

import com.vaadin.data.Item;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 8/29/13
 * Time: 10:11 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ChooseDialogCallback {

    void onCancel();

    void onItemChosen(String actionName, Item item);
}
