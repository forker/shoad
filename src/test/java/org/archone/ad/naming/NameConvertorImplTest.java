/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.naming;

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
public class NameConvertorImplTest {
    
    private NameConvertor instance = new NameConvertor();
    
    public NameConvertorImplTest() {
        
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
     * Test of toGroupDn method, of class NameConvertorImpl.
     */
    @Test
    public void testToGroupDn() throws InvalidNameException {
        System.out.println("toGroupDn");
        String groupId = "test@groups.example.com";
        String expResult = "cn=test,ou=groups,dc=example,dc=com";
        String result = instance.toGroupDn(groupId);
        assertEquals(expResult, result);
    }

    /**
     * Test of toUserDn method, of class NameConvertorImpl.
     */
    @Test
    public void testToUserDn() throws InvalidNameException {
        System.out.println("toUserDn");
        String userId = "testuser@users.example.com";
        String expResult = "uid=testuser,ou=users,dc=example,dc=com";
        String result = instance.toUserDn(userId);
        assertEquals(expResult, result);
    }

    /**
     * Test of toGroupId method, of class NameConvertorImpl.
     */
    @Test
    public void testToGroupId() throws InvalidNameException {
        System.out.println("toGroupId");
        String groupDn = "cn=test,ou=groups,dc=example,dc=com";
        String expResult = "test@groups.example.com";
        String result = instance.toGroupId(groupDn);
        assertEquals(expResult, result);
    }

    /**
     * Test of toUserId method, of class NameConvertorImpl.
     */
    @Test
    public void testToUserId() throws InvalidNameException {
        System.out.println("toUserId");
        String userDn = "uid=testuser,ou=users,dc=example,dc=com";
        String expResult = "testuser@users.example.com";
        String result = instance.toUserId(userDn);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractDomain method, of class NameConvertorImpl.
     */
    @Test
    public void testExtractDomain() throws InvalidNameException {
        System.out.println("extractDomain");
        String entityId = "testuser@users.example.com";
        String expResult = "example.com";
        String result = instance.extractDomain(entityId);
        assertEquals(expResult, result);
    }

    /**
     * Test of extractDomainDn method, of class NameConvertorImpl.
     */
    @Test
    public void testExtractDomainDn() throws InvalidNameException {
        System.out.println("extractDomainDn");
        String entityId = "testuser@users.example.com";
        String expResult = "dc=example,dc=com";
        String result = instance.extractDomainDn(entityId);
        assertEquals(expResult, result);
    }

    /**
     * Test of toDomainDn method, of class NameConvertorImpl.
     */
    @Test
    public void testToDomainDn() throws InvalidNameException {
        System.out.println("toDomainDn");
        String domain = "example.com";
        String expResult = "dc=example,dc=com";
        String result = instance.toDomainDn(domain);
        assertEquals(expResult, result);
    }

    /**
     * Test of toDomain method, of class NameConvertorImpl.
     */
    @Test
    public void testToDomain() throws Exception {
        System.out.println("toDomain");
        String domainDn = "dc=example,dc=com";
        String expResult = "example.com";
        String result = instance.toDomain(domainDn);
        assertEquals(expResult, result);
    }
}
