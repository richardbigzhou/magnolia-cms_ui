/**
 * This file Copyright (c) 2010-2012 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.ui.vaadin.integration.widget;

import java.util.Map;

import info.magnolia.ui.vaadin.integration.widget.client.VMagnoliaRichTextField;

import org.vaadin.openesignforms.ckeditor.CKEditorConfig;
import org.vaadin.openesignforms.ckeditor.CKEditorTextField;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;

/**
 * Extended CKEditorTextField.
 */
@com.vaadin.ui.ClientWidget(VMagnoliaRichTextField.class)
public class MagnoliaRichTextField extends CKEditorTextField{

    private static final long serialVersionUID = -5194325714251243359L;
    
    private String externalLink = null;

    public MagnoliaRichTextField() {
        super();
    }
    
    public MagnoliaRichTextField(CKEditorConfig config) {
        super(config);
    }

    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        super.changeVariables(source, variables);
        if(variables.containsKey(VMagnoliaRichTextField.VAR_EXTERNAL_LINK)) {
            externalLink = "/aeinstein"; //obtain this from real source
            requestRepaint();
        }
    }
    
    
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        super.paintContent(target);
        if(externalLink != null) {
            target.addAttribute(VMagnoliaRichTextField.VAR_EXTERNAL_LINK, externalLink);
            externalLink = null;
        }
    }
}
