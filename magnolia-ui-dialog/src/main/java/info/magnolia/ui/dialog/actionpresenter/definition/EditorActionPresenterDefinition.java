package info.magnolia.ui.dialog.actionpresenter.definition;

import info.magnolia.ui.dialog.actionpresenter.DialogActionPresenter;
import info.magnolia.ui.dialog.definition.SecondaryActionDefinition;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/5/13
 * Time: 11:40 PM
 * To change this template use File | Settings | File Templates.
 */
public interface EditorActionPresenterDefinition extends ActionPresenterDefinition {

    List<SecondaryActionDefinition> getSecondaryActions();

    Class<? extends DialogActionPresenter> getPresenterClass();

}
