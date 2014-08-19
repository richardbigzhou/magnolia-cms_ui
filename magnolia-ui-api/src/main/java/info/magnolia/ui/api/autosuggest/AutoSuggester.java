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
package info.magnolia.ui.api.autosuggest;

import java.util.Collection;

/**
 * Returns suggestions and how to display them given a field.
 */
public interface AutoSuggester {

    /**
     * Get suggestions and how to display them given a tree field.
     * 
     * @param itemId identifies the row of the tree where the field resides
     * @param propertyId identifies the column of the tree where the field resides
     * @return the {@link AutoSuggesterResult} encapsulating suggestions for the field and how to display them
     */
    AutoSuggesterResult getSuggestionsFor(Object itemId, Object propertyId);

    /**
     * Suggestions and how to display them.
     */
    interface AutoSuggesterResult {
        /**
         * How to match suggestions to the current field value.
         */
        public static final int STARTS_WITH = 0;
        public static final int CONTAINS = 1;

        /**
         * Whether suggestions could be calculated for the field.
         */
        boolean suggestionsAvailable();

        /**
         * Get suggestions for the field.
         */
        Collection<String> getSuggestions();

        /**
         * Get method to match suggestions to the current field value.
         */
        int getMatchMethod();

        /**
         * Whether to show suggestions that cannot match the current field value.
         */
        boolean showMismatchedSuggestions();

        /**
         * Whether to highlight the field as a possible error if the current field value does not match any of the suggestions.
         */
        boolean showErrorHighlighting();
    }
}
