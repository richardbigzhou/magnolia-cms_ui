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
package info.magnolia.ui.mediaeditor.data;


import info.magnolia.i18nsystem.SimpleTranslator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.TransactionalPropertyWrapper;

/**
 * Property implementation that uses temporary files for storing the intermediate results of data modification.
 * Tracking is done in both direction - steps can undone and redone.
 * Original data is stored in memory and the value can be rolled back to it.
 */
public class EditHistoryTrackingPropertyImpl extends TransactionalPropertyWrapper<byte[]> implements EditHistoryTrackingProperty {

    private static final int DEFAULT_DEPTH = 5;

    private static final String TEMP_FILE_PREFIX = "MEDIA_EDITOR_";

    private Logger log = Logger.getLogger(getClass());

    private TempFileStack doneActions = new TempFileStack(DEFAULT_DEPTH);

    private TempFileStack unDoneActions = new TempFileStack(DEFAULT_DEPTH);

    private boolean currentActionInitialized;

    private Listener listener;

    private final SimpleTranslator i18n;

    public EditHistoryTrackingPropertyImpl(byte[] bytes, SimpleTranslator i18n) {
        super(new ObjectProperty<byte[]>(bytes));

        this.i18n = i18n;
        startTransaction();
        startAction("");
        setValue(bytes);
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public String getLastDoneActionName() {
        return doneActions.size() < 2 ? null : doneActions.peek().actionName;
    }

    @Override
    public String getLastUnDoneActionName() {
        return unDoneActions.isEmpty() ? null : unDoneActions.peek().actionName;
    }

    @Override
    public void purgeHistory() {
        unDoneActions.clear();
        doneActions.clear();
    }

    @Override
    public void setCapacity(int depth) {
        doneActions.setCapacity(depth);
        unDoneActions.setCapacity(depth);
    }

    @Override
    public void startAction(String actionName) {
        if (!currentActionInitialized && doneActions.size() > 1) {
            doneActions.pop();
        }
        Record record = new Record();
        record.actionName = actionName;
        try {
            record.file = File.createTempFile(TEMP_FILE_PREFIX + actionName, null, null);
            record.file.deleteOnExit();
            doneActions.push(record);
            currentActionInitialized = false;
        } catch (IOException e) {
            logErrorAndNotify(i18n.translate("ui-mediaeditor.editHistoryTrackingProperty.tmpFileCreationFailure.message"), e);
        }
    }

    @Override
    public void undo() {
        if (doneActions.size() > 1) {
            Record lastDone = doneActions.peek();
            unDoneActions.push(lastDone);
            doneActions.remove(lastDone);
            updateValue(doneActions.peek());
        }
    }

    @Override
    public void redo() {
        if (!unDoneActions.isEmpty()) {
            final Record toBeRedone = unDoneActions.peek();
            updateValue(toBeRedone);
            doneActions.push(toBeRedone);
            unDoneActions.remove(toBeRedone);
        }
    }

    @Override
    public void revert() {
        rollback();
        purgeHistory();
    }

    @Override
    public void commit() {
        super.commit();
        purgeHistory();
    }

    @Override
    public void setValue(byte[] bytes) throws ReadOnlyException {
        if (!unDoneActions.isEmpty() &&
            !unDoneActions.peek().actionName.equals(doneActions.peek().actionName)) {
            unDoneActions.clear();
        }
        currentActionInitialized = true;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(doneActions.peek().file);
            IOUtils.write(bytes, fos);
            super.setValue(bytes);
        } catch (IOException e) {
            logErrorAndNotify(i18n.translate("ui-mediaeditor.editHistoryTrackingProperty.ioException.message"), e);
        } finally {
            IOUtils.closeQuietly(fos);
        }
    }

    private void logErrorAndNotify(String message, Exception e) {
        log.error(e);
        if (listener != null) {
            listener.errorOccurred(message, e);
        }
    }

    private void updateValue(Record newLastDone) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(newLastDone.file);
            super.setValue(IOUtils.toByteArray(fis));
        } catch (IOException e) {
            logErrorAndNotify(i18n.translate("ui-mediaeditor.editHistoryTrackingProperty.ioException.message"), e);
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }

    /**
     * Helper class to store the action data in a file and
     * an action name which can be used in UI.
     */
    private static class Record {

        public File file;

        public String actionName;
    }

    /**
     * Simple and limited implementation of a stack of file records.
     *
     */
    private static class TempFileStack extends LinkedList<Record> {

        private int capacity;

        public TempFileStack(int capacity) {
            this.capacity = capacity;
        }

        @Override
        public void push(Record record) {
            while (size() > capacity) {
                removeLast();
            }
            super.push(record);
        }

        @Override
        public Record pop() {
            Record result = super.pop();
            if (result != null) {
                result.file.delete();
            }
            return result;
        }

        @Override
        public void clear() {
            while (!isEmpty()) {
                pop();
            }
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }
    }
}
