/*
 * Content of this file is subject to the license
 * you can find in the enclosed LICENSE.txt file with the project.
 */
package org.archone.ad.model;

import org.archone.ad.naming.GroupDn;
import org.archone.ad.naming.NameHelper;
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
public class GroupDnTest {
    
    private NameHelper dh;
    
    public GroupDnTest() {
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
     * Test of getDomain method, of class GroupDn.
     */
    @Test
    public void testGetDomain() throws InvalidNameException {
        System.out.println("getDomain");
        GroupDn instance = dh.newGroupDn("cn=mygroup,ou=groups,dc=big,dc=bang");
        String expResult = "big.bang";
        String result = instance.getDomain();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAsGroupId method, of class GroupDn.
     */
    @Test
    public void testGetAsGroupId() throws InvalidNameException {
        System.out.println("getAsGroupId");
        GroupDn instance = dh.newGroupDn("cn=mygroup,ou=groups,dc=big,dc=bang");
        String expResult = "mygroup@groups.big.bang";
        String result = instance.getAsGroupId();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testFromGroupId() throws InvalidNameException {
        System.out.println("fromGroupId");
        String groupId = "adminos@groups.example.com";
        GroupDn instance = dh.newGroupDnFromId(groupId);
        
        assertEquals(groupId, instance.getAsGroupId());
        
    }
    
}
