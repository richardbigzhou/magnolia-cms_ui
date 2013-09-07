package info.magnolia.ui.dialog.actionpresenter.definition;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/6/13
 * Time: 9:27 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DetailSubAppActionPresenterDefinition extends EditorActionPresenterDefinition {

    List<FormActionItemDefinition> getActions();
}
