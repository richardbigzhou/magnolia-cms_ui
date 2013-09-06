package info.magnolia.ui.dialog.actionpresenter.definition;

import info.magnolia.ui.dialog.actionpresenter.renderer.ActionRenderer;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/5/13
 * Time: 12:17 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ActionRendererDefinition {

    Class<? extends ActionRenderer> getPresenterClass();
}
