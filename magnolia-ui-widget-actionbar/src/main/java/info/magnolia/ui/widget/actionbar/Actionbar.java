package info.magnolia.ui.widget.actionbar;

import info.magnolia.ui.widget.actionbar.gwt.client.VActionbar;

import java.util.Map;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.ClientWidget.LoadStyle;


@SuppressWarnings("serial")
@ClientWidget(value = VActionbar.class, loadStyle = LoadStyle.EAGER)
public class Actionbar extends AbstractComponent {

    public Actionbar() {
        setWidth("270px");
    }

    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
    }

}
