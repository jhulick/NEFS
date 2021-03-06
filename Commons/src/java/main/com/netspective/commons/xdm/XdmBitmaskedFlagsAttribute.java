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
package com.netspective.commons.xdm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.netspective.commons.text.TextUtils;
import com.netspective.commons.xdm.exception.DataModelException;
import com.netspective.commons.xdm.exception.UnsupportedBitmaskedFlagsAttributeValueException;

/**
 * Helper class for attributes that can take multiple flags from a set of a flags.
 */
public abstract class XdmBitmaskedFlagsAttribute implements Cloneable, Serializable
{
    private static final Log log = LogFactory.getLog(XdmBitmaskedFlagsAttribute.class);

    /**
     * If an aliasDescription has this text within it, it will be replaced at runtime with an auto-generated alias
     * comment from XmlDataModelSchema.
     */
    private static final String ALIAS_COMMENT_REPL_EXPR = "${xdmAttrDetailAliasComment}";

    public static final int ACCESS_XDM = 1;      // available via XML
    public static final int ACCESS_PRIVATE = 2;  // available only to Java (will be ignored in setValue(String))

    public static class FlagDefn
    {
        private int access;
        private String name;
        private int mask;
        private String description;
        private String aliasDescription;

        public FlagDefn(int access, String name, int mask)
        {
            this.access = access;
            this.name = name;
            this.mask = mask;
        }

        public FlagDefn(int access, String name, int mask, String description)
        {
            this.access = access;
            this.name = name;
            this.mask = mask;
            this.description = description;
            this.aliasDescription = description;
        }

        public FlagDefn(int access, String name, int mask, String description, String aliasDescription)
        {
            this.access = access;
            this.name = name;
            this.mask = mask;
            this.description = description;
            this.aliasDescription = aliasDescription;
        }

        public FlagDefn(String name, int mask)
        {
            this(ACCESS_XDM, name, mask);
        }

        public int getAccess()
        {
            return access;
        }

        public void setAccess(int access)
        {
            this.access = access;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public String getAliasDescription(String aliasComment)
        {
            return TextUtils.getInstance().replaceTextValues(aliasDescription, ALIAS_COMMENT_REPL_EXPR, aliasComment);
        }

        public void setAliasDescription(String aliasDescription)
        {
            this.aliasDescription = aliasDescription;
        }

        public int getMask()
        {
            return mask;
        }

        public void setMask(int mask)
        {
            this.mask = mask;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }
    }

    protected int flags = 0;
    protected Map flagDefnsByName;
    protected Map flagSetterXmlNodeNames;
    protected String flagDelimiter = "|";

    public String getFlagDelimiter()
    {
        return flagDelimiter;
    }

    public void setFlagDelimiter(String flagDelimiter)
    {
        this.flagDelimiter = flagDelimiter;
    }

    /**
     * This is the only method a subclass needs to implement.
     *
     * @return an array holding all possible values of the flags.
     */
    public abstract FlagDefn[] getFlagsDefns();

    public Map getFlagDefnsByName()
    {
        if(flagDefnsByName == null)
        {
            flagDefnsByName = new HashMap();
            flagSetterXmlNodeNames = new HashMap();

            FlagDefn[] flagDefns = getFlagsDefns();
            for(int i = 0; i < flagDefns.length; i++)
            {
                FlagDefn flagDefn = flagDefns[i];
                if(flagDefns == null)
                    throw new RuntimeException("Flags " + i + " in " + this.getClass().getName() + " is null");

                String flagName = flagDefn.getName();
                String javaId = TextUtils.getInstance().xmlTextToJavaIdentifier(flagName.toLowerCase(), false);
                String xmlNodeName = TextUtils.getInstance().javaIdentifierToXmlNodeName(javaId).toLowerCase();

                flagDefnsByName.put(flagName, flagDefns);
                flagDefnsByName.put(javaId, flagDefn);
                flagDefnsByName.put(xmlNodeName, flagDefn);

                if(flagDefn.access == ACCESS_XDM)
                    flagSetterXmlNodeNames.put(xmlNodeName, flagDefn);
            }
        }

        return flagDefnsByName;
    }

    public Map getFlagSetterXmlNodeNames()
    {
        if(flagSetterXmlNodeNames == null)
            getFlagDefnsByName();

        return flagSetterXmlNodeNames;
    }

    public XdmBitmaskedFlagsAttribute()
    {
    }

    public XdmBitmaskedFlagsAttribute cloneFlags()
    {
        try
        {
            return (XdmBitmaskedFlagsAttribute) super.clone();
        }
        catch(CloneNotSupportedException e)
        {
            log.error("Error cloning flags", e);
            return null;
        }
    }

    public XdmBitmaskedFlagsAttribute(int flags)
    {
        setFlag(flags);
    }

    public void setValue(String value, boolean clearFirst)
    {
        if(clearFirst)
        {
            this.flags = 0;
            if(value == null || value.length() == 0)
            {
                flagsChanged();
                return;
            }
        }

        String[] flagNames = TextUtils.getInstance().split(value, flagDelimiter, true);
        FlagDefn[] flagDefns = getFlagsDefns();
        for(int i = 0; i < flagNames.length; i++)
        {
            boolean set = true;
            String flagName = flagNames[i];
            if(flagName.charAt(0) == '~')
            {
                set = false;
                flagName = flagName.substring(1);
            }

            boolean found = false;
            for(int j = 0; j < flagDefns.length; j++)
            {
                FlagDefn flagDefn = flagDefns[j];
                if(flagDefn.name.equalsIgnoreCase(flagName))
                {
                    if(flagDefn.access == ACCESS_PRIVATE)
                        throw new RuntimeException("Attempting to set ACCESS_PRIVATE flag '" + flagDefn.name + "' from outside Java.");

                    found = true;
                    if(set)
                        this.flags |= flagDefn.mask;
                    else
                        this.flags &= ~flagDefn.mask;
                }
            }

            if(!found)
                throw new RuntimeException("Invalid " + this.getClass().getName() + " value: " + value + " (flag '" + flagName + "' not found)");
        }

        flagsChanged();
    }

    public void setValue(String value)
    {
        setValue(value, true);
    }

    public void setValue(XdmParseContext pc, Object element, String attribute, String value) throws DataModelException
    {
        try
        {
            setValue(value);
        }
        catch(RuntimeException e)
        {
            UnsupportedBitmaskedFlagsAttributeValueException bfae = new UnsupportedBitmaskedFlagsAttributeValueException(pc, this, element, attribute, value);
            pc.addError(bfae);
            if(pc.isThrowErrorException())
                throw e;
        }
    }

    public long getFlags()
    {
        return flags;
    }

    public boolean flagIsSet(long flag)
    {
        return (flags & flag) == 0 ? false : true;
    }

    public void setFlag(long flag)
    {
        flags |= flag;
        flagsChanged();
    }

    public void clearFlag(long flag)
    {
        flags &= ~flag;
        flagsChanged();
    }

    public void updateFlag(long flag, boolean set)
    {
        if(set) setFlag(flag); else clearFlag(flag);
    }

    public boolean updateFlag(String flagName, boolean set)
    {
        FlagDefn flagDefn = (FlagDefn) getFlagDefnsByName().get(flagName);
        if(flagDefn == null)
            return false;
        else
        {
            updateFlag(flagDefn.getMask(), set);
            return true;
        }
    }

    public void copy(XdmBitmaskedFlagsAttribute flags)
    {
        FlagDefn[] flagDefns = flags.getFlagsDefns();
        for(int i = 0; i < flagDefns.length; i++)
        {
            int copyMask = flagDefns[i].mask;
            updateFlag(copyMask, flags.flagIsSet(copyMask));
        }
    }

    /**
     * Inherit flags from the source
     *
     * @param flags   The source
     * @param inherit The flags to inherit (all other flags will remain unset)
     */
    public void inherit(XdmBitmaskedFlagsAttribute flags, int inherit)
    {
        FlagDefn[] flagDefns = flags.getFlagsDefns();
        for(int i = 0; i < flagDefns.length; i++)
        {
            int copyMask = flagDefns[i].mask;
            if((copyMask & inherit) != 0)
                updateFlag(copyMask, flags.flagIsSet(copyMask));
        }
    }

    public String[] getFlagNames()
    {
        FlagDefn[] flagDefns = getFlagsDefns();
        if(flagDefns == null)
            return new String[]{this.getClass().getName() + ".getFlagsDefns() is NULL"};

        String[] result = new String[flagDefns.length];
        for(int i = 0; i < flagDefns.length; i++)
            result[i] = flagDefns[i] != null
                        ? flagDefns[i].getName() : (this.getClass().getName() + ".getFlagsDefns()[" + i + "] is NULL");
        return result;
    }

    public String[] getFlagNamesWithXdmAccess()
    {
        FlagDefn[] flagDefns = getFlagsDefns();
        if(flagDefns == null)
            return new String[]{this.getClass().getName() + ".getFlagsDefns() is NULL"};

        List result = new ArrayList();
        for(int i = 0; i < flagDefns.length; i++)
        {
            if(flagDefns[i] != null && flagDefns[i].getAccess() != ACCESS_XDM)
                continue;
            result.add(flagDefns[i] != null
                       ? flagDefns[i].getName() : (this.getClass().getName() + ".getFlagsDefns()[" + i + "] is NULL"));
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    public String getFlagsText()
    {
        StringBuffer text = new StringBuffer();

        FlagDefn[] flagDefns = getFlagsDefns();
        for(int i = 0; i < flagDefns.length; i++)
        {
            if(flagDefns[i] != null)
            {
                if((flags & flagDefns[i].mask) != 0)
                {
                    if(text.length() > 0)
                        text.append(" " + flagDelimiter + " ");
                    text.append(flagDefns[i].getName());
                }
            }
            else
                text.append(this.getClass().getName() + " flagDefns[" + i + "] was null. ");
        }

        return text.toString();
    }

    public void flagsChanged()
    {

    }

    public String toString()
    {
        return flags + " [" + getFlagsText() + "]";
    }
}
