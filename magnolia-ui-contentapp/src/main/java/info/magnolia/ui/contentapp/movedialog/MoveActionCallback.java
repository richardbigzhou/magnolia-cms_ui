package info.magnolia.ui.contentapp.movedialog;

import com.vaadin.data.Item;
import info.magnolia.ui.framework.action.MoveLocation;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/8/13
 * Time: 11:32 AM
 * To change this template use File | Settings | File Templates.
 */
public interface MoveActionCallback {

    void onMoveCancelled();

    void onMovePerformed(Item newHost, MoveLocation moveLocation);
}
