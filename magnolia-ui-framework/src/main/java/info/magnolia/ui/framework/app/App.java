package info.magnolia.ui.framework.app;

import info.magnolia.ui.framework.location.Location;

/**
 * @version $Id$
 */
public interface App {

    AppView start(AppContext context, Location location);

    void locationChanged(Location location);

    void stop();

    Location getDefaultLocation();
}
