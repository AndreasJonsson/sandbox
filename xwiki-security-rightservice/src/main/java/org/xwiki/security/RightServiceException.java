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
 * @version $Id: RightServiceException.java 30733 2010-08-24 22:22:15Z sdumitriu $
 */
public class RightServiceException extends Exception
{
    /**
     * @see java.lang.Exception}. 
     */
    public RightServiceException()
    {
        super();
    }

    /**
     * @param message Message.
     * @see java.lang.Exception.
     */
    public RightServiceException(String message)
    {
        super(message);
    }
    
    /**
     * @param message Message.
     * @param cause Original cause.
     * @see java.lang.Exception.
     */
    public RightServiceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * @param cause Original cause.
     * @see java.lang.Exception.
     */
    public RightServiceException(Throwable cause)
    {
        super(cause);
    }
}
