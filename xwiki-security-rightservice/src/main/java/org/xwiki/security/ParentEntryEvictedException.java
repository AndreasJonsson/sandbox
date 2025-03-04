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
package org.xwiki.security;

/**
 * All cache entries except wiki cache entries must have their parent
 * cached, so the {@link RightsLoader}  must insert the entries, if missing in
 * turn.
 *
 * There is a chance, though, that the cache will evict a parent entry
 * of the entry that the {@link RightsLoader} is about to insert.
 * When this happens, this exception is thrown and the attempt to load
 * the cache must be restarted.
 *
 * @version $Id: ParentEntryEvictedException.java 30733 2010-08-24 22:22:15Z sdumitriu $
 */
public class ParentEntryEvictedException extends Exception
{
}
