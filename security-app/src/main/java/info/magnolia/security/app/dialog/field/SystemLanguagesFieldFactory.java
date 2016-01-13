/**
 * This file Copyright (c) 2013-2015 Magnolia International
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
package info.magnolia.security.app.dialog.field;

import info.magnolia.cms.security.SilentSessionOp;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.util.SessionUtil;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.ui.form.field.definition.SelectFieldOptionDefinition;
import info.magnolia.ui.form.field.factory.SelectFieldFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Creates and initializes a select field with system languages, as configured under <code>CONFIG:/server/i18n/system/languages</code>.
 *
 * @param <D> definition type
 */
public class SystemLanguagesFieldFactory<D extends SystemLanguagesFieldDefinition> extends SelectFieldFactory<D> {

    static final String SYSTEM_LANGUAGES_PATH = "/server/i18n/system/languages";

    private static final Logger log = LoggerFactory.getLogger(SystemLanguagesFieldDefinition.class);

    private final Context context;

    private String selectedLanguage;

    @Inject
    public SystemLanguagesFieldFactory(D definition, Item relatedFieldItem, Context context) {
        super(definition, relatedFieldItem);
        this.context = context;
    }

    /**
     * Returns the languages defined under <code>CONFIG:/server/i18n/system/languages</code>.
     */
    @Override
    public List<SelectFieldOptionDefinition> getSelectFieldOptionDefinition() {
        List<SelectFieldOptionDefinition> options = new LinkedList<>();

        Node systemLanguages = MgnlContext.doInSystemContext(new SilentSessionOp<Node>(RepositoryConstants.CONFIG) {

            @Override
            public Node doExec(Session session) throws RepositoryException {
                return SessionUtil.getNode(session, SYSTEM_LANGUAGES_PATH);
            }
        });

        if (systemLanguages == null) {
            log.error("Cannot load system languages definition from [{}], check the system configuration.", SYSTEM_LANGUAGES_PATH);
        } else {
            try {
                Locale currentLocale = context.getLocale();
                for (Node language : NodeUtil.getNodes(systemLanguages)) {
                    if (language.hasProperty("enabled") && language.getProperty("enabled").getBoolean()) {
                        SelectFieldOptionDefinition option = new SelectFieldOptionDefinition();
                        option.setValue(language.getName());
                        // get either the language code from property, or first two characters from the node name
                        String langCode = language.hasProperty("language") ? language.getProperty("language").getString() : language.getName().substring(0, 2);
                        String countryCode = language.hasProperty("country") ? language.getProperty("country").getString() : "";
                        Locale locale = new Locale(langCode, countryCode);
                        String label = locale.getDisplayLanguage(currentLocale);
                        if (!"".equals(countryCode)) {
                            label += " (" + locale.getDisplayCountry(currentLocale) + ")";
                        }
                        option.setLabel(label);
                        if (currentLocale.equals(locale) || currentLocale.getLanguage().equals(locale.getLanguage())) {
                            option.setSelected(true);
                            selectedLanguage = option.getValue();
                        }
                        options.add(option);
                    }
                }
            } catch (RepositoryException e) {
                log.error("Cannot load system languages from [{}]: {}", SYSTEM_LANGUAGES_PATH, e.getMessage());
            }
        }
        return options;
    }

    @Override
    protected Object createDefaultValue(Property<?> dataSource) {
        return StringUtils.isNotBlank(selectedLanguage) ? selectedLanguage : super.createDefaultValue(dataSource);
    }
}
