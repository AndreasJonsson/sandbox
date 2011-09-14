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
package org.xwiki.model.internal;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.doc.XWikiDocument;

import org.xwiki.model.Entity;
import org.xwiki.model.EntityIterator;
import org.xwiki.model.EntityManager;
import org.xwiki.model.EntityType;
import org.xwiki.model.ModelException;
import org.xwiki.model.Server;
import org.xwiki.model.UniqueReference;
import org.xwiki.model.Wiki;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.WikiReference;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

public class BridgedServer implements Server
{
    private XWikiContext xcontext;

    private EntityManager entityManager;

    public BridgedServer(XWikiContext xcontext, EntityManager entityManager)
    {
        this.xcontext = xcontext;
        this.entityManager = entityManager;
    }

    public Wiki addWiki(String wikiName)
    {
        return new BridgedWiki(getXWikiContext());
    }

    public Wiki getWiki(String wikiName)
    {
        return this.entityManager.getEntity(new UniqueReference(new WikiReference(wikiName)));
    }

    public EntityIterator<Wiki> getWikis()
    {
        throw new ModelException("Not supported");
    }

    public boolean hasWiki(String wikiName)
    {
        throw new ModelException("Not supported");
    }

    public void removeWiki(String wikiName)
    {
        throw new ModelException("Not supported");
    }

    public void save(String comment, boolean isMinorEdit, Map<String, String> extraParameters)
    {
        throw new ModelException("Not supported");
    }

    public XWikiContext getXWikiContext()
    {
        return this.xcontext;
    }
}
