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
package com.netspective.commons.value;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections.LRUMap;
import org.apache.commons.discovery.tools.DiscoverClass;
import org.apache.commons.discovery.tools.DiscoverSingleton;
import org.apache.commons.lang.exception.NestableRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.netspective.commons.metric.CountMetric;
import com.netspective.commons.metric.Metric;
import com.netspective.commons.metric.MetricsGroup;
import com.netspective.commons.metric.MetricsProducer;
import com.netspective.commons.value.exception.UnexpectedValueContextException;
import com.netspective.commons.value.exception.ValueSourceInitializeException;
import com.netspective.commons.value.exception.ValueSourceNotFoundException;
import com.netspective.commons.value.source.ExceptionValueSource;
import com.netspective.commons.value.source.FilesystemEntriesValueSource;
import com.netspective.commons.value.source.GloballyUniqueIdValueSource;
import com.netspective.commons.value.source.JavaExpressionValueSource;
import com.netspective.commons.value.source.RedirectValueSource;
import com.netspective.commons.value.source.ScriptValueSource;
import com.netspective.commons.value.source.StaticListValueSource;
import com.netspective.commons.value.source.StaticValueSource;
import com.netspective.commons.value.source.SystemPropertyValueSource;
import com.netspective.commons.value.source.ValueSrcExpressionValueSource;

public class ValueSources implements MetricsProducer
{
    private static DiscoverClass discoverClass = new DiscoverClass();
    protected static final Log log = LogFactory.getLog(ValueSources.class);

    public static final String VSMETHODNAME_GETIDENTIFIERS = "getIdentifiers";
    public static final String VSMETHODNAME_GETDOCUMENTATION = "getDocumentation";

    public static final int VSNOTFOUNDHANDLER_NULL = 1;
    public static final int VSNOTFOUNDHANDLER_ERROR_VS = 2;
    public static final int VSNOTFOUNDHANDLER_THROW_EXCEPTION = 3;

    protected static final int VS_INSTANCES_LRU_MAP_MAX_SIZE = 4096;

    private static ValueSources instance = (ValueSources) DiscoverSingleton.find(ValueSources.class, ValueSources.class.getName());

    private Map srcClassesMap;
    private Map srcInstancesMap;
    private Set srcClassesSet;

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
        srcClassesMap = createSourceClassesMap();
        srcClassesSet = createSourceClassesSet();
        srcInstancesMap = createSourceInstancesMap();
        registerDefaultValueSources();
    }

    public void produceMetrics(Metric parent)
    {
        parent.addValueMetric("Value Source Classes", Integer.toString(srcClassesSet.size()));
        MetricsGroup instancesMetrics = parent.addGroupMetric("Value Source Instances");
        for (Iterator i = srcInstancesMap.values().iterator(); i.hasNext();)
        {
            ValueSource instance = (ValueSource) i.next();
            CountMetric instanceMetric = instancesMetrics.addCountMetric(instance.getClass().getName());
            instanceMetric.incrementCount();
        }
    }

    protected Map createSourceClassesMap()
    {
        return new TreeMap();
    }

    protected Set createSourceClassesSet()
    {
        return new HashSet();
    }

    protected Map createSourceInstancesMap()
    {
        return new LRUMap(VS_INSTANCES_LRU_MAP_MAX_SIZE);
    }

    protected void registerDefaultValueSources()
    {
        registerValueSource(ValueSrcExpressionValueSource.class);
        registerValueSource(JavaExpressionValueSource.class);
        registerValueSource(FilesystemEntriesValueSource.class);
        registerValueSource(GloballyUniqueIdValueSource.class);
        registerValueSource(StaticValueSource.class);
        registerValueSource(StaticListValueSource.class);
        registerValueSource(SystemPropertyValueSource.class);
        registerValueSource(RedirectValueSource.class);
        registerValueSource(ScriptValueSource.class);
    }

    public Map getValueSourceClassesMap()
    {
        return srcClassesMap;
    }

    public Set getValueSourceClassesSet()
    {
        return srcClassesSet;
    }

    public Map getValueSourceInstancesMap()
    {
        return srcInstancesMap;
    }

    public void registerValueSource(Class vsClass)
    {
        Class actualClass = discoverClass.find(vsClass, vsClass.getName());
        String[] identifiers = getValueSourceIdentifiers(actualClass);
        for (int i = 0; i < identifiers.length; i++)
        {
            srcClassesMap.put(identifiers[i], actualClass);
            if (log.isTraceEnabled())
                log.trace("Registered value source " + actualClass.getName() + " as '" + identifiers[i] + "'.");
        }
        srcClassesSet.add(actualClass);
    }

    /**
     * Gets all the identifiers available for a value source class
     *
     * @param vsClass
     *
     * @return
     */
    public String[] getValueSourceIdentifiers(Class vsClass)
    {
        Method getIdsMethod = null;
        try
        {
            getIdsMethod = vsClass.getMethod(VSMETHODNAME_GETIDENTIFIERS, null);
        }
        catch (NoSuchMethodException e)
        {
            log.error("Error retrieving method " + VSMETHODNAME_GETIDENTIFIERS, e);
            throw new NestableRuntimeException("Static method 'String[] " + VSMETHODNAME_GETIDENTIFIERS + "()' not found in value source " + vsClass.getName(), e);
        }

        try
        {
            return (String[]) getIdsMethod.invoke(null, null);
        }
        catch (Exception e)
        {
            log.error("Error executing method " + VSMETHODNAME_GETIDENTIFIERS, e);
            throw new NestableRuntimeException("Exception while obtaining identifiers using 'String[] " + VSMETHODNAME_GETIDENTIFIERS + "()' method in value source " + vsClass.getName(), e);
        }
    }

    /**
     * Gets all available indentifiers for all registered value source classes
     *
     * @return a string array containing all the identifiers
     */
    public String[] getAllValueSourceIdentifiers()
    {
        String[] iArray = null;
        if (srcClassesMap != null && srcClassesMap.size() > 0)
        {
            int i = 0;
            Iterator iset = srcClassesMap.keySet().iterator();
            iArray = new String[srcClassesMap.keySet().size()];
            while (iset.hasNext())
            {
                iArray[i] = (String) iset.next();
                i++;
            }
        }
        return iArray;
    }

    /**
     * Gets the documentation associated with a value source class
     *
     * @param vsClass
     *
     * @return the value source class documentation
     */
    public ValueSourceDocumentation getValueSourceDocumentation(Class vsClass)
    {
        Method getDocsMethod = null;
        try
        {
            getDocsMethod = vsClass.getMethod(VSMETHODNAME_GETDOCUMENTATION, null);
        }
        catch (NoSuchMethodException e)
        {
            return null;
        }

        try
        {
            return (ValueSourceDocumentation) getDocsMethod.invoke(null, null);
        }
        catch (Exception e)
        {
            log.error("Error executing method " + VSMETHODNAME_GETDOCUMENTATION + " in value source " + vsClass.getName(), e);
            return null;
        }
    }

    public Set getValueSourceAliases(String identifier)
    {
        Set result = new TreeSet();
        Class cls = (Class) getValueSourceClassesMap().get(identifier);
        if (cls != null)
        {
            String[] identifiers = getValueSourceIdentifiers(cls);
            for (int i = 0; i < identifiers.length; i++)
                if (!identifiers[i].equals(identifier))
                    result.add(identifiers[i]);
        }

        return result;
    }

    public final void assertValueContextInstance(Class expected, ValueContext vc, ValueSource vs) throws UnexpectedValueContextException
    {
        if (!expected.isAssignableFrom(vc.getClass()) || null == vc || null == expected)
        {
            UnexpectedValueContextException e = new UnexpectedValueContextException(expected, vc, vs);
            log.error("Invalid value context instance", e);
            throw e;
        }
    }

    public final ExceptionValueSource createExceptionValueSource(Throwable t)
    {
        log.error("ValueSource exception", t);
        return new ExceptionValueSource(t);
    }

    public final ValueContext createDefaultValueContext()
    {
        return new DefaultValueContext();
    }

    public int getUsageCount(Class vsClass)
    {
        if (vsClass.equals(StaticValueSource.class))
            return StaticValueSource.getUsageCount();
        else
        {
            int count = 0;
            for (Iterator i = srcInstancesMap.values().iterator(); i.hasNext();)
            {
                ValueSource instance = (ValueSource) i.next();
                if (instance.getClass() == vsClass)
                    count++;
            }
            return count;
        }
    }

    /* ------------------------------------------------------------------------------------------------------------- */

    public ValueSource getValueSource(ValueSourceSpecification vss, int notFoundHandlerType, boolean cacheInstance) throws ValueSourceNotFoundException, ValueSourceInitializeException
    {
        ValueSource vs;
        String idOrClassName = vss.getIdOrClassName();
        // if the id or class name is null, then the specification did not contain a valid value source string
        if (idOrClassName == null)
            return null;
        Class vsClass = (Class) srcClassesMap.get(idOrClassName);
        try
        {
            // if no identifier was registered, then see if it's a custom class
            if (vsClass == null)
            {
                vsClass = Class.forName(idOrClassName);
                vss.setCustomClass(true);
            }
        }
        catch (ClassNotFoundException cnfe)
        {
            vsClass = null;
        }

        if (vsClass == null)
        {
            switch (notFoundHandlerType)
            {
                case VSNOTFOUNDHANDLER_NULL:
                    return null;

                case VSNOTFOUNDHANDLER_ERROR_VS:
                    return new StaticValueSource("Value source '" + idOrClassName + "' class not found in '" + vss.getSpecificationText() + "'.");

                case VSNOTFOUNDHANDLER_THROW_EXCEPTION:
                    throw new ValueSourceNotFoundException(vss);
            }
        }

        try
        {
            vs = (ValueSource) vsClass.newInstance();
            vs.initialize(vss);

            if (cacheInstance)
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
        if (vs != null)
            return vs;
        else
        {
            ValueSourceSpecification vst = createSpecification(source);
            if (vst.isValid())
                return getValueSource(vst, notFoundHandlerType, true);
            else
                return null;
        }
    }

    public ValueSource createValueSource(String source, int notFoundHandlerType) throws ValueSourceInitializeException
    {
        ValueSourceSpecification vst = createSpecification(source);
        if (vst.isValid())
            return getValueSource(vst, notFoundHandlerType, false);
        else
            return null;
    }

    public ValueSource getValueSourceOrStatic(String source)
    {
        ValueSource vs = (ValueSource) srcInstancesMap.get(source);
        if (vs != null)
            return vs;
        else
        {
            ValueSourceSpecification vst = createSpecification(source);
            if (vst.isValid())
            {
                try
                {
                    return getValueSource(vst, VSNOTFOUNDHANDLER_NULL, true);
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

    public ValueSource createValueSourceOrStatic(String source)
    {
        ValueSourceSpecification vst = createSpecification(source);
        if (vst.isValid())
        {
            try
            {
                return getValueSource(vst, VSNOTFOUNDHANDLER_NULL, false);
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