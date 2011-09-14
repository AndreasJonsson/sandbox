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

import org.junit.*;
import org.xwiki.cache.CacheManager;
import org.xwiki.model.*;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.test.AbstractBridgedComponentTestCase;

public class BridgedServerTest extends AbstractBridgedComponentTestCase
{
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        final XWiki xwiki = getMockery().mock(XWiki.class);
        getContext().setWiki(xwiki);
    }

    @Test
    public void addWiki() throws Exception
    {
        Server server = new BridgedServer(getContext(), new BridgedEntityManager(getContext(),
            getComponentManager().lookup(CacheManager.class)));
        Wiki wiki = server.addWiki("wiki");

        // Verify we get the exact same instance since we haven't saved yet.
        Assert.assertSame(wiki, server.getWiki("wiki"));
    }
}
