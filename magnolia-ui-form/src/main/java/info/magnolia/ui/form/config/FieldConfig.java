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
package info.magnolia.ui.form.config;

import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.form.field.converter.BaseIdentifierToPathConverter;

/**
 * Config object creating builders for form fields.
 */
public class FieldConfig {

    public DateFieldBuilder date(String name) {
        return new DateFieldBuilder(name);
    }

    public BasicUploadFieldBuilder basicUpload(String name) {
        return new BasicUploadFieldBuilder(name).binaryNodeName(name);
    }

    public TextFieldBuilder text(String name) {
        return new TextFieldBuilder(name);
    }

    public LinkFieldBuilder link(String name) {
        return new LinkFieldBuilder(name);
    }

    public LinkFieldBuilder pageLink(String name) {
        return new LinkFieldBuilder(name).appName("pages").targetWorkspace(RepositoryConstants.WEBSITE).identifierToPathConverter(new BaseIdentifierToPathConverter());
    }

    public SelectFieldBuilder select(String name) {
        return new SelectFieldBuilder(name);
    }

    public HiddenFieldBuilder hidden(String name) {
        return new HiddenFieldBuilder(name);
    }

    public CheckboxFieldBuilder checkbox(String name) {
        return new CheckboxFieldBuilder(name);
    }

    public OptionGroupFieldBuilder optionGroup(String name) {
        return new OptionGroupFieldBuilder(name);
    }

    public PasswordFieldBuilder password(String name) {
        return new PasswordFieldBuilder(name);
    }

    public StaticFieldBuilder staticField(String name) {
        return new StaticFieldBuilder(name);
    }

    public RichTextFieldBuilder richText(String name) {
        return new RichTextFieldBuilder(name);
    }

    public TwinColSelectFieldBuilder twinColSelect(String name) {
        return new TwinColSelectFieldBuilder(name);
    }

    public MultiValueFieldBuilder multi(String name) {
        return new MultiValueFieldBuilder(name);
    }

    public CodeFieldBuilder code(String name) {
        return new CodeFieldBuilder(name);
    }

    /**
     * @deprecated Since 5.4, use {@link #code(String)} instead.
     */
    @Deprecated
    public BasicTextCodeFieldBuilder basicTextCode(String name) {
        return new BasicTextCodeFieldBuilder(name);
    }

    public SwitchableFieldBuilder switchable(String name) {
        return new SwitchableFieldBuilder(name);
    }
}
