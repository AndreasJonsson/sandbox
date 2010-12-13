/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.store.filesystem.internal;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.WeakHashMap;

import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.XWikiContext;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.phase.Initializable;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.DocumentReference;

/**
 * Default tools for getting files to store data in the filesystem.
 * This should be replaced by a module which provides a secure extension of java.io.File.
 *
 * @version $Id$
 * @since TODO
 */
@Component
public class DefaultFilesystemStoreTools implements FilesystemStoreTools, Initializable
{
    /** Serializer used for obtaining a safe file path from a document reference. */
    @Requirement("path")
    private EntityReferenceSerializer<String> pathSerializer;

    /**
     * We need to get the XWiki object in order to get the work directory.
     */
    @Requirement
    private Execution exec;

    /** This is the directory where all of the attachments will stored. */
    private File storageDir;

    /** A map which holds locks by the file path so that the same lock is used for the same file. */
    private final Map<String, WeakReference<ReadWriteLock>> fileLockMap =
        new WeakHashMap<String, WeakReference<ReadWriteLock>>();

    /** {@inheritDoc} */
    public void initialize()
    {
        final XWikiContext context = ((XWikiContext) this.exec.getContext().getProperty("xwikicontext"));
        final File workDir = context.getWiki().getWorkDirectory(context);
        this.storageDir = new File(workDir, STORAGE_DIR_NAME);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.store.filesystem.internal.FilesystemStoreTools#getBackupFile(File)
     */
    public File getBackupFile(final File storageFile)
    {
        return new File(storageFile.getAbsolutePath() + BACKUP_FILE_SUFFIX);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.store.filesystem.internal.FilesystemStoreTools#getTempFile(File)
     */
    public File getTempFile(final File storageFile)
    {
        return new File(storageFile.getAbsolutePath() + TEMP_FILE_SUFFIX);
    }

    /**
     * {@inheritDoc}
     * This implementation knows nothing about symlinks and will stack overflow with cyclical symlinks.
     *
     * @see org.xwiki.store.filesystem.internal.FilesystemStoreTools#allChildrenOf(File)
     */
    public List<File> allChildrenOf(final File parent)
    {
        final List<File> out = new ArrayList<File>();
        final File[] children = parent.listFiles();
        for (int i = 0; i < children.length; i++) {
            out.add(children[i]);
            if (children[i].isDirectory()) {
                out.addAll(allChildrenOf(children[i]));
            }
        }
        return out;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.store.filesystem.internal.FilesystemStoreTools#deleteDir(File)
     */
    public boolean deleteDir(final File directory)
    {
        final File[] children = directory.listFiles();
        for (int i = 0; i < children.length; i++) {
            if (children[i].isDirectory()) {
                this.deleteDir(children[i]);
            } else {
                children[i].delete();
            }
        }
        // Now move up the chain until a non-empty directory is found.
        File parent = directory;
        File dir;
        while (parent.listFiles().length == 0) {
            dir = parent;
            parent = dir.getParentFile();
            dir.delete();
        }

        return directory.exists();
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.store.filesystem.internal.FilesystemStoreTools#fileForAttachment(XWikiAttachment)
     */
    public File fileForAttachment(final XWikiAttachment attachment)
    {
        final XWikiDocument doc = attachment.getDoc();
        if (doc == null) {
            throw new NullPointerException("Could not store attachment because it is not "
                                           + "associated with a document.");
        }

        final String encodedName;
        try {
            encodedName = URLEncoder.encode(attachment.getFilename(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 not available, this Java VM is not standards compliant!");
        }

        final File attachmentsDir =
            getDocumentDir(doc.getDocumentReference(), this.storageDir, this.pathSerializer);

        final File attachmentDir = new File(attachmentsDir, encodedName);
        return new File(attachmentDir, encodedName);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.store.filesystem.internal.FilesystemStoreTools#getLockForFile(File)
     */
    public synchronized ReadWriteLock getLockForFile(final File toLock)
    {
        final String path = toLock.getAbsolutePath();
        WeakReference<ReadWriteLock> lock = this.fileLockMap.get(path);
        ReadWriteLock strongLock = null;
        if (lock != null) {
            strongLock = lock.get();
        }
        if (strongLock == null) {
            strongLock = new ReentrantReadWriteLock() {
                /**
                 * A strong reference on the string to make sure that the
                 * mere existence of the lock will keep it in the map.
                 */
                private final String lockMapReference = path;
            };
            this.fileLockMap.put(path, new WeakReference<ReadWriteLock>(strongLock));
        }
        return strongLock;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.xwiki.store.filesystem.internal.FilesystemStoreTools#getLockForFiles(List)
     */
    public synchronized ReadWriteLock getLockForFiles(final List<File> toLock)
    {
        final List<ReadWriteLock> locks = new ArrayList<ReadWriteLock>(toLock.size());
        for (File file : toLock) {
            locks.add(this.getLockForFile(file));
        }
        return new CompositeReadWriteLock(locks);
    }

    /**
     * Get the directory to store the attachment in.
     * This is a path obtained from the owner document reference, where each reference segment
     * (wiki, spaces, document name) contributes to the final path.
     * For a document called xwiki:Main.WebHome, the directory will be:
     * <code>(storageDir)/xwiki/Main/WebHome/~this/attachments/</code>
     * 
     * @param docRef the DocumentReference for the document to get the directory for.
     * @param storageDir the directory to place the directory hirearcy for attachments in.
     * @param pathSerializer an EntityReferenceSerializer which will make a directory path from an
     *                       an EntityReference.
     * @return a file path corresponding to the attachment location; each segment in the path is
     *         URL-encoded in order to be safe.
     */
    private static File getDocumentDir(final DocumentReference docRef,
                                       final File storageDir,
                                       final EntityReferenceSerializer<String> pathSerializer)
    {
        final File path = new File(storageDir, pathSerializer.serialize(docRef));
        final File docDir = new File(path, DOCUMENT_DIR_NAME);
        return new File(docDir, ATTACHMENT_DIR_NAME);
    }

    /**
     * A ReadWriteLock made up of many ReadWriteLocks.
     * To acquire this lock means acquiring all of it's component locks.
     */
    private static class CompositeReadWriteLock implements ReadWriteLock
    {
        /** The composite lock made of all the read locks. */
        private final Lock readLock;

        /** The composite lock made of all the write locks. */
        private final Lock writeLock;

        /**
         * The Constructor.
         *
         * @param members the locks to make this composite lock out of.
         */
        public CompositeReadWriteLock(final List<ReadWriteLock> members)
        {
            final List<Lock> readLocks = new ArrayList<Lock>();
            final List<Lock> writeLocks = new ArrayList<Lock>();
            for (ReadWriteLock member : members) {
                readLocks.add(member.readLock());
                writeLocks.add(member.writeLock());
            }
            this.readLock = new CompositeLock(readLocks);
            this.writeLock = new CompositeLock(writeLocks);
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.concurrent.locks.ReadWriteLock#readLock()
         */
        public Lock readLock()
        {
            return this.readLock;
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.concurrent.locks.ReadWriteLock#writeLock()
         */
        public Lock writeLock()
        {
            return this.writeLock;
        }
    }

    /**
     * A Lock made up of a number of other Locks.
     * Acquiring this lock means acquiring all of it's component locks.
     */
    private static class CompositeLock implements Lock
    {
        /** Exception to throw when a function is called which has not been written. */
        private static final String NOT_IMPLEMENTED = "Function not implemented.";

        /** The locks which make up this composite lock. */
        private final List<Lock> locks = new ArrayList<Lock>();

        /**
         * The Constructor.
         *
         * @param locks the locks which should make up this composite lock.
         */
        public CompositeLock(final List<Lock> locks)
        {
            this.locks.addAll(locks);
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.concurrent.locks.Lock#lock()
         */
        public void lock()
        {
            final List<Lock> toLock = new ArrayList(this.locks);
            try {
                // Get all of the locks which we can.
                int i = 0;
                while (i < toLock.size()) {
                    if (toLock.get(i).tryLock()) {
                        toLock.remove(i);
                    } else {
                        i++;
                    }
                }
                // Force the locks which we haven't locked yet.
                for (Lock stillWaiting : toLock) {
                    stillWaiting.lock();
                }
            } catch (RuntimeException e) {
                this.unlock();
                throw e;
            }
        }

        /**
         * {@inheritDoc}
         * Not implemented.
         *
         * @see java.util.concurrent.locks.Lock#lockInterruptibly()
         */
        public void lockInterruptibly()
        {
            throw new RuntimeException(NOT_IMPLEMENTED);
        }

        /**
         * {@inheritDoc}
         * Not implemented.
         *
         * @see java.util.concurrent.locks.Lock#newCondition()
         */
        public Condition newCondition()
        {
            throw new RuntimeException(NOT_IMPLEMENTED);
        }

        /**
         * {@inheritDoc}
         * Not implemented.
         *
         * @see java.util.concurrent.locks.Lock#tryLock()
         */
        public boolean tryLock()
        {
            throw new RuntimeException(NOT_IMPLEMENTED);
        }

        /**
         * {@inheritDoc}
         * Not implemented.
         *
         * @see java.util.concurrent.locks.Lock#tryLock(long, TimeUnit)
         */
        public boolean tryLock(final long time, final TimeUnit unit)
        {
            throw new RuntimeException(NOT_IMPLEMENTED);
        }

        /**
         * {@inheritDoc}
         *
         * @see java.util.concurrent.locks.Lock#unlock()
         */
        public void unlock()
        {
            for (Lock lock : this.locks) {
                try {
                    lock.unlock();
                } catch (RuntimeException e) {
                    // We probably don't own this lock so we just move on.
                }
            }
        }
    }
}
