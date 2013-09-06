package info.magnolia.ui.dialog.actionpresenter;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/2/13
 * Time: 3:41 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ActionListener {

    void onActionFired(String actionName, Object... actionContextParams);
}
