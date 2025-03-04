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
 *
 */
package org.xwiki.security.internal;

import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.bridge.DocumentModelBridge;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;

import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.user.api.XWikiGroupService;

import org.xwiki.security.RightServiceException;
import org.xwiki.security.RightCache;

import com.xpn.xwiki.web.Utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains utility functions for accessing various xwiki
 * objecst, such as the XWikiGroupService.
 * @version $Id: XWikiUtils.java 30733 2010-08-24 22:22:15Z sdumitriu $
 */
final class XWikiUtils
{
    /** Name of document where wiki rights are stored. */
    static final String WIKI_DOC = "XWikiPreferences";

    /** Name of the space where wiki document is stored. */
    static final String WIKI_SPACE = "XWiki";

    /** Name of document where space rights are stored. */
    static final String SPACE_DOC = "WebPreferences";

    /** Name of group class. */
    static final String GROUP_CLASS = "XWiki.XWikiGroups";

    /** Name of group class property. */
    static final String GROUP_CLASS_PROPERTY = "member";

    /**
     * The logging tool.
     */
    private static final Logger LOG = LoggerFactory.getLogger(XWikiUtils.class);

    /** Hide constructor. */
    private XWikiUtils()
    {
    }

    /**
     * @param user The identity of the user.
     * @return the collection of groups represented by their document references.
     * @throws RightServiceException on error.
     */
    public static Collection<DocumentReference> getGroupsForUser(DocumentReference user)
        throws RightServiceException
    {
        DocumentReferenceResolver<String> resolver   = getUserResolver();
        EntityReferenceSerializer<String> serializer = Utils.getComponent(EntityReferenceSerializer.class);

        Collection<String> groupNames;
        try {
            XWikiContext xwikiContext = getXWikiContext();
            XWikiGroupService groupService = xwikiContext.getWiki().getGroupService(xwikiContext);
            String userName = serializer.serialize(user);
            groupNames = groupService.getAllGroupsNamesForMember(userName, Integer.MAX_VALUE, 0, xwikiContext);
        } catch (XWikiException e) {
            throw new RightServiceException("Failed to generate the group names.",  e);
        }
        /*
         * The groups inherit the wiki of the user, unless the wiki
         * name is explicitly given.
         */
        String wikiName = user.getWikiReference().getName();
        Collection<DocumentReference> groups = new LinkedList();
        for (String groupName : groupNames) {
            DocumentReference group = resolver.resolve(groupName, wikiName);
            groups.add(group);
        }
        return groups;
    }

    /**
     * Due to the special case where a user have been added to the
     * group, we need to step through all members of the group and
     * remove all corresponding user entries.
     * @param group The group.
     * @param rightCache Right cache instance to invalidate.
     * @throws RightServiceException on error.
     */
    public static void invalidateGroupMembers(DocumentReference group,
                                              RightCache rightCache)
        throws RightServiceException
    {
        DocumentReferenceResolver<String> resolver = getUserResolver();
        EntityReferenceSerializer<String> serializer  = Utils.getComponent(EntityReferenceSerializer.class);
        try {
            XWikiContext xwikiContext = getXWikiContext();
            XWikiGroupService groupService = xwikiContext.getWiki().getGroupService(xwikiContext);
            String groupName = serializer.serialize(group);
            /*
             * The group members inherit the wiki from the group
             * itself, unless the wiki name is explicitly given.
             */
            String wikiName = group.getWikiReference().getName();
            final int nb = 100;
            int i = 0;
            Collection<String> memberNames;
            do {
                memberNames = groupService.getAllMembersNamesForGroup(groupName,
                                                                      nb,
                                                                      i * nb,
                                                                      xwikiContext);
                for (String member : memberNames) {
                    DocumentReference memberRef = resolver.resolve(member, wikiName);
                    /*
                     * Avoid infinite loops.
                     */
                    if (!memberRef.equals(group)) {
                        rightCache.remove(rightCache.getRightCacheKey(memberRef));
                    }
                }
                i++;
            } while(memberNames.size() == nb);
        } catch (XWikiException e) {
            LOG.error("Failed to invalidate group members.", e);
            throw new RightServiceException(e);
        }
    }

    /**
     * @param source an xwiki document, that has just been updated.
     * @return true if and only if the xwiki document corresponds to a group.
     */
    public static boolean isGroupDocument(Object source)
    {
        DocumentAccessBridge ab = Utils.getComponent(DocumentAccessBridge.class);
        DocumentModelBridge doc = (DocumentModelBridge) source;
        DocumentReference docRef = doc.getDocumentReference();
        DocumentReferenceResolver<String> resolver = Utils.getComponent(DocumentReferenceResolver.class);
        DocumentReference groupClass = resolver.resolve(GROUP_CLASS, docRef);
        Object prop = ab.getProperty(docRef, groupClass, GROUP_CLASS_PROPERTY);
        return prop != null;
    }

    /**
     * Obtain a document reference to the {@link DocumentModelBridge} given
     * as parameter.
     * @param documentModelBridge The xwiki document.
     * @return The document reference.
     */
    public static DocumentReference getDocumentReference(Object documentModelBridge)
    {
        DocumentModelBridge doc = (DocumentModelBridge) documentModelBridge;
        return doc.getDocumentReference();
    }

    /** @return the XWikiContext */
    private static XWikiContext getXWikiContext()
    {
        Execution execution = Utils.getComponent(Execution.class);
        ExecutionContext context = execution.getContext();
        XWikiContext xwikiContext = (XWikiContext) context.getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
        return xwikiContext;
    }

    /**
     * @return the name of the main wiki.
     */
    public static String getMainWiki()
    {
        return getXWikiContext().getMainXWiki();
    }

    /** @return a document reference resolver for user names. */
    private static DocumentReferenceResolver<String> getUserResolver()
    {
        return Utils.getComponent(DocumentReferenceResolver.class, "user");
    }

    /**
     * @param user A user identity.
     * @param document A document reference.
     * @return {@code true} if and only if the user is the creator of
     * the document.
     */
    public static boolean isCreator(DocumentReference user, DocumentReference document)
    {
        XWikiContext context = getXWikiContext();
        String creator;
        try {
            if (context.getWiki().getDocument(document, context) == null) {
                return false;
            }
            creator = context.getWiki().getDocument(document, context).getCreator();
        } catch (XWikiException e) {
            LOG.error("Caught exception.", e);
            return false;
        }
        DocumentReferenceResolver<String> resolver = getUserResolver();
        DocumentReference creatorRef = resolver.resolve(creator, document.getWikiReference().getName());
        return user.equals(creatorRef);
    }

    /**
     * @param user A user identity.
     * @param wikiName The name of the wiki.
     * @return {@code true} if and only if the user is the owner of
     * the wiki.
     */
    public static boolean isWikiOwner(DocumentReference user, String wikiName)
    {
        XWikiContext context = getXWikiContext();
        DocumentReferenceResolver<String> resolver = getUserResolver();
        String wikiOwner;
        try {
            wikiOwner = context.getWiki().getWikiOwner(wikiName, context);
        } catch (XWikiException e) {
            LOG.error("Failed to obtain wiki owner.", e);
            return false;
        }
        DocumentReference ownerRef = resolver.resolve(wikiOwner, wikiName);
        return user.equals(ownerRef);
    }

}
