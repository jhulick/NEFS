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
 * $Id: MockScheduleElementProvider.java,v 1.1 2004-03-29 04:34:21 shahid.shah Exp $
 */

package com.netspective.commons.schedule.mock;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.netspective.commons.schedule.CalendarUtils;
import com.netspective.commons.schedule.impl.DefaultScheduleEvents;
import com.netspective.commons.schedule.impl.DefaultScheduleTemplate;
import com.netspective.commons.schedule.impl.DefaultScheduleTemplates;
import com.netspective.commons.schedule.model.ScheduleEventProvider;
import com.netspective.commons.schedule.model.ScheduleEvents;
import com.netspective.commons.schedule.model.ScheduleManager;
import com.netspective.commons.schedule.model.ScheduleParticipantProvider;
import com.netspective.commons.schedule.model.ScheduleParticipantTypes;
import com.netspective.commons.schedule.model.ScheduleParticipants;
import com.netspective.commons.schedule.model.ScheduleTemplateProvider;
import com.netspective.commons.schedule.model.ScheduleTemplates;
import com.netspective.commons.set.IntSpan;

public class MockScheduleElementProvider implements ScheduleEventProvider, ScheduleTemplateProvider, ScheduleParticipantProvider
{
    public static final int[][] MOCK_EVENT_HOURS = new int[][]
    {
        {  9,  0,  9, 30 }, // 09:00 to 09:30 AM
        { 10, 15, 11, 00 }, // 10:15 to 11:00 AM
        { 13, 30, 14, 30 }, // 01:30 to 02:30 PM
        { 14, 30, 14, 45 }, // 02:30 to 02:45 PM
        { 15, 15, 16, 00 }  // 03:15 to 04:00 PM
    };
    
    public ScheduleEvents getScheduledEvents(ScheduleManager scheduleManager, Date beginDate, Date endDate)
    {
        CalendarUtils calendarUtils = scheduleManager.getCalendarUtils();
        DefaultScheduleEvents result = new DefaultScheduleEvents();

        int beginDay = calendarUtils.getJulianDay(beginDate);
        int endDay = calendarUtils.getJulianDay(endDate);

        for(int day = beginDay; day <= endDay; day++)
        {
            Date julianDate = calendarUtils.getDateFromJulianDay(day);

            for(int i = 0; i < MOCK_EVENT_HOURS.length; i++)
            {
                int[] hm = MOCK_EVENT_HOURS[i];
                result.addEvent(
                        new MockScheduleEvent(scheduleManager, calendarUtils.createDate(julianDate, hm[0], hm[1]),
                                                               calendarUtils.createDate(julianDate, hm[2], hm[3]))
                );
            }
        }

        return result;
    }

    public ScheduleTemplates getScheduleTemplates(ScheduleManager scheduleManager, Date beginDate, Date endDate, ScheduleParticipants participants)
    {
        CalendarUtils calendarUtils = scheduleManager.getCalendarUtils();
        DefaultScheduleTemplates result = new DefaultScheduleTemplates();

        // create a standard template that will work from 9 to 5 on weekdays with no other restrictions (normal workday)
        result.addTemplate(new DefaultScheduleTemplate(null, scheduleManager, participants, null, null, null, true,
                beginDate, endDate, calendarUtils.createDate(beginDate, 9, 0), calendarUtils.createDate(beginDate, 16, 59, 59),
                null, null, null, new IntSpan(Calendar.MONDAY + "-" + Calendar.FRIDAY)
        ));

        // create an unavailable template that will work from 12p to 1p on weekdays with no other restrictions (lunch)
        result.addTemplate(new DefaultScheduleTemplate(null, scheduleManager, participants, null, null, null, false,
                beginDate, endDate, calendarUtils.createDate(beginDate, 12, 0), calendarUtils.createDate(beginDate, 12, 59, 59),
                null, null, null, new IntSpan(Calendar.MONDAY + "-" + Calendar.FRIDAY)
        ));

        return result;
    }

    public ScheduleParticipants getScheduleParticipants(ScheduleManager scheduleManager, List identifiers)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ScheduleParticipants getScheduleParticipants(ScheduleManager scheduleManager, Object[] identifiers)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ScheduleParticipants getScheduleParticipants(ScheduleManager scheduleManager, List identifiers, ScheduleParticipantTypes participantTypes)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ScheduleParticipants getScheduleParticipants(ScheduleManager scheduleManager, Object[] identifiers, ScheduleParticipantTypes participantTypes)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ScheduleParticipantTypes getScheduleParticipantTypes(ScheduleManager scheduleManager)
    {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}