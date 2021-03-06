/*
 * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
 *
 * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
 * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
 * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
 * before using The Software. Any use of The Software indicates agreement with the NSL.
 *
 * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
 *
 * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
 *    containing the .class files) and only as part of an application that uses The Software as part of its primary
 *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
 *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
 *    same restrictions.
 *
 * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
 *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
 *
 * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
 *    used to endorse or appear in products derived from The Software without written consent of Netspective.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
 * ARE HEREBY DISCLAIMED.
 *
 * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
 * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
 * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */
package com.netspective.commons.security;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.netspective.commons.acl.AccessControlListsManager;
import com.netspective.commons.acl.Permission;
import com.netspective.commons.acl.PermissionNotFoundException;
import com.netspective.commons.acl.Role;
import com.netspective.commons.acl.RoleNotFoundException;
import com.netspective.commons.value.ValueContext;

public class BasicAuthenticatedUser implements MutableAuthenticatedUser, java.io.Serializable
{
    private static final Log log = LogFactory.getLog(BasicAuthenticatedUser.class);

    private Object userId;
    private String userName;
    private String encryptedPassword;
    private boolean isRemembered;
    private String[] userRoleNames;
    private String[] userPermissionNames;
    private Set userRoleNamesSet = new HashSet();
    private BitSet userPermissions;
    private AuthenticatedOrganizations organizations = createOrganizations();

    public BasicAuthenticatedUser()
    {
    }

    public void init(ValueContext vc) throws AuthenticatedUserInitializationException
    {
    }

    public String getUserName()
    {
        return userName != null ? userName : (userId != null ? userId.toString() : null);
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getName() // implementation for java.security.Principal
    {
        return userId.toString();
    }

    public Object getUserId()
    {
        return userId;
    }

    public void setUserId(Object userId)
    {
        this.userId = userId;
    }

    public String getEncryptedPassword()
    {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword)
    {
        this.encryptedPassword = encryptedPassword;
    }

    public void setUnencryptedPassword(String unEncryptedPassword)
    {
        // never store the unencrypted password -- it will live in VM as plaintext
        setEncryptedPassword(Crypt.crypt(PASSWORD_ENCRYPTION_SALT, unEncryptedPassword));
    }

    public BitSet getUserPermissions()
    {
        return userPermissions;
    }

    public String[] getUserRoleNames()
    {
        return userRoleNames;
    }

    /**
     * Gets the names of all assigned roles as one comma separated string
     */
    public String getUserRoleNamesAsString()
    {
        if(userRoleNames != null)
        {
            StringBuffer sb = new StringBuffer();
            for(int i = 0; i < userRoleNames.length; i++)
            {
                if(i != 0)
                    sb.append(", ");
                sb.append(userRoleNames[i]);
            }
            return sb.toString();
        }
        return null;
    }

    public boolean isUserInRole(String roleName)
    {
        return userRoleNamesSet.contains(roleName);
    }

    public BitSet createPermissionsBitSet(AccessControlListsManager aclsManager)
    {
        return new BitSet(aclsManager.getAccessControlLists().getHighestPermissionId());
    }

    public void setPermissions(AccessControlListsManager aclsManager, String[] permissions) throws PermissionNotFoundException
    {
        userPermissionNames = permissions;
        userPermissions = null;
        if(userPermissionNames == null)
            return;

        userPermissions = createPermissionsBitSet(aclsManager);
        for(int i = 0; i < permissions.length; i++)
        {
            String permName = permissions[i];
            Permission permission = aclsManager.getPermission(permName);
            userPermissions.or(permission.getChildPermissions());
        }
    }

    public void setRoles(AccessControlListsManager aclsManager, String[] roles) throws RoleNotFoundException
    {
        userRoleNames = roles;
        userRoleNamesSet.clear();
        userPermissions = null;
        if(userRoleNames == null)
            return;

        userPermissions = createPermissionsBitSet(aclsManager);
        for(int i = 0; i < roles.length; i++)
        {
            String roleName = roles[i];
            Role role = aclsManager.getRole(roleName);
            userPermissions.or(role.getPermissions());
        }

        for(int i = 0; i < userRoleNames.length; i++)
            userRoleNamesSet.add(userRoleNames[i]);
    }

    public boolean hasPermission(AccessControlListsManager aclsManager, String permissionName) throws PermissionNotFoundException
    {
        Permission perm = aclsManager.getPermission(permissionName);
        if(perm == null)
            throw new RuntimeException("Permission '" + permissionName + "' does not exist in ACL.");
        return userPermissions.get(perm.getId());
    }

    public boolean hasAnyPermission(AccessControlListsManager aclsManager, String[] permissionNames) throws PermissionNotFoundException
    {
        for(int i = 0; i < permissionNames.length; i++)
        {
            if(hasPermission(aclsManager, permissionNames[i]))
                return true;
        }
        return false;
    }

    public boolean isRemembered()
    {
        return isRemembered;
    }

    public void setRemembered(boolean isRemembered)
    {
        this.isRemembered = isRemembered;
    }

    public void registerLogin(ValueContext vc)
    {
    }

    public void registerLogout(ValueContext vc, AuthenticatedUserLogoutType type)
    {
    }

    public AuthenticatedOrganizations getOrganizations()
    {
        return organizations;
    }

    protected MutableAuthenticatedOrganizations createOrganizations()
    {
        return new BasicAuthenticatedOrganizations();
    }
}