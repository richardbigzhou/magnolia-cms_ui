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
package info.magnolia.ui.workbench.autosuggest;

import info.magnolia.ui.api.autosuggest.AutoSuggester;

import java.util.Collection;

/**
 * Adapter for {@link AutoSuggester}.
 */
public abstract class AutoSuggesterAdapter implements AutoSuggester {
    protected AutoSuggesterResult noSuggestionsAvailable() {
        return new AutoSuggesterResultAdapter();
    }

    /**
     * Convenient adapter for AutoSuggesterResult.
     */
    protected static class AutoSuggesterResultAdapter implements AutoSuggesterResult {
        private boolean suggestionsAvailable;
        private Collection<String> suggestions;
        private MatchMethod matchMethod;
        boolean showMismatchedSuggestions;
        boolean showErrorHighlighting;

        public AutoSuggesterResultAdapter(boolean suggestionsAvailable, Collection<String> suggestions, MatchMethod matchMethod, boolean showMismatchedSuggestions, boolean showErrorHighlighting) {
            this.suggestionsAvailable = suggestionsAvailable;
            this.suggestions = suggestions;
            this.matchMethod = matchMethod;
            this.showMismatchedSuggestions = showMismatchedSuggestions;
            this.showErrorHighlighting = showErrorHighlighting;
        }

        /**
         * The result constructed by default has no suggestions available.
         */
        public AutoSuggesterResultAdapter() {
            this(false, null, MatchMethod.STARTS_WITH, false, false);
        }

        @Override
        public boolean suggestionsAvailable() {
            if (!suggestionsAvailable) {
                return false;
            }
            else if (suggestions == null || suggestions.isEmpty()) {
                return false;
            }
            else {
                return true;
            }
        }

        @Override
        public Collection<String> getSuggestions() {
            return suggestions;
        }

        @Override
        public MatchMethod getMatchMethod() {
            return matchMethod;
        }

        @Override
        public boolean showMismatchedSuggestions() {
            return showMismatchedSuggestions;
        }

        @Override
        public boolean showErrorHighlighting() {
            return showErrorHighlighting;
        }
    }
}
