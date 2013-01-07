package org.vaadin.openesignforms.ckeditor.widgetset.client.ui;

import org.vaadin.openesignforms.ckeditor.CKEditorTextField;

import com.vaadin.client.ui.LegacyConnector;
import com.vaadin.client.ui.SimpleManagedLayout;
import com.vaadin.shared.ui.Connect;

@Connect(CKEditorTextField.class)
public class CKEditorConnector extends LegacyConnector  implements SimpleManagedLayout {
    
    @Override
    public VCKEditorTextField getWidget() {
        return (VCKEditorTextField) super.getWidget();
    }

    @Override
    public void layout() {
        // TODO Auto-generated method stub
        
    } 

}
