package info.magnolia.ui.contentapp.setup;

import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.QueryTask;
import info.magnolia.module.delta.TaskExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/10/13
 * Time: 3:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class ContentAppDescriptorMigrationTask extends QueryTask {

    private static final Logger log = LoggerFactory.getLogger(ContentAppDescriptorMigrationTask.class);

    private String configRepository;

    public ContentAppDescriptorMigrationTask(String name, String description, String configRepository, String query) {
        super(name, description, configRepository, query);
        this.configRepository = configRepository;
    }

    @Override
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        log.info("Start content app descriptor migration ");
        try {
            super.doExecute(ctx);
            log.info("Successfully execute cleanup of the content repository ");
        } catch (Exception e) {
            log.error("Unable to clean content repository", e);
            ctx.error("Unable to perform Migration task " + getName(), e);
            throw new TaskExecutionException(e.getMessage());
        } finally {
            log.info("Finished content app descriptor migration ");
        }
    }

    @Override
    protected void operateOnNode(InstallContext ctx, Node node) {
        try {
            NodeUtil.visit(node.getParent(), new AppNodeVisitor());
        } catch (RepositoryException e) {
            log.error("Unable to process app node ", e);
        }

    }
}
