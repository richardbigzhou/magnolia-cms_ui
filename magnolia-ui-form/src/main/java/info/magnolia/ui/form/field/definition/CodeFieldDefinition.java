/**
 * This file Copyright (c) 2013-2016 Magnolia International
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
package info.magnolia.ui.form.field.definition;

import info.magnolia.ui.form.field.transformer.basic.NotNullInitialStringValueTransformer;

/**
 * Field definition for a rich code editor.
 */
public class CodeFieldDefinition extends ConfiguredFieldDefinition {

    private String language;
    private String fileNameProperty;

    private int height = 300;

    public CodeFieldDefinition() {
        setTransformerClass(NotNullInitialStringValueTransformer.class);
    }

    /**
     * Defines the programming language to use for syntax highlighting.
     * <p>
     * See the {@link org.vaadin.aceeditor.AceMode AceMode} enum for possible values, <code>"freemarker"</code> is also supported.
     *
     * @see org.vaadin.aceeditor.AceMode
     */
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Defines the Vaadin propertyId under which file name is exposed by the current form {@link com.vaadin.data.Item Item}.
     */
    public String getFileNameProperty() {
        return fileNameProperty;
    }

    public void setFileNameProperty(String fileNameProperty) {
        this.fileNameProperty = fileNameProperty;
    }

    /**
     * Defines the height of this code field, in pixels. Default value is <code>300</code>.
     */
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

}
