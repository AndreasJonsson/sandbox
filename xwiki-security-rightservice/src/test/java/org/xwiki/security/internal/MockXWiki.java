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

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;

import java.util.Map;
import java.util.HashMap;

import com.xpn.xwiki.web.Utils;

public class MockXWiki extends XWiki
{
    Map<DocumentReference, MockDocument> documents = new HashMap();
    Map<String, String> wikiOwners = new HashMap();
    //    private final HashMap<DocumentReference, HashMap<DocumentReference, HashMap<String, Object>>> properties = new HashMap();

    private final XWiki mainXWiki;
    private String cacheCapacity = "500";

    public MockXWiki(String name, XWiki mainXWiki)
    {
        super();
        this.mainXWiki = mainXWiki;
        super.setDatabase(name);
    }

    public MockXWiki add(MockDocument doc)
    {
        documents.put(doc.getDocumentReference(), doc);
        return this;
    }

    @Override
    public XWikiDocument getDocument(DocumentReference docRef, XWikiContext context)
    {
        if (documents.get(docRef) == null) {
            System.out.println("Tried to get non-existing document: " + docRef);
        }
        return documents.get(docRef);
    }

    @Override
    public XWikiDocument getDocument(String docName, XWikiContext context)
    {
        DocumentReferenceResolver<String> resolver = Utils.getComponent(DocumentReferenceResolver.class);
        return getDocument(resolver.resolve(docName), context);
    }

    @Override
    public String Param(String key, String defaultValue)
    {
        if (key.equals("xwiki.security.rightcache.capacity")) {
            return cacheCapacity;
        }
        return super.Param(key, defaultValue);
    }

    public void setCacheCapacity(Integer value)
    {
        cacheCapacity = value.toString();
    }

    @Override
    public String getWikiOwner(String wikiName, XWikiContext context)
    {
        String userName = wikiOwners.get(wikiName);
        if (userName == null) {
            return "xwiki:XWiki.Admin";
        }
        return userName;
    }

    /*
    @Override
    public Object getProperty(DocumentReference docRef, DocumentReference classRef, String propertyName) {
        HashMap<DocumentReference, HashMap<String, Object>> docProperties = properties.get(docRef);
        if (docProperties == null) {
            return null;
        }

        HashMap<String, Object> classProperties = docProperties.get(classRef);
        if (classProperties == null) {
            return null;
        }

        return classProperties.get(propertyName);
    }

    public void addProperty(DocumentReference docRef, DocumentReference classRef, String propertyName, Object value) {
        HashMap<DocumentReference, HashMap<String, Object>> docProperties = properties.get(docRef);
        if (docProperties == null) {
            docProperties = new HashMap();
            properties.put(docRef, docProperties);
        }

        HashMap<String, Object> classProperties = docProperties.get(classRef);
        if (classProperties == null) {
            classProperties = new HashMap();
            properties.put(classRef, classProperties);
        }
        
        classProperties.put(propertyName, value);
    }
    */

    public void setWikiOwner(String wikiName, String userName) {
        wikiOwners.put(wikiName, userName);
    }
}