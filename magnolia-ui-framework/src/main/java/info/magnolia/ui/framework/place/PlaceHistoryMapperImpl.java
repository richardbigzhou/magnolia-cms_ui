/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.ui.framework.place;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link PlaceHistoryMapper} that looks for the {@link Prefix} annotation and finds the tokenizers
 * for its places by searching for a nested class that implements {@link PlaceTokenizer}.
 *
 * @version $Id$
 */
public class PlaceHistoryMapperImpl extends AbstractPlaceHistoryMapper {

    private static final Logger log = LoggerFactory.getLogger(PlaceHistoryMapperImpl.class);

    /**
     * Maps a prefix to a tokenizer.
     */
    private Map<String, PlaceTokenizer<Place>> tokenizers = new HashMap<String, PlaceTokenizer<Place>>();

    public PlaceHistoryMapperImpl(Class<? extends Place>... places) {
        registerTokenizers(places);
    }

    @SuppressWarnings("unchecked")
    private void registerTokenizers(Class<? extends Place>... places) {
        log.debug("Starting registering tokenizers for places...");
        for (Class<? extends Place> clazz : places) {

            Prefix prefix = clazz.getAnnotation(Prefix.class);
            if (prefix == null) {
                log.warn("No @Prefix annotation found for place {}. Skipping it...", clazz.getName());
                continue;
            }

            PlaceTokenizer<Place> tokenizer = findTokenizer(clazz);
            if (tokenizer == null) {
                log.warn("An @Prefix annotation was detected for {} but a PlaceTokenizer implementation was not found.", clazz.getName());
                continue;
            }

            tokenizers.put(prefix.value(), tokenizer);
            log.debug("Added tokenizer for place {}", clazz.getName());
        }
    }

    private PlaceTokenizer<Place> findTokenizer(Class<? extends Place> clazz) {
        for (Class<?> declaredClass : clazz.getDeclaredClasses()) {
            if (PlaceTokenizer.class.isAssignableFrom(declaredClass)) {
                try {
                    return (PlaceTokenizer<Place>) declaredClass.newInstance();
                } catch (InstantiationException e) {
                    throw new IllegalStateException(e);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return null;
    }

    /**
     * Returns the prefix value for this place if a <code>Prefix</code> annotation exists, else <code>null</code>.
     */
    private String getPrefixValue(Place place) {
        Prefix prefix = place.getClass().getAnnotation(Prefix.class);
        if (prefix != null) {
            return prefix.value();
        }
        return null;
    }

    @Override
    protected PrefixAndToken getPrefixAndToken(Place newPlace) {
        final String prefix = getPrefixValue(newPlace);
        if (prefix == null) {
            return null;
        }
        PlaceTokenizer<Place> tokenizer = tokenizers.get(prefix);
        if (tokenizer == null) {
            return null;
        }
        String token = tokenizer.getToken(newPlace);
        return new PrefixAndToken(prefix, token);
    }

    @Override
    protected PlaceTokenizer<?> getTokenizer(String prefix) {
        return tokenizers.get(prefix);
    }
}
