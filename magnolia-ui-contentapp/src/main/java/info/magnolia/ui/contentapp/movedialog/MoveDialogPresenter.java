package info.magnolia.ui.contentapp.movedialog;

import info.magnolia.ui.contentapp.browser.BrowserSubAppDescriptor;
import info.magnolia.ui.dialog.DialogView;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/8/13
 * Time: 12:45 AM
 * To change this template use File | Settings | File Templates.
 */
public interface MoveDialogPresenter {

    DialogView start(BrowserSubAppDescriptor subAppDescriptor, List<JcrNodeAdapter> nodesToMove, MoveActionCallback callback);
}
