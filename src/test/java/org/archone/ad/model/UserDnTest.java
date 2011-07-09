/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.ad.model;

import org.archone.ad.naming.NameHelperImpl;
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
 * @author forker
 */
public class UserDnTest {
    
    private NameHelper dh;
    
    public UserDnTest() {
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
        String expResult = "kuzya@users.example.com";
        String result = instance.getAsUserId();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testFromUserId() throws InvalidNameException {
        String userId = "kuzya@users.example.com";
        UserDn userDn = dh.newUserDnFromId(userId);
        
        assertEquals(userId, userDn.getAsUserId());
    }
    
}
