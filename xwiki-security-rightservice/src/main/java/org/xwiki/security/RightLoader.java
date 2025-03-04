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

import org.xwiki.component.annotation.ComponentRole;

import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.DocumentReference;

/**
 * Component for loading rights into a right cache.
 * @version $Id: RightLoader.java 30733 2010-08-24 22:22:15Z sdumitriu $
 */
@ComponentRole
public interface RightLoader
{
    /**
     * Load the cache with the required entries to look up the access
     * level for the user on the given entity.
     * @param user The user identity.
     * @param entity The entity.
     * @return The resulting access level for the user at the entity.
     * @exception RightServiceException if an error occurs.
     */
    AccessLevel load(DocumentReference user, EntityReference entity)
        throws RightServiceException;

}