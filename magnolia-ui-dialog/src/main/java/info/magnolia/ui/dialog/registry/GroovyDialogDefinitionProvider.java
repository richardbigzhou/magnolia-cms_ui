/**
 * This file Copyright (c) 2014 Magnolia International
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
package info.magnolia.ui.dialog.registry;

import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.registry.RegistrationException;
import info.magnolia.ui.dialog.config.DialogBuilder;
import info.magnolia.ui.dialog.definition.FormDialogDefinition;
import info.magnolia.ui.dialog.formdialog.FormDialogPresenter;
import info.magnolia.ui.form.config.FieldConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

/**
 * Provides a dialog definition out of a groovy script.
 */
public class GroovyDialogDefinitionProvider implements DialogDefinitionProvider{

    public static final String GROOVY_DIALOG_PATTERN = ".*/modules/(.*)/dialogs/(.*).groovy";

    private static Logger log = LoggerFactory.getLogger(GroovyDialogDefinitionProvider.class);

    public static final String DIALOG_VAR = "dialog";

    public static final String FIELD_VAR = "field";

    private Map<String, Object> configProviders;

    private String scriptPath;

    private String dialogId;

    public GroovyDialogDefinitionProvider(String scriptPath, Map<String, Object> builderProviders) {
        this.scriptPath = scriptPath;
        this.configProviders = builderProviders;
    }

    @Override
    public String getId() {
        if (dialogId == null) {
            Matcher matcher = Pattern.compile(GROOVY_DIALOG_PATTERN).matcher(scriptPath);
            if (matcher.matches()) {
                dialogId = String.format("%s:%s", matcher.group(1), matcher.group(2));
            }
        }
        return dialogId;
    }

    @Override
    public FormDialogDefinition getDialogDefinition() throws RegistrationException {
        InputStream is;
        try {
            is = ClasspathResourcesUtil.getStream(scriptPath, false);
        } catch (IOException e) {
            log.error("Failed to load groovy script {}", scriptPath, e);
            is = null;
        }

        if (is != null) {

            Binding binding = initBinding();
            final DialogBuilder dialogBuilder = new DialogBuilder(getId());
            binding.setVariable(DIALOG_VAR, dialogBuilder);
            binding.setVariable(FIELD_VAR, new FieldConfig());


            final InputStreamReader reader = new InputStreamReader(encloseWithVariables(is));

            GroovyShell shell = new GroovyShell(binding);
            shell.evaluate(reader);

            return dialogBuilder.definition();
        }

        return null;
    }

    private SequenceInputStream encloseWithVariables(InputStream is) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(FIELD_VAR).append(".with{").append("\n");
        StringBuilder endingParentheses = new StringBuilder("\n").append("}");
        return new SequenceInputStream(new SequenceInputStream(
                IOUtils.toInputStream(stringBuilder.toString()), is),
                    IOUtils.toInputStream(endingParentheses.toString()));
    }

    private Binding initBinding() {
        Binding binding = new Binding();
        final Iterator<Map.Entry<String,Object>> it = configProviders.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<String,Object> entry = it.next();
            binding.setVariable(entry.getKey(), entry.getValue());
        }
        return binding;
    }

    @Override
    public Class<? extends FormDialogPresenter> getPresenterClass() throws RegistrationException {
        return getDialogDefinition().getPresenterClass();
    }
}
