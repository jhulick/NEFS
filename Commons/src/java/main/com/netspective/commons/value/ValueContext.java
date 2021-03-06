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

import java.util.Date;
import java.util.Map;

import com.netspective.commons.RuntimeEnvironment;
import com.netspective.commons.acl.AccessControlListsManager;
import com.netspective.commons.config.ConfigurationsManager;
import com.netspective.commons.script.Script;
import com.netspective.commons.script.ScriptContext;
import com.netspective.commons.script.ScriptException;
import com.netspective.commons.script.ScriptsManager;
import com.netspective.commons.security.AuthenticatedUser;

public interface ValueContext extends RuntimeEnvironment
{
    /**
     * Returns the time the value context was created
     */
    public long getCreationTime();

    /**
     * Return the date/time the value context was created
     */
    public Date getCreationDate();

    /**
     * Returns the default configuration properties manager.
     */
    public ConfigurationsManager getConfigurationsManager();

    /**
     * Returns the default access control lists manager.
     */
    public AccessControlListsManager getAccessControlListsManager();

    /**
     * Returns the default scripts manager.
     */
    public ScriptsManager getScriptsManager();

    /**
     * Returns the currently logged-in user, if any (used to retrieve ACLs for a particular user)
     */
    public AuthenticatedUser getAuthenticatedUser();

    /**
     * Returns the value of the value-context attribute named attributeId.
     */
    public Object getAttribute(String attributeId);

    /**
     * Sets the value of the value-context attribute named attributeId. This
     * value is available only during the lifetime of the value context and
     * is not persistent.
     */
    public void setAttribute(String attributeId, Object attributeValue);

    /**
     * Removes the value of the value-context attribute named attributeId.
     */
    public void removeAttribute(String attributeId);

    /**
     * Set an application-specific locator for the value context -- useful for debugging state
     *
     * @param locator A user-defined URL, URI, or other useful locator
     */
    public void setContextLocation(Object locator);

    /**
     * Retrieves the active locator stored by setLocator() method
     */
    public Object getContextLocation();

    /**
     * Ascertains whether the given Java expression is true in this value context.
     *
     * @param expr A JSTL expression
     * @param vars Any additional variables that should be made available in expression (null if not required)
     *
     * @return True if the expression return a boolean and the boolean is true, false otherwise
     */
    public boolean isConditionalExpressionTrue(String expr, Map vars);

    /**
     * Evaluates the given Java expression in this value context and returns the value returned by the expression.
     *
     * @param expr A JSTL expression
     * @param vars Any additional variables that should be made available in expression (null if not required)
     *
     * @return The object returned by the expression
     */
    public Object evaluateExpression(String expr, Map vars);

    /**
     * Either create or return a previously created context to run the given script in this ValueContext
     *
     * @param script The script for which to create the context
     *
     * @return The ScriptContext that can be used to run this script for the given script
     */
    public ScriptContext getScriptContext(Script script) throws ScriptException;
}