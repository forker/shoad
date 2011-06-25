/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.archone.vhd.model;

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
public class NameTransformUtilTest {
    
    private NameTransformUtil nameTransformUtil;
    
    public NameTransformUtilTest() {
        nameTransformUtil = new NameTransformUtil("ou=users","ou=groups","dc=example,dc=com");
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
     * Test of parseUserId method, of class NameTransformUtil.
     */
    @Test
    public void testParseUserId() {
        System.out.println("parseUserId");
        String dn = "uid=foo,ou=users,dc=example,dc=com";
        NameTransformUtil instance = this.nameTransformUtil;
        String expResult = "foo@users.example.com";
        String result = instance.parseUserId(dn);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractDomainDn method, of class NameTransformUtil.
     */
    @Test
    public void testExtractDomainDn() {
        System.out.println("extractDomainDn");
        String groupId = "administrator@groups.example.com";
        NameTransformUtil instance = this.nameTransformUtil;
        String expResult = "dc=example,dc=com";
        String result = instance.extractDomainDn(groupId);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractDomain method, of class NameTransformUtil.
     */
    @Test
    public void testExtractDomain() {
        System.out.println("extractDomain");
        String groupId = "administrator@groups.example.com";
        NameTransformUtil instance = this.nameTransformUtil;
        String expResult = "example.com";
        String result = instance.extractDomain(groupId);
        assertEquals(expResult, result);
    }

    /**
     * Test of isAdminGroupId method, of class NameTransformUtil.
     */
    @Test
    public void testIsAdminGroupId() {
        System.out.println("isAdminGroupId");
        String groupId = "administrator@groups.example.com";
        NameTransformUtil instance = this.nameTransformUtil;
        boolean expResult = true;
        boolean result = instance.isAdminGroupId(groupId);
        assertEquals(expResult, result);
    }

    /**
     * Test of parseDomainDn method, of class NameTransformUtil.
     */
    @Test
    public void testParseDomainDn() {
        System.out.println("parseDomainDn");
        String domain = "example.com";
        String expResult = "dc=example,dc=com";
        String result = NameTransformUtil.parseDomainDn(domain);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testParseUserDn() {
        System.out.println("ParseUserDn");
        String userId = "makaroni@users.example.com";
        String expResult = "uid=makaroni,ou=users,dc=example,dc=com";
        String result = nameTransformUtil.parseUserDn(userId);
        assertEquals(expResult, result);
    }

}
