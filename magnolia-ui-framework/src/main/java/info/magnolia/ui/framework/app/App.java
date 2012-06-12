package info.magnolia.ui.framework.app;

import info.magnolia.ui.framework.location.Location;

/**
 * @version $Id$
 */
public interface App {

    AppView start(AppContext context, String token);

    void tokenChanged(String token);

    void stop();

    Location getDefaultLocation();
}
