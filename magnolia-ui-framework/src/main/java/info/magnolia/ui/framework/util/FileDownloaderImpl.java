package info.magnolia.ui.framework.util;

import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;

/**
 * Implementation for {@link FileDownloader}
 */
public class FileDownloaderImpl implements FileDownloader {

    @Override
    public void downloadFile(String fileName, String mimeType, final FileInputStream fileInputStream) {
        StreamResource.StreamSource source = new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                return fileInputStream;
            }
        };
        StreamResource resource = new StreamResource(source, fileName);
        // Accessing the DownloadStream via getStream() will set its cacheTime to whatever is set in the parent
        // StreamResource. By default it is set to 1000 * 60 * 60 * 24, thus we have to override it beforehand.
        // A negative value or zero will disable caching of this stream.
        resource.setCacheTime(-1);
        resource.getStream().setParameter("Content-Disposition", "attachment; filename=" + fileName + "\"");

        if (StringUtils.isBlank(mimeType)) {
            resource.setMIMEType(mimeType);
        }

        Page.getCurrent().open(resource, "", true);
    }

    @Override
    public void downloadFile(String fileName, final FileInputStream fileInputStream) {
        downloadFile(fileName, null, fileInputStream);
    }
}
