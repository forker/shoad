/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.vhd.ldap;

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
public class RegexTest {
    
    public RegexTest() {
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
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void hello() {
        String dn = "cn=Administrator,ou=groups,dc=example,dc=com";
        assertEquals(true, dn.toLowerCase().matches("cn=administrator,ou=groups(,dc=[a-z]{1,}){1,}"));
    }
}
