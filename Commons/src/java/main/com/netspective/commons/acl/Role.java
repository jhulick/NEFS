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
package com.netspective.commons.acl;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.netspective.commons.xdm.XmlDataModelSchema;

public class Role
{
    public static final XmlDataModelSchema.Options XML_DATA_MODEL_SCHEMA_OPTIONS = new XmlDataModelSchema.Options().setIgnorePcData(true);

    private AccessControlList owner;
    private Role parent;
    private int id = -1;
    private int level = 0;
    private String name;
    private String qualifiedName;
    private BitSet permissions = new BitSet();
    private List children = new ArrayList();
    private RoleOrPermissionReferences grants;
    private RoleOrPermissionReferences revokes;

    public Role(AccessControlList owner)
    {
        setOwner(owner);
        setId(getOwner().getHighestRoleId());
    }

    public Role(Role parent)
    {
        setParent(parent);
        setId(getOwner().getHighestRoleId());
    }

    public void unionChildPermissions(Role role)
    {
        permissions.or(role.getPermissions());
        if(getParent() != null) getParent().unionChildPermissions(this);
    }

    protected void setOwner(AccessControlList owner)
    {
        this.owner = owner;
    }

    public AccessControlList getOwner()
    {
        return owner;
    }

    protected void setParent(Role parent)
    {
        this.parent = parent;
        if(parent != null)
        {
            setOwner(parent.getOwner());
            setLevel(parent.getLevel() + 1);
        }
    }

    public Role getParent()
    {
        return parent;
    }

    public int getId()
    {
        return id;
    }

    protected void setId(int id)
    {
        this.id = id;
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getQualifiedName()
    {
        if(null == qualifiedName)
        {
            String qName = AccessControlList.NAME_SEPARATOR + getName();
            if(parent != null)
                qName = parent.getQualifiedName() + qName;
            else
                qName = owner.getQualifiedName() + qName;
            setQualifiedName(qName);
        }

        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName)
    {
        this.qualifiedName = qualifiedName;
    }

    public BitSet getPermissions()
    {
        return permissions;
    }

    public Set getPermissionNames()
    {
        final Set result = new TreeSet();
        final Map aclPerms = owner.getPermissionsById();

        for(Iterator i = aclPerms.entrySet().iterator(); i.hasNext();)
        {
            Map.Entry entry = (Map.Entry) i.next();
            int index = ((Integer) entry.getKey()).intValue();
            String qualifiedName = ((Permission) entry.getValue()).getQualifiedName();

            if(permissions.get(index))
                result.add(qualifiedName);
        }

        return result;
    }

    public Role createRole()
    {
        return new Role(this);
    }

    public void addRole(Role childRole)
    {
        children.add(childRole);
        unionChildPermissions(childRole);
        getOwner().registerRole(childRole);

        if(permissions == null)
            permissions = childRole.getPermissions();
        else
            permissions.or(childRole.getPermissions());
    }

    public RoleOrPermissionReference createGrant()
    {
        return new RoleOrPermissionReference(getOwner());
    }

    public void addGrant(RoleOrPermissionReference grant) throws PermissionNotFoundException, RoleNotFoundException
    {
        if(grants == null)
            grants = new RoleOrPermissionReferences();
        permissions.or(grant.getPermissions());
        grants.add(grant);
        if(parent != null) parent.addGrant(grant);
    }

    public RoleOrPermissionReference createRevoke()
    {
        return new RoleOrPermissionReference(getOwner());
    }

    public void addRevoke(RoleOrPermissionReference revoke) throws PermissionNotFoundException, RoleNotFoundException
    {
        if(revokes == null)
            revokes = new RoleOrPermissionReferences();
        permissions.andNot(revoke.getPermissions());
        revokes.add(revoke);
        if(parent != null) parent.addRevoke(revoke);
    }

    public int getAncestorsCount()
    {
        int result = 0;
        Role parent = getParent();
        while(parent != null)
        {
            result++;
            parent = parent.getParent();
        }
        return result;
    }

    public List getAncestorsList()
    {
        List result = new ArrayList();
        Role parent = getParent();
        while(parent != null)
        {
            if(result.size() == 0)
                result.add(parent);
            else
                result.add(0, parent);
            parent = parent.getParent();
        }
        return result;
    }

    public List getChildren()
    {
        return children;
    }

    public RoleOrPermissionReferences getGrants()
    {
        return grants;
    }

    public RoleOrPermissionReferences getRevokes()
    {
        return revokes;
    }

    public String toString()
    {
        int depth = getAncestorsCount();

        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < depth; i++)
            sb.append("  ");

        sb.append(getQualifiedName());
        sb.append(" = ");
        sb.append(getId());
        sb.append(" ");
        sb.append(permissions);
        sb.append("\n");

        for(int i = 0; i < children.size(); i++)
        {
            Role perm = (Role) children.get(i);
            sb.append(perm.toString());
        }

        return sb.toString();
    }
}