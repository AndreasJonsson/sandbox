package com.xwiki.authentication.trustedldap;

import java.util.Map;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.test.AbstractBridgedComponentTestCase;

public class TrustedLDAPAuthServiceImplTest extends AbstractBridgedComponentTestCase
{
    private Mockery mockery = new Mockery()
    {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private XWiki xwikiMock;

    private TrustedLDAPAuthServiceImpl authenticator;

    @Before
    public void setUp() throws Exception
    {
        super.setUp();

        this.xwikiMock = this.mockery.mock(XWiki.class);

        getContext().setWiki(this.xwikiMock);
        getContext().setDatabase("xwiki");

        this.mockery.checking(new Expectations()
        {{
            allowing(xwikiMock).Param(with(any(String.class))); will(returnValue(null));
        }});
        
        this.authenticator = new TrustedLDAPAuthServiceImpl();
    }

    @After
    public void tearDown()
    {
        this.mockery.assertIsSatisfied();
    }

    @Test
    public void testParseRemoteUserWithNoConfiguration() throws Exception
    {             
        this.mockery.checking(new Expectations()
        {{
            allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserParser", getContext()); will(returnValue(null));
        }});
        
        Map<String, String> ldapConfiguration = this.authenticator.parseRemoteUser("remoteuser", getContext());
        
        Assert.assertEquals("remoteuser", ldapConfiguration.get("login"));
    }
    
    @Test
    public void testParseRemoteUserWithSimplePattern() throws Exception
    {       
        this.mockery.checking(new Expectations()
        {{
            allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserParser", getContext()); will(returnValue("remote"));
        }});
        
        Map<String, String> ldapConfiguration = this.authenticator.parseRemoteUser("remoteuser", getContext());
        
        Assert.assertEquals("remote", ldapConfiguration.get("login"));
    }
    
    @Test
    public void testParseRemoteUserWithGroupsPattern() throws Exception
    {       
        this.mockery.checking(new Expectations()
        {{
            allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserParser", getContext()); will(returnValue("(remote)(user)"));
            allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.1", getContext()); will(returnValue("login"));
            allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.2", getContext()); will(returnValue("ldap_server,ldap_base_DN"));
            allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.login", getContext()); will(returnValue(null));
            allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.ldap_server", getContext()); will(returnValue(null));
            allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.ldap_base_DN", getContext()); will(returnValue(null));
        }});
        
        Map<String, String> ldapConfiguration = this.authenticator.parseRemoteUser("remoteuser", getContext());
        
        Assert.assertEquals("remote", ldapConfiguration.get("login"));
        Assert.assertEquals("user", ldapConfiguration.get("ldap_server"));
        Assert.assertEquals("user", ldapConfiguration.get("ldap_base_DN"));
    }
    
    @Test
    public void testParseRemoteUserWithGroupsPatternandConversions() throws Exception
    {       
        this.mockery.checking(new Expectations()
        {{
            allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserParser", getContext()); will(returnValue("(.+)@(.+)"));
            allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.1", getContext()); will(returnValue("login"));
            allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.2", getContext()); will(returnValue("ldap_server,ldap_base_DN"));
            allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.login", getContext()); will(returnValue(null));
            allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.ldap_server", getContext()); will(returnValue("doMain=my.domain.com|domain2=my.domain2.com"));
            allowing(xwikiMock).getXWikiPreference("trustedldap_remoteUserMapping.ldap_base_DN", getContext()); will(returnValue("dOmain=dc=my,dc=domain,dc=com|domain2=dc=my,dc=domain2,dc=com"));
        }});
        
        Map<String, String> ldapConfiguration = this.authenticator.parseRemoteUser("user@domain", getContext());
        
        Assert.assertEquals("user", ldapConfiguration.get("login"));
        Assert.assertEquals("my.domain.com", ldapConfiguration.get("ldap_server"));
        Assert.assertEquals("dc=my,dc=domain,dc=com", ldapConfiguration.get("ldap_base_DN"));
    }
}
