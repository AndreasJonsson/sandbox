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

package org.xwiki.escaping.framework;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

import org.xwiki.escaping.suite.FileTest;

/**
 * Abstract base class for escaping tests. Implements common initialization pattern and some utility methods
 * like URL escaping, retrieving page content by URL etc. Subclasses need to implement parsing
 * and custom tests.
 * <p>
 * Note: JUnit4 requires tests to have one public default constructor, subclasses will need to implement
 * it and pass pattern matcher to match file names they can handle.</p>
 * 
 * @version $Id$
 * @since 2.5
 */
public abstract class AbstractEscapingTest implements FileTest
{
    /** HTTP client shared between all subclasses. */
    private static HttpClient client;

    /** File name of the template to use. */
    protected String name;

    /** User provided data found in the file. */
    protected Set<String> userInput;

    /** Pattern used to match files by name. */
    private Pattern namePattern;

    /**
     * Create new AbstractEscapingTest
     * 
     * @param fileNameMatcher regex pattern used to filter files by name
     */
    protected AbstractEscapingTest(Pattern fileNameMatcher)
    {
        namePattern = fileNameMatcher;
    }

    /**
     * {@inheritDoc}
     * @see org.xwiki.escaping.suite.FileTest#initialize(java.lang.String, java.io.Reader)
     */
    public boolean initialize(String name, final Reader reader)
    {
        if (namePattern != null && namePattern.matcher(name).matches()) {
            this.userInput = parse(reader);
            if (!userInput.isEmpty()) {
                this.name = name;
                // TODO do something
                return true;
            }
        }
        return false;
    }

    /**
     * Parse the file and collect parameters controlled by the user.
     * 
     * @param reader the reader associated with the file
     * @return collection of user-controlled input parameters
     */
    protected abstract Set<String> parse(Reader reader);

    /**
     * Download a page from the server and return its content.
     * 
     * @param url URL of the page
     * @return content of the page
     * @throws EscapingException on connection problems
     */
    protected InputStream getUrlContent(String url) throws EscapingException
    {
        GetMethod get = new GetMethod(url);
        get.setFollowRedirects(true);
        get.setDoAuthentication(true);

        // make the request
        HttpClient client = AbstractEscapingTest.getClient();
        try {
            int statusCode = client.executeMethod(get);
            if (statusCode != HttpStatus.SC_OK) {
                throw new EscapingException("HTTP GET request returned status " + statusCode + " for URL: " + url);
            }

            // get the data, converting to utf-8
            String str = get.getResponseBodyAsString();
            if (str == null) {
                return null;
            }
            return new ByteArrayInputStream(str.getBytes("utf-8"));
        } catch (Exception exception) {
            throw new EscapingException(exception);
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * URL-escape given string.
     * 
     * @param str string to escape
     * @return URL-escaped {@code str}
     */
    protected final String escapeUrl(String str)
    {
        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException exception) {
            // should not happen
            throw new RuntimeException("Should not happen: ", exception);
        }
    }

    /**
     * Get an instance of the HTTP client to use.
     * 
     * @return HTTP client initialized with admin credentials
     */
    protected static HttpClient getClient()
    {
        if (AbstractEscapingTest.client == null) {
            HttpClient adminClient = new HttpClient();
            Credentials defaultcreds = new UsernamePasswordCredentials("Admin", "admin");
            adminClient.getState().setCredentials(AuthScope.ANY, defaultcreds);
            HttpClientParams clientParams = new HttpClientParams();
            clientParams.setSoTimeout(2000);
            HttpConnectionManagerParams connectionParams = new HttpConnectionManagerParams();
            connectionParams.setConnectionTimeout(30000);
            adminClient.getHttpConnectionManager().setParams(connectionParams);
            AbstractEscapingTest.client = adminClient;
        }
        return AbstractEscapingTest.client;
    }

    /**
     * {@inheritDoc}
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return name + " [space, page, parameters: " + userInput + "]";
    }
}
