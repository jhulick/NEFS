/*
 * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
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
 *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
 *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
 *    without written consent of Netspective.
 *
 * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
 *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
 * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
 * ARE HEREBY DISCLAIMED.
 *
 * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
 * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
 * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * @author Shahid N. Shah
 */

/**
 * $Id: ValueSources.java,v 1.3 2003-03-16 17:03:52 shahid.shah Exp $
 */

package com.netspective.commons.value;

import java.util.*;
import java.lang.reflect.Method;

import org.apache.commons.discovery.tools.DiscoverSingleton;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.exception.NestableRuntimeException;

import com.netspective.commons.value.source.ExpressionValueSource;
import com.netspective.commons.value.source.StaticValueSource;
import com.netspective.commons.value.source.ExceptionValueSource;
import com.netspective.commons.value.source.StaticListValueSource;
import com.netspective.commons.value.source.FilesystemEntriesValueSource;
import com.netspective.commons.value.source.GloballyUniqueIdValueSource;
import com.netspective.commons.value.source.SystemPropertyValueSource;
import com.netspective.commons.value.exception.ValueSourceNotFoundException;
import com.netspective.commons.value.exception.ValueSourceInitializeException;
import com.netspective.commons.value.exception.UnexpectedValueContextException;

public class ValueSources
{
    protected static final Log log = LogFactory.getLog(ValueSources.class);

    public static final String VSMETHODNAME_GETIDENTIFIERS = "getIdentifiers";

    public static final int VSNOTFOUNDHANDLER_NULL = 1;
    public static final int VSNOTFOUNDHANDLER_ERROR_VS = 2;
    public static final int VSNOTFOUNDHANDLER_THROW_EXCEPTION = 3;

    private static ValueSources instance = (ValueSources) DiscoverSingleton.find(ValueSources.class, ValueSources.class.getName());
    private Map srcClassesMap = new HashMap();
    private Map srcInstancesMap = new HashMap();
    private Set srcClassesSet = new HashSet();

    public static final ValueSources getInstance()
    {
        return instance;
    }

    public static final ValueSourceSpecification createSpecification(String text)
    {
        return new ValueSourceSpecification(text);
    }

    public ValueSources()
    {
        registerValueSource(ExpressionValueSource.class);
        registerValueSource(FilesystemEntriesValueSource.class);
        registerValueSource(GloballyUniqueIdValueSource.class);
        registerValueSource(StaticValueSource.class);
        registerValueSource(StaticListValueSource.class);
        registerValueSource(SystemPropertyValueSource.class);
    }

    public Map getValueSourceClassesMap()
    {
        return srcClassesMap;
    }

    public Set getValueSourceClassesSet()
    {
        return srcClassesSet;
    }

    public void registerValueSource(Class valueSource)
    {
        Method getIdsMethod = null;
        try
        {
            getIdsMethod = valueSource.getMethod(VSMETHODNAME_GETIDENTIFIERS, null);
        }
        catch (NoSuchMethodException e)
        {
            log.error(e);
            throw new NestableRuntimeException("Static method 'String[] "+ VSMETHODNAME_GETIDENTIFIERS +"()' not found in value source " + valueSource.getName(), e);
        }

        try
        {
            String[] identifiers = (String[]) getIdsMethod.invoke(null, null);
            for(int i = 0; i < identifiers.length; i++)
                srcClassesMap.put(identifiers[i], valueSource);
            srcClassesSet.add(valueSource);
        }
        catch (Exception e)
        {
            log.error(e);
            throw new NestableRuntimeException("Exception while obtaining identifiers using 'String[] "+ VSMETHODNAME_GETIDENTIFIERS +"()' method in value source " + valueSource.getClass().getName(), e);
        }
    }

    public final void assertValueContextInstance(Class expected, ValueContext vc, ValueSource vs) throws UnexpectedValueContextException
    {
        if(! expected.isAssignableFrom(vc.getClass()))
        {
            UnexpectedValueContextException e = new UnexpectedValueContextException(expected, vc, vs);
            log.error(e);
            throw e;
        }
    }

    public final ExceptionValueSource createExceptionValueSource(Throwable t)
    {
        log.error(t);
        return new ExceptionValueSource(t);
    }

    public final ValueContext createDefaultValueContext()
    {
        return new DefaultValueContext();
    }

    /* ------------------------------------------------------------------------------------------------------------- */

    public ValueSource getValueSource(ValueSourceSpecification vss, int notFoundHandlerType) throws ValueSourceNotFoundException, ValueSourceInitializeException
    {
        ValueSource vs;
        String idOrClassName = vss.getIdOrClassName();
        Class vsClass = (Class) srcClassesMap.get(idOrClassName);
        try
        {
            // if no identifier was registered, then see if it's a custom class
            if(vsClass == null)
            {
                vsClass = Class.forName(idOrClassName);
                vss.setCustomClass(true);
            }
        }
        catch(ClassNotFoundException cnfe)
        {
            vsClass = null;
        }

        if(vsClass == null)
        {
            switch(notFoundHandlerType)
            {
                case VSNOTFOUNDHANDLER_NULL:
                    return null;

                case VSNOTFOUNDHANDLER_ERROR_VS:
                    return new StaticValueSource("Value source '" + idOrClassName + "' class not found in '"+ vss.getSpecificationText()  +"'.");

                case VSNOTFOUNDHANDLER_THROW_EXCEPTION:
                    throw new ValueSourceNotFoundException(vss);
            }
        }

        try
        {
            vs = (ValueSource) vsClass.newInstance();
            vs.initialize(vss);
            srcInstancesMap.put(vs.getSpecification().getSpecificationText(), vs);
        }
        catch (InstantiationException e)
        {
            vs = createExceptionValueSource(e);
        }
        catch (IllegalAccessException e)
        {
            vs = createExceptionValueSource(e);
        }

        return vs;
    }

    public ValueSource getValueSource(String source, int notFoundHandlerType) throws ValueSourceInitializeException
    {
        ValueSource vs = (ValueSource) srcInstancesMap.get(source);
        if(vs != null)
            return vs;
        else
        {
            ValueSourceSpecification vst = createSpecification(source);
            if(vst.isValid())
                return getValueSource(vst, notFoundHandlerType);
            else
                return null;
        }
    }

    public ValueSource getValueSourceOrStatic(String source)
    {
        ValueSourceSpecification vst = createSpecification(source);
        if(vst.isValid())
        {
            try
            {
                return getValueSource(vst, VSNOTFOUNDHANDLER_NULL);
            }
            catch (ValueSourceInitializeException e)
            {
                return createExceptionValueSource(e);
            }
        }
        else
            return vst.getStaticValueSource();
    }
}