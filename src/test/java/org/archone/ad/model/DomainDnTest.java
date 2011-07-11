/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.model;

import org.archone.ad.naming.NameHelperImpl;
import org.archone.ad.naming.DomainDn;
import org.archone.ad.naming.NameHelper;
import javax.naming.InvalidNameException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author romansergey
 */
public class DomainDnTest {
    
    private NameHelper dh;

    public DomainDnTest() {
        dh = new NameHelperImpl("ou=users", "ou=groups");
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testClass() throws InvalidNameException {
        String domainDn = "dc=example,dc=com";
        String notDomainDn = "ou=some,dc=example,dc=com";
        DomainDn dd = dh.newDomainDn(domainDn);

        if (!dd.getDomain().equals("example.com")) {
            Assert.fail("getDomain failed");
        }

        try {
            DomainDn dd2 = dh.newDomainDn(notDomainDn);
            Assert.fail("Shouldn't have gotten here!");
        } catch (InvalidNameException ex) {
            return;
        }

    }
    
    public void testFromDomain() throws InvalidNameException {
        String domain = "megaxmpl.com";
        DomainDn instance = DomainDn.fromDomain(domain);
        
        Assert.assertEquals(instance.getDomain(), domain);
    }
    
}
