package info.magnolia.ui.legacy.admininterface.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.RemoveNodeTask;
import info.magnolia.module.delta.Task;

import java.util.List;

/**
 * This version handler removes on install the old command definitions from the
 * admin interface module (if installed).
 */
public class AdminInterfaceLegacyModuleVersionHandler extends DefaultModuleVersionHandler {

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        List<Task> tasks = super.getExtraInstallTasks(installContext);
        tasks.add(new RemoveNodeTask("Remove default activate command",
                "Removes the activate command definition from the default catalog (path /modules/adminInterface/commands/default/activate).",
                "config", "/modules/adminInterface/commands/default/activate"));
        tasks.add(new RemoveNodeTask("Remove default deactivate command",
                "Removes the deactivate command definition from the default catalog (path /modules/adminInterface/commands/default/deactivate).",
                "config", "/modules/adminInterface/commands/default/deactivate"));
        tasks.add(new RemoveNodeTask("Remove website activate command",
                "Removes the activate command definition from the website catalog (path /modules/adminInterface/commands/website/activate).",
                "config", "/modules/adminInterface/commands/website/activate"));
        return tasks;
    }

}
