package info.magnolia.ui.dialog.actionpresenter.renderer;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import info.magnolia.ui.api.action.ActionDefinition;
import info.magnolia.ui.api.view.View;
import info.magnolia.ui.dialog.actionpresenter.ActionListener;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: sasha
 * Date: 9/2/13
 * Time: 3:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultEditorActionRenderer implements ActionRenderer {

    @Override
    public View start(final ActionDefinition definition, final ActionListener listener) {
        return new View() {
            @Override
            public Component asVaadinComponent() {
                Button button = new Button();
                button.addStyleName("btn-dialog");
                button.addStyleName(definition.getName());
                button.setCaption(definition.getLabel());
                button.addClickListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        listener.onActionFired(definition.getName(), new HashMap<String, Object>());
                    }
                });
                return button;
            }
        };
    }
}
