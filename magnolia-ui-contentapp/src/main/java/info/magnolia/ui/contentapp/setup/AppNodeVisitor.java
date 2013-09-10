package info.magnolia.ui.contentapp.setup;

import info.magnolia.jcr.util.NodeVisitor;
import info.magnolia.ui.contentapp.ConfiguredContentAppDescriptor;
import info.magnolia.ui.contentapp.ContentApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/10/13
 * Time: 3:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class AppNodeVisitor implements NodeVisitor {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void visit(Node node) throws RepositoryException {
        Property p =  node.getProperty("appClass");
        if (p != null) {
            try {
                Class<?> clazz = Class.forName(p.getValue().getString());
                if (ContentApp.class.isAssignableFrom(clazz)) {
                    node.setProperty("class", ConfiguredContentAppDescriptor.class.getCanonicalName());
                }
            } catch (ClassNotFoundException e) {
                log.error("Failed to resolve app class: " +  p.getValue().getString(), e);
            }

        }
    }
}
