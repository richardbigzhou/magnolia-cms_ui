package info.magnolia.ui.contentapp.movedialog;

import info.magnolia.ui.dialog.actionpresenter.ActionPresenter;
import info.magnolia.ui.framework.action.MoveLocation;

import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/7/13
 * Time: 8:19 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MoveDialogActionPresenter extends ActionPresenter {

    void setPossibleMoveLocations(Set<MoveLocation> possibleMoveLocations);
}
