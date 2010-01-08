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
package org.xwiki.officepreview.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.xwiki.bridge.AttachmentName;
import org.xwiki.bridge.AttachmentNameSerializer;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.bridge.DocumentName;
import org.xwiki.bridge.DocumentNameSerializer;
import org.xwiki.cache.Cache;
import org.xwiki.cache.CacheException;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.config.CacheConfiguration;
import org.xwiki.cache.eviction.LRUEvictionConfiguration;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.logging.AbstractLogEnabled;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.container.Container;
import org.xwiki.context.Execution;
import org.xwiki.officeimporter.openoffice.OpenOfficeManager;
import org.xwiki.officeimporter.openoffice.OpenOfficeManager.ManagerState;
import org.xwiki.officepreview.OfficePreviewBuilder;
import org.xwiki.officepreview.OfficePreviewConfiguration;
import org.xwiki.rendering.block.XDOM;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiAttachment;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * An abstract implementation of {@link OfficePreviewBuilder} which provides caching and other utility functions.
 * 
 * @version $Id$
 */
public abstract class AbstractOfficePreviewBuilder extends AbstractLogEnabled implements OfficePreviewBuilder,
    Initializable
{
    /**
     * Used to access the temporary directory.
     */
    @Requirement
    private Container container;

    /**
     * Reference to current execution.
     */
    @Requirement
    private Execution execution;

    /**
     * Used to access attachment content.
     */
    @Requirement
    private DocumentAccessBridge docBridge;

    /**
     * Used for serializing {@link DocumentName} instances into strings.
     */
    @Requirement
    private DocumentNameSerializer documentNameSerializer;

    /**
     * Used for serializing {@link AttachmentName} instances into strings.
     */
    @Requirement
    private AttachmentNameSerializer attachmentNameSerializer;

    /**
     * Used to query openoffice server state.
     */
    @Requirement
    private OpenOfficeManager officeManager;
    
    /**
     * Used to read configuration details.
     */
    @Requirement
    protected OfficePreviewConfiguration conf;

    /**
     * Used to initialize the previews cache.
     */
    @Requirement
    private CacheManager cacheManager;

    /**
     * Office document previews cache.
     */
    private Cache<OfficeDocumentPreview> previewsCache;

    /**
     * {@inheritDoc}
     */
    public void initialize() throws InitializationException
    {
        CacheConfiguration config = new CacheConfiguration();
        LRUEvictionConfiguration lec = new LRUEvictionConfiguration();
        
        lec.setMaxEntries(conf.getMaxCachedPreviewsCount());
        
        config.put(LRUEvictionConfiguration.CONFIGURATIONID, lec);
        try {
            previewsCache = cacheManager.createNewCache(config);
        } catch (CacheException ex) {
            throw new InitializationException("Error while initializing previews cache.", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public XDOM build(AttachmentName attachmentName) throws Exception
    {
        String strAttachmentName = attachmentNameSerializer.serialize(attachmentName);
        DocumentName reference = attachmentName.getDocumentName();

        // Search the cache.
        OfficeDocumentPreview preview = previewsCache.get(strAttachmentName);

        // It's possible that the attachment has been deleted. We need to catch such events and cleanup the cache.
        if (!docBridge.getAttachments(reference).contains(attachmentName)) {
            // If a cached preview exists, flush it.
            if (null != preview) {
                previewsCache.remove(strAttachmentName);
            }
            throw new Exception(String.format("Attachment [%s] does not exist.", strAttachmentName));
        }

        // Query the current version of the attachment.
        String currentVersion = getAttachmentVersion(attachmentName);

        // Check if the preview has been expired.
        if (null != preview && !currentVersion.equals(preview.getAttachmentVersion())) {
            // Flush the cached preview.
            previewsCache.remove(strAttachmentName);
            preview = null;
        }

        // If a preview in not available, build one.
        if (null == preview) {
            // Make sure an openoffice server is available.
            connect();

            // Build preview.
            preview = build(attachmentName, currentVersion, docBridge.getAttachmentContent(attachmentName));

            // Cache the preview.
            previewsCache.set(strAttachmentName, preview);
        }

        // Done.
        return preview.getXdom();
    }

    /**
     * Builds a {@link OfficeDocumentPreview} of the specified attachment.
     * 
     * @param attachmentName name of the attachment to be previewed.
     * @param attachmentVersion version of the attachment for which the preview should be generated for.
     * @param attachmentStream content stream of the attachment.
     * @return {@link OfficeDocumentPreview} corresponding to the specified attachment.
     * @throws Exception if an error occurs while building the attachment.
     */
    protected abstract OfficeDocumentPreview build(AttachmentName attachmentName, String attachmentVersion,
        InputStream attachmentStream) throws Exception;

    /**
     * Writes specified artifact into a temporary file.
     * 
     * @param attachmentName attachment into which this artifact belongs.
     * @param artifactName original name of the artifact.
     * @param fileData artifact data.
     * @return file that was just written.
     * @throws Exception if an error occurs while writing the temporary file.
     */
    protected File writeArtifact(AttachmentName attachmentName, String artifactName, byte[] fileData) throws Exception
    {
        String extension = artifactName.substring(artifactName.indexOf('.') + 1);
        String tempFileName = String.format("%s.%s", UUID.randomUUID().toString(), extension);
        File tempFile = new File(getTempDir(attachmentName), tempFileName);

        FileOutputStream fos = null;
        try {
            // Write the slide image into a temporary file.
            fos = new FileOutputStream(tempFile);
            IOUtils.write(fileData, fos);
            return tempFile;
        } finally {
            IOUtils.closeQuietly(fos);
        }            
    }

    /**
     * Utility method for obtaining a temporary storage directory for holding preview artifacts for a given attachment.
     * 
     * @param attachmentName name of the attachment.
     * @return directory used to hold temporary files belonging to the office preview of the specified attachment.
     */
    protected File getTempDir(AttachmentName attachmentName)
    {
        // TODO: For the moment we're using the charting directory to store office preview images. We need to change
        // this when a generic temporary-resource action becomes available.
        File tempDir = container.getApplicationContext().getTemporaryDirectory();
        return new File(tempDir, "charts");
    }

    /**
     * Utility method for building a URL for the specified temporary file belonging to the office preview of the given
     * attachment.
     * 
     * @param attachmentName name of the attachment being previewed.
     * @param fileName name of the temporary file.
     * @return URL string that refers the specified temporary file.
     */
    protected String getURL(AttachmentName attachmentName, String fileName)
    {
        // TODO: Since we are using the charting directory for holding temporary image files, here also we have to use
        // the charting action so that we have valid URL references. This needs to be updated when a temporary-resource
        // action becomes available.
        XWikiContext xcontext = getContext();
        try {
            return xcontext.getWiki().getExternalURL(null, "charting", xcontext) + "/" + fileName;
        } catch (Exception ex) {
            getLogger().error("Unexpected error.", ex);
        }
        return null;
    }

    /**
     * Attempts to connect to an openoffice server for conversions.
     * 
     * @throws Exception if an openoffice server is not available.
     */
    private void connect() throws Exception
    {
        if (!officeManager.getState().equals(ManagerState.CONNECTED)) {
            throw new Exception("OpenOffice server unavailable.");
        }
    }

    /**
     * Utility method for finding the current version of the specified attachment.
     * 
     * @param attachmentName name of the office attachment.
     * @return current version of the specified attachment.
     * @throws Exception if an error occurs while accessing attachment details.
     */
    private String getAttachmentVersion(AttachmentName attachmentName) throws Exception
    {
        XWikiContext xcontext = getContext();
        String documentName = documentNameSerializer.serialize(attachmentName.getDocumentName());
        XWikiDocument doc = xcontext.getWiki().getDocument(documentName, xcontext);
        XWikiAttachment attach = doc.getAttachment(attachmentName.getFileName());
        return attach.getVersion();
    }

    /**
     * @return {@link XWikiContext} instance.
     */
    private XWikiContext getContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
    }
}
