/**
 * This file Copyright (c) 2010-2016 Magnolia International
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

/**
 * Field definition for a rich-text field.
 */
public class RichTextFieldDefinition extends ConfiguredFieldDefinition {

    private boolean alignment;
    private boolean images;
    private boolean lists = true;
    private boolean source;
    private boolean tables;

    private long height;

    private String colors;
    private String fonts;
    private String fontSizes;

    private String configJsFile;

    /**
     * Defines whether text alignment (left, center, right, justify) is allowed in this rich-text field.
     *
     * @return <code>true</code> if alignment is enabled, <code>false</code> otherwise. Defaults to <code>false</code>.
     */
    public boolean isAlignment() {
        return alignment;
    }

    public void setAlignment(boolean alignment) {
        this.alignment = alignment;
    }

    /**
     * Defines whether images are allowed in this rich-text field.
     *
     * @return <code>true</code> if images are enabled, <code>false</code> otherwise. Defaults to <code>false</code>.
     */
    public boolean isImages() {
        return images;
    }

    public void setImages(boolean images) {
        this.images = images;
    }

    /**
     * Defines whether lists are allowed in this rich-text field.
     *
     * @return <code>true</code> if lists are enabled, <code>false</code> otherwise. Defaults to <code>true</code>.
     */
    public boolean isLists() {
        return lists;
    }

    public void setLists(boolean lists) {
        this.lists = lists;
    }

    /**
     * Defines whether source mode is allowed in this rich-text field.
     *
     * @return <code>true</code> if source mode is enabled, <code>false</code> otherwise. Defaults to <code>false</code>.
     */
    public boolean isSource() {
        return source;
    }

    public void setSource(boolean source) {
        this.source = source;
    }

    /**
     * Defines whether tables are allowed in this rich-text field.
     *
     * @return <code>true</code> if tables are enabled, <code>false</code> otherwise. Defaults to <code>true</code>.
     */
    public boolean isTables() {
        return tables;
    }

    public void setTables(boolean tables) {
        this.tables = tables;
    }

    /**
     * Defines the height of this rich-text field, in pixels.
     */
    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    /**
     * Defines the text colors that are allowed in this rich-text field.
     *
     * @return a comma separated list of colors, as per CKEditor documentation (hexadecimal codes, without leading hash).
     * @see <a href="http://docs.ckeditor.com/#!/api/CKEDITOR.config-cfg-colorButton_colors">http://docs.ckeditor.com/#!/api/CKEDITOR.config-cfg-colorButton_colors</a>
     */
    public String getColors() {
        return colors;
    }

    public void setColors(String colors) {
        this.colors = colors;
    }

    /**
     * Defines the font families that are allowed in this rich-text field.
     *
     * @return a semi-colon separated list of font names, as per CKEditor documentation.
     * @see <a href="http://docs.ckeditor.com/#!/api/CKEDITOR.config-cfg-font_names">http://docs.ckeditor.com/#!/api/CKEDITOR.config-cfg-font_names</a>
     */
    public String getFonts() {
        return fonts;
    }

    public void setFonts(String fonts) {
        this.fonts = fonts;
    }

    /**
     * Defines the font sizes that are allowed in this rich-text field.
     *
     * @return a semi-colon separated list of font sizes, as per CKEditor documentation (including CSS unit).
     * @see <a href="http://docs.ckeditor.com/#!/api/CKEDITOR.config-cfg-fontSize_sizes">http://docs.ckeditor.com/#!/api/CKEDITOR.config-cfg-fontSize_sizes</a>
     */
    public String getFontSizes() {
        return fontSizes;
    }

    public void setFontSizes(String fontSizes) {
        this.fontSizes = fontSizes;
    }

    /**
     * Defines the custom configuration file for this rich-text field.
     *
     * @return a path to the config JavaScript file, relative to context path.
     */
    public String getConfigJsFile() {
        return configJsFile;
    }

    public void setConfigJsFile(String configJsFile) {
        this.configJsFile = configJsFile;
    }
}
