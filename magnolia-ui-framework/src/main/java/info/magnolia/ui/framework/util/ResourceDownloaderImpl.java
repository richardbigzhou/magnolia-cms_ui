package info.magnolia.ui.framework.util;

import com.vaadin.server.Page;
import com.vaadin.server.Resource;

/**
 * Implementation for {@link ResourceDownloader}
 */
public class ResourceDownloaderImpl implements ResourceDownloader {

    @Override
    public void download(Resource resource) {
        Page.getCurrent().open(resource, "", true);
    }
}
