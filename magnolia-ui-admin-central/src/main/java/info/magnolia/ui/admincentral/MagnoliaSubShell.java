package info.magnolia.ui.admincentral;

import info.magnolia.ui.framework.event.HandlerRegistration;
import info.magnolia.ui.framework.shell.ConfirmationHandler;
import info.magnolia.ui.framework.shell.FragmentChangedHandler;
import info.magnolia.ui.framework.shell.Shell;
import info.magnolia.ui.widget.magnoliashell.BaseMagnoliaShell;

@SuppressWarnings("serial")
public class MagnoliaSubShell extends BaseMagnoliaShell implements Shell {

    private MagnoliaShell parent;
    private String id;

    public MagnoliaSubShell(String id, MagnoliaShell parent) {
        this.id = id;
        this.parent = parent;
    }

    public void askForConfirmation(String message, ConfirmationHandler listener) {
        parent.askForConfirmation(message, listener);
    }

    public void showNotification(String message) {
        parent.showNotification(message);
    }

    public void showError(String message, Exception e) {
        parent.showError(message, e);
    }

    public String getFragment() {
        return parent.getFragment();
    }
    public void openWindow(String uri, String windowName) {
        parent.openWindow(uri, windowName);
    }

    @Override
    public void setFragment(String fragment) {
        parent.setFragment(fragment);

    }
    @Override
    public HandlerRegistration addFragmentChangedHandler(FragmentChangedHandler handler) {
        return parent.addFragmentChangedHandler(handler);
    }

    @Override
    public Shell createSubShell(String id) {
       throw new UnsupportedOperationException("A MagnoliaSubShell cannot create a sub shell.");
    }
}