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
package info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.connector;

import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.rpc.ThumbnailLayoutClientRpc;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.rpc.ThumbnailLayoutServerRpc;
import info.magnolia.ui.vaadin.gwt.shared.Range;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.shared.ThumbnailData;
import info.magnolia.ui.vaadin.gwt.client.layout.thumbnaillayout.widget.EscalatorThumbnailsPanel;
import info.magnolia.ui.vaadin.layout.LazyThumbnailLayout;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Timer;
import com.vaadin.client.communication.RpcProxy;
import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.client.ui.layout.ElementResizeEvent;
import com.vaadin.client.ui.layout.ElementResizeListener;
import com.vaadin.shared.ui.Connect;

/**
 * Connector for {@link LazyThumbnailLayout}.
 */
@Connect(LazyThumbnailLayout.class)
public class LazyThumbnailLayoutConnector extends AbstractComponentConnector implements EscalatorThumbnailsPanel.Listener {

    private static final Logger log = Logger.getLogger(LazyThumbnailLayoutConnector.class.getSimpleName());

    private static final int THUMBNAIL_QUERY_RPC_DELAY = 200;

    private Range cachedThumbnails = Range.between(0, 0);

    private Map<Object, Integer> idToIndex = new HashMap<>();

    private Map<Integer, ThumbnailData> indexToThumbnail = new HashMap<>();

    private Map<String, String> idToUrl = new HashMap<>();

    private final ThumbnailLayoutServerRpc rpc = RpcProxy.create(ThumbnailLayoutServerRpc.class, this);

    private boolean widgetInitialized = false;

    private boolean waitingData = false;

    @Override
    public void onThumbnailClicked(int index, boolean isMetaKeyPressed, boolean isShiftKeyPressed) {
        rpc.onThumbnailSelected(index, isMetaKeyPressed, isShiftKeyPressed);
    }

    @Override
    public void onThumbnailRightClicked(int index, int xPos, int yPos) {
        rpc.onThumbnailRightClicked(index, xPos, yPos);
    }

    @Override
    public void onThumbnailDoubleClicked(int index) {
        rpc.onThumbnailDoubleClicked(index);
    }


    @Override
    protected void init() {
        super.init();
        registerRpc(ThumbnailLayoutClientRpc.class, new ThumbnailLayoutClientRpc() {
            @Override
            public void addThumbnails(List<ThumbnailData> data, int startingFrom) {
                final Range received = Range.withLength(startingFrom, data.size());
                final Range maxCacheRange = getMaxCacheRange();
                final Range[] partition = received.partitionWith(maxCacheRange);
                final Range newUsefulData = partition[1];
                if (!newUsefulData.isEmpty()) {
                    // Update the parts that are actually inside
                    for (int i = newUsefulData.getStart(); i < newUsefulData.getEnd(); i++) {
                        final ThumbnailData thumbnailData = data.get(i - startingFrom);
                        if (thumbnailData.isRealResource()) {
                            idToUrl.put(thumbnailData.getThumbnailId(), getResourceUrl(thumbnailData.getThumbnailId()));
                        }

                        indexToThumbnail.put(i, thumbnailData);
                        idToIndex.put(thumbnailData.getThumbnailId(), i);
                    }

                    final Range toPushToWidget = newUsefulData.restrictTo(getWidget().getDisplayedRange());
                    for (int i = toPushToWidget.getStart(); i < toPushToWidget.getEnd(); ++i) {
                        updateThumbnailContentAtIndex(i);
                    }

                    // Potentially extend the range
                    if (cachedThumbnails.isEmpty()) {
                        cachedThumbnails = newUsefulData;
                    } else {
                        purgeCache();
                        if (!cachedThumbnails.isEmpty()) {
                            cachedThumbnails = cachedThumbnails.combineWith(newUsefulData);
                        } else {
                            cachedThumbnails = newUsefulData;
                        }
                    }
                }

                waitingData = false;

                // Eventually check whether all needed rows are now available
                serveThumbnails();
            }

            @Override
            public void refresh() {
                refreshViewport();
            }
        });

        getLayoutManager().addElementResizeListener(getWidget().getElement(), new ElementResizeListener() {
            @Override
            public void onElementResize(ElementResizeEvent e) {
                getWidget().resize();
            }
        });
    }

    @Override
    public void onStateChanged(StateChangeEvent stateChangeEvent) {
        super.onStateChanged(stateChangeEvent);

        if (widgetInitialized && stateChangeEvent.hasPropertyChanged("size")) {
            getWidget().initialize(getState().thumbnailAmount, getState().offset, getState().size, getState().scaleRatio, getState().isFirstUpdate);
        }

        if (widgetInitialized && stateChangeEvent.hasPropertyChanged("selection")) {
            updateSelection();
        }

        if (!widgetInitialized) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    widgetInitialized = true;
                    getWidget().initialize(getState().thumbnailAmount, getState().offset, getState().size, getState().scaleRatio, getState().isFirstUpdate);
                    updateSelection();
                }
            });
        }
    }

    private void refreshViewport() {
        // Clear caches and maps
        this.cachedThumbnails = Range.between(0, 0);
        this.idToUrl.clear();
        this.idToIndex.clear();
        this.indexToThumbnail.clear();

        // Reset thumbnail amount
        getWidget().setThumbnailAmount(getState().thumbnailAmount);

        // Previous call would trigger lazy update of the viewport, but we don't need it because we anyway
        // are going to query the whole visible range => cancel the timer
        timer.cancel();
        queryThumbnails(getWidget().getDisplayedRange());
    }

    public EscalatorThumbnailsPanel getWidget() {
        return (EscalatorThumbnailsPanel) super.getWidget();
    }

    @Override
    public ThumbnailLayoutState getState() {
        return (ThumbnailLayoutState) super.getState();
    }

    private void serveThumbnails() {
        rpc.updateOffset(getWidget().getCurrentThumbnailOffset());

        if (waitingData) {
            return;
        }

        final Range newMinimumCachedRange = getMinCacheRange();
        if (!newMinimumCachedRange.intersects(cachedThumbnails) || cachedThumbnails.isEmpty()) {
            indexToThumbnail.clear();
            idToIndex.clear();
            idToUrl.clear();

            cachedThumbnails = Range.between(0, 0);

            queryThumbnails(getMaxCacheRange());
            log.log(Level.FINEST, "Querying: " + getMaxCacheRange());
        } else {
            final Range intersection = newMinimumCachedRange.restrictTo(cachedThumbnails).restrictTo(getWidget().getDisplayedRange());
            for (int i = intersection.getStart(); i < intersection.getEnd(); ++i) {
                updateThumbnailContentAtIndex(i);
            }

            purgeCache();

            if (!newMinimumCachedRange.isSubsetOf(cachedThumbnails)) {
                final Range[] missingCachePartition = getMaxCacheRange().partitionWith(cachedThumbnails);
                queryThumbnails(missingCachePartition[0]);
                queryThumbnails(missingCachePartition[2]);
                log.log(Level.FINEST, "Querying: " + missingCachePartition[0] + " and " + missingCachePartition[2]);
            }
        }
    }

    private void purgeCache() {
        final Range[] cachePartition = cachedThumbnails.partitionWith(getMaxCacheRange());
        dropFromCache(cachePartition[0]);
        cachedThumbnails = cachePartition[1];
        dropFromCache(cachePartition[2]);
    }

    private void dropFromCache(Range range) {
        for (int i = range.getStart(); i < range.getEnd(); i++) {
            final ThumbnailData removed = indexToThumbnail.remove(i);

            String thumbnailId = removed.getThumbnailId();
            idToIndex.remove(thumbnailId);
            idToUrl.remove(thumbnailId);
        }
    }

    private void queryThumbnails(Range range) {
        if (!range.isEmpty()) {
            rpc.loadThumbnails(range.getStart(), range.length(), cachedThumbnails.getStart(), cachedThumbnails.getEnd());
            waitingData = true;
        }
    }

    /**
     * @return how many thumbnails should be cached from both sides before we will have to ask server to
     * load more (half of the page).
     */
    private Range getMinCacheRange() {
        final Range displayedRange = getWidget().getDisplayedRange();
        int cachePageSize = displayedRange.length();
        return displayedRange.expand(cachePageSize / 2, cachePageSize / 2).restrictTo(getAvailableRange());
    }

    /**
     * @return how many thumbnails can be cached on either of sides before we start evicting them from the
     * cache (full page by). Also this is the amount of thumbnails to be queried from teh server.
     */
    private Range getMaxCacheRange() {
        final Range displayedRange = getWidget().getDisplayedRange();
        int cachePageSize = displayedRange.length();
        return displayedRange.expand(cachePageSize, cachePageSize).restrictTo(getAvailableRange());
    }

    /**
     * @return
     */
    private Range getAvailableRange() {
        return Range.between(0, getState().thumbnailAmount);
    }

    private Timer timer = new Timer() {
        @Override
        public void run() {
            serveThumbnails();
        }
    };

    @Override
    protected EscalatorThumbnailsPanel createWidget() {
        final EscalatorThumbnailsPanel layout = new EscalatorThumbnailsPanel(this);
        layout.setThumbnailService(new ThumbnailService() {
            @Override
            public void onViewportChanged(final Range requestedRange) {
                timer.schedule(THUMBNAIL_QUERY_RPC_DELAY);
                updateSelection();
            }

            @Override
            public void onThumbnailsScaled(float ratio) {
                rpc.setScaleRatio(ratio);
            }
        });
        return layout;
    }

    private void updateSelection() {
        final ThumbnailLayoutState.SelectionModel selection = getState().selection;

        final Range displayedRange = getWidget().getDisplayedRange();
        final Range selectionBoundaries = selection.getSelectionBoundaries();
        final List<Integer> indices = new LinkedList<>();

        if (selectionBoundaries.intersects(displayedRange)) {
            for (int selectedIndex : selection.selectedIndices) {
                if (displayedRange.contains(selectedIndex)) {
                    indices.add(selectedIndex);
                }
            }
        }
        getWidget().setSelectedThumbnailsViaIndices(indices);
    }

    private void updateThumbnailContentAtIndex(int thumbnailAbsoluteIndex) {
        final ThumbnailData thumbnailData = indexToThumbnail.get(thumbnailAbsoluteIndex);
        if (thumbnailData.isRealResource()) {
            getWidget().updateImageSource(idToUrl.get(thumbnailData.getThumbnailId()), thumbnailAbsoluteIndex);
        } else {
            getWidget().updateIconFontStyle("icon-" + thumbnailData.getIconFontId(), thumbnailAbsoluteIndex);
        }
    }

    /**
     * Serves thumbnails.
     */
    public interface ThumbnailService {

        void onViewportChanged(Range requestedRange);

        void onThumbnailsScaled(float ratio);
    }
}
