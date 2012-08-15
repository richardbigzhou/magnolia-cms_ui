/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.ui.framework.instantpreview;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimaps;

/**
 * Implementation of {@link InstantPreviewLocationManager}.
 */
@Singleton
public class InstantPreviewLocationManagerImpl implements InstantPreviewLocationManager {

    private final Random idGenerator;

    private List<String> hosts = Collections.synchronizedList(Lists.<String>newArrayList());

    private ListMultimap<String, PreviewLocationListener> listeners = Multimaps.synchronizedListMultimap(ArrayListMultimap.<String, PreviewLocationListener>create());

    private static final int MIN_HOST_ID = 0;

    private static final int MAX_HOST_ID = 999999999;

    @Inject
    public InstantPreviewLocationManagerImpl() {
        idGenerator = new Random(System.currentTimeMillis());
    }

    @Override
    public String registerInstantPreviewHost() {
        String id = generateNineDigitsRandomNumberAsString(MIN_HOST_ID, MAX_HOST_ID);
        while(hosts.contains(id)) {
            id = generateNineDigitsRandomNumberAsString(MIN_HOST_ID, MAX_HOST_ID);
        }
        hosts.add(id);
        return id;
    }

    @Override
    public void unregisterInstantPreviewHost(String hostId) throws InstantPreviewHostNotFoundException {
        if (!hosts.contains(hostId)) {
            throw new InstantPreviewHostNotFoundException("Host with id " + hostId + " does not exist. It is possible that you specified an invalid id or that the host has stopped sharing.");
        }
        hosts.remove(hostId);
        listeners.removeAll(hostId);
    }

    @Override
    public void subscribeTo(String hostId, PreviewLocationListener listener) throws InstantPreviewHostNotFoundException {
        if (!hosts.contains(hostId)) {
            throw new InstantPreviewHostNotFoundException("Host with id " + hostId + " does not exist. It is possible that you specified an invalid id or that the host has stopped sharing.");
        }
        listeners.put(hostId, listener);
    }

    @Override
    public void unsubscribeFrom(String hostId, PreviewLocationListener listener) throws InstantPreviewHostNotFoundException {
        if (!hosts.contains(hostId)) {
            throw new InstantPreviewHostNotFoundException("Host with id " + hostId + " does not exist. It is possible that you specified an invalid id or that the host has stopped sharing.");
        }
        listeners.remove(hostId, listener);
    }

    @Override
    public void sendPreviewToken(String hostId, String token) {
        for (final PreviewLocationListener listener : listeners.get(hostId)) {
            listener.onPreviewLocationReceived(token);
        }
    }

    /**
     * @return an unmodifiable list with the host ids. Exposed here mainly for testing purposes.
     */
    protected final List<String> getHosts() {
        return Collections.unmodifiableList(hosts);
    }

    /**
     * @return an unmodifiable multimap list with the listeners bound to each host. Exposed here mainly for testing purposes.
     */
    protected final ListMultimap<String, PreviewLocationListener> getListeners() {
        return Multimaps.unmodifiableListMultimap(listeners);
    }
    /**
     * @return a String representation of a random number in the range [{@value #MIN_HOST_ID}, {@value #MAX_HOST_ID}].
     * If the number is less than 9 digits, the returned string is left-padded with zeroes. Exposed here mainly for testing purposes.
     */
    protected final String generateNineDigitsRandomNumberAsString(int min, int max) {
        if((min < MIN_HOST_ID || max > MAX_HOST_ID) || (min > max)) {
            throw new IllegalArgumentException("Got invalid arguments: min = " + min + " and max = " + max);
        }
        int random = Math.abs(idGenerator.nextInt(max - min + 1) + min);
        return String.format("%09d", random);
    }
}
