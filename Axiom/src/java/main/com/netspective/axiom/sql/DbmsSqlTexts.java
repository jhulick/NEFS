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
package com.netspective.axiom.sql;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.netspective.axiom.DatabasePolicies;
import com.netspective.axiom.DatabasePolicy;
import com.netspective.commons.text.ExpressionText;
import com.netspective.commons.text.ValueSourceOrJavaExpressionText;

public class DbmsSqlTexts implements Cloneable
{
    private Object owner;
    private String ownerVarName;
    private Map byDbms = new HashMap();

    public DbmsSqlTexts(Object instance, String ownerVarName)
    {
        this.owner = instance;
        this.ownerVarName = ownerVarName;
    }

    public DbmsSqlTexts getCopy()
    {
        DbmsSqlTexts result = new DbmsSqlTexts(owner, ownerVarName);
        result.byDbms.putAll(byDbms);
        return result;
    }

    public Map createVarsMap()
    {
        Map jexlVars = new HashMap();
        jexlVars.put(ownerVarName, owner);
        return jexlVars;
    }

    public Object getOwner()
    {
        return owner;
    }

    public String getOwnerVarName()
    {
        return ownerVarName;
    }

    public ExpressionText createExpr(DbmsSqlText sqlText, String sql)
    {
        return new ValueSourceOrJavaExpressionText(sql, createVarsMap());
    }

    public DbmsSqlText create()
    {
        return new DbmsSqlText(this);
    }

    public void add(DbmsSqlText expr)
    {
        byDbms.put(expr.getDbms(), expr);
    }

    public void merge(DbmsSqlTexts exprs)
    {
        byDbms.putAll(exprs.byDbms);
    }

    public DbmsSqlText removeByDbms(String dbmsId)
    {
        return (DbmsSqlText) byDbms.remove(dbmsId);
    }

    public DbmsSqlText getByDbms(DatabasePolicy dbPolicy)
    {
        return (DbmsSqlText) byDbms.get(dbPolicy.getDbmsIdentifier());
    }

    public DbmsSqlText getByDbmsOrAnsi(DatabasePolicy dbPolicy)
    {
        DbmsSqlText text = getByDbms(dbPolicy);
        if(text != null)
            return text;
        else
            return getByDbms(DatabasePolicies.DBPOLICY_ANSI);
    }

    public DbmsSqlText getByDbmsId(String dbmsId)
    {
        return (DbmsSqlText) byDbms.get(dbmsId);
    }

    public Set getAvailableDbmsIds()
    {
        return byDbms.keySet();
    }

    public int size()
    {
        return byDbms.size();
    }

    public String toString()
    {
        return byDbms.toString();
    }
}
