/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.model;

import org.archone.ad.naming.NameHelper;
import org.archone.ad.naming.UserDn;
import org.archone.ad.naming.NameHelper;
import javax.naming.InvalidNameException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author romansergey
 */
public class UserDnTest {
    
    private NameHelper dh;
    
    public UserDnTest() {
        dh = new NameHelper("ou=users", "ou=groups");
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

    /**
     * Test of getDomain method, of class UserDn.
     */
    @Test
    public void testGetDomain() throws InvalidNameException {
        System.out.println("getDomain");
        UserDn instance = dh.newUserDn("uid=kuzya,ou=users,dc=example,dc=com");
        String expResult = "example.com";
        String result = instance.getDomain();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAsUserId method, of class UserDn.
     */
    @Test
    public void testGetAsUserId() throws InvalidNameException {
        System.out.println("getAsUserId");
        UserDn instance = dh.newUserDn("uid=kuzya,ou=users,dc=example,dc=com");
        String expResult = "kuzya@example.com";
        String result = instance.getAsUserId();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testFromUserId() throws InvalidNameException {
        String userId = "kuzya@example.com";
        UserDn userDn = dh.newUserDnFromId(userId);
        
        assertEquals(userId, userDn.getAsUserId());
    }
    
}
