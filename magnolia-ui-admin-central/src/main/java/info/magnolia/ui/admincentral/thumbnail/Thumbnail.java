/**
 *
 */
package info.magnolia.ui.admincentral.thumbnail;

import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;

import java.net.URL;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Embedded;

/**
 * @author fgrilli
 *
 */
public class Thumbnail extends Embedded {
    private JcrNodeAdapter node;

    public Thumbnail(final Node node, final URL url) {
        this.node = new JcrNodeAdapter(node);
        setType(TYPE_IMAGE);
        setSource(new ExternalResource(url));
        try {
            setCaption(node.getName());
        } catch (RepositoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public JcrNodeAdapter getNode() {
        return node;
    }
}
