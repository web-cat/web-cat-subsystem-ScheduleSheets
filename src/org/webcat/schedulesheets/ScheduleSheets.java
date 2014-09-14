/*==========================================================================*\
 |  $Id$
 |*-------------------------------------------------------------------------*|
 |  Copyright (C) 2014 Virginia Tech
 |
 |  This file is part of Web-CAT.
 |
 |  Web-CAT is free software; you can redistribute it and/or modify
 |  it under the terms of the GNU Affero General Public License as published
 |  by the Free Software Foundation; either version 3 of the License, or
 |  (at your option) any later version.
 |
 |  Web-CAT is distributed in the hope that it will be useful,
 |  but WITHOUT ANY WARRANTY; without even the implied warranty of
 |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 |  GNU General Public License for more details.
 |
 |  You should have received a copy of the GNU Affero General Public License
 |  along with Web-CAT; if not, see <http://www.gnu.org/licenses/>.
\*==========================================================================*/

package org.webcat.schedulesheets;

import org.apache.log4j.Logger;
import org.webcat.core.Subsystem;
import org.webcat.woextensions.WCEC;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;

//-------------------------------------------------------------------------
/**
 * A subsystem to support schedule sheet assignments, where students enter
 * estimates of the time required to complete an associated programming
 * assignment.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class ScheduleSheets
    extends Subsystem
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new ScheduleSheets subsystem object.
     */
    public ScheduleSheets()
    {
        super();
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    @Override
    public void start()
    {
        super.start();
        alerter = new Alerter();
        alerter.start();
    }


    // ----------------------------------------------------------
    public static void pingAlerter()
    {
        if (alerter != null)
        {
            alerter.ping();
        }
    }


    // ----------------------------------------------------------
    public class Alerter
        extends Thread
    {
        private boolean sleeping = false;


        // ----------------------------------------------------------
        public void ping()
        {
            if (sleeping)
            {
                interrupt();
            }
        }


        // ----------------------------------------------------------
        @Override
        public void run()
        {
            EOEditingContext ec = null;
            while (true)
            {
                long sleepTime = 0;
                if (ec == null)
                {
                    ec = WCEC.newEditingContext();
                }
                ec.lock();
                try
                {
                    NSTimestamp now = new NSTimestamp();
                    NSArray<EmailAlertForAssignmentOffering> offerings =
                        EmailAlertForAssignmentOffering
                        .objectsMatchingQualifier(ec,
                        EmailAlertForAssignmentOffering.sent.isFalse().and(
                        EmailAlertForAssignmentOffering.sendTime.before(now)),
                        EmailAlertForAssignmentOffering.sendTime.ascs());

                    if (offerings.count() == 0)
                    {
                        sleepTime = 1000 * 60 * 60 * 24;
                    }
                    else
                    {
                        for (EmailAlertForAssignmentOffering o : offerings)
                        {
                            log.debug("sending email alert for "
                                + o.alertForAssignment().alertGroup()
                                    .assignment()
                                + " in "
                                + o.courseOffering().compactName());
                            o.send();
                        }
                    }
                }
                catch (Exception e)
                {
                    log.error("Unexpected exception in Alerter", e);
                    if (ec != null)
                    {
                        try
                        {
                            ec.unlock();
                            ec.dispose();
                        }
                        catch (Exception ee)
                        {
                            log.error("Exception cleaning up bad editing "
                                + "context", ee);
                        }
                    }
                    ec = null;
                }
                finally
                {
                    if (ec != null)
                    {
                        ec.unlock();
                    }
                }

                // sleep until next item becomes available
                if (sleepTime > 0)
                {
                    sleeping = true;
                    try
                    {
                        sleep(sleepTime);
                    }
                    catch (InterruptedException e)
                    {
                        // ignore
                    }
                    finally
                    {
                        sleeping = false;
                    }
                }
            }
        }
    }


    //~ Instance/static fields ................................................

    private static Alerter alerter;
    static Logger log = Logger.getLogger(ScheduleSheets.class);
}
