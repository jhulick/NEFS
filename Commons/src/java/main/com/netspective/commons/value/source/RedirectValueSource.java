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
package com.netspective.commons.value.source;

import com.netspective.commons.value.PresentationValue;
import com.netspective.commons.value.Value;
import com.netspective.commons.value.ValueContext;
import com.netspective.commons.value.ValueSource;
import com.netspective.commons.value.ValueSourceDocumentation;
import com.netspective.commons.value.ValueSourceSpecification;
import com.netspective.commons.value.ValueSources;
import com.netspective.commons.value.exception.ValueSourceInitializeException;

/**
 * A special value source wrapper that handles redirects -- URLs. It's basically a wrapper object that delegates its
 * calls to another value source by default but may be extended to perform URL rewriting and parameter scanning or
 * replacement.
 */
public class RedirectValueSource implements ValueSource
{
    public static final String[] IDENTIFIERS = new String[]{"redirect"};
    public static final ValueSourceDocumentation DOCUMENTATION = new ValueSourceDocumentation("A special value source wrapper that handles redirects.",
                                                                                              new ValueSourceDocumentation.Parameter[]
                                                                                              {
                                                                                                  new ValueSourceDocumentation.Parameter("redirect value", true, "")
                                                                                              });

    private ValueSource valueSource;

    public static String[] getIdentifiers()
    {
        return IDENTIFIERS;
    }

    public static ValueSourceDocumentation getDocumentation()
    {
        return DOCUMENTATION;
    }

    public RedirectValueSource()
    {
    }

    public RedirectValueSource(String url)
    {
        setValueSource(ValueSources.getInstance().getValueSourceOrStatic(url));
    }

    public void initialize(ValueSourceSpecification srcTokens) throws ValueSourceInitializeException
    {
        valueSource.initialize(srcTokens);
    }

    public ValueSource getValueSource()
    {
        return valueSource;
    }

    public void setValueSource(ValueSource valueSource)
    {
        this.valueSource = valueSource;
    }

    public String getUrl(ValueContext vc)
    {
        return getTextValue(vc);
    }



    /* --- DELEGATE THE REST OF THE CALLS FOR NOW --- */

    public PresentationValue.Items.Item getPresentationItem(ValueContext vc, String value)
    {
        return valueSource.getPresentationItem(vc, value);
    }

    public PresentationValue getPresentationValue(ValueContext vc)
    {
        return valueSource.getPresentationValue(vc);
    }

    public ValueSourceSpecification getSpecification()
    {
        return valueSource.getSpecification();
    }

    public String getTextValue(ValueContext vc)
    {
        return valueSource.getTextValue(vc);
    }

    public String getTextValueOrBlank(ValueContext vc)
    {
        return valueSource.getTextValueOrBlank(vc);
    }

    public String[] getTextValues(ValueContext vc)
    {
        return valueSource.getTextValues(vc);
    }

    public Value getValue(ValueContext vc)
    {
        return valueSource.getValue(vc);
    }

    public boolean hasValue(ValueContext vc)
    {
        return valueSource.hasValue(vc);
    }


}
