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

import org.webcat.core.User;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;
import er.extensions.eof.ERXKey;

//-------------------------------------------------------------------------
/**
 * Represents a pair of a user together with a schedule sheet submission.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class UserSubmissionPair
    implements NSKeyValueCodingAdditions
{
    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    public UserSubmissionPair(User aUser, ScheduleSheetSubmission aSubmission)
    {
        this._user = aUser;
        this._submission = aSubmission;
    }


    //~ Public constants ......................................................

    public static final ERXKey<User> user =
        new ERXKey<User>("user");
    public static final ERXKey<ScheduleSheetSubmission> submission =
        new ERXKey<ScheduleSheetSubmission>("submission");


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    public User user()
    {
        return _user;
    }


    // ----------------------------------------------------------
    public ScheduleSheetSubmission submission()
    {
        return _submission;
    }


    // ----------------------------------------------------------
    public boolean userHasSubmission()
    {
        return _submission != null;
    }


    // ----------------------------------------------------------
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<" + user().toString() + ", ");

        if (submission() != null)
        {
            buffer.append(submission().toString());
        }
        else
        {
            buffer.append("no submission");
        }

        buffer.append(">");
        return buffer.toString();
    }


    // ----------------------------------------------------------
    public boolean equals(Object object)
    {
        if (object instanceof UserSubmissionPair)
        {
            UserSubmissionPair otherPair = (UserSubmissionPair) object;

            return (otherPair.user() == user()
                    && otherPair.submission() == submission());
        }
        else
        {
            return false;
        }
    }


    //~ KVC implementation ....................................................

    // ----------------------------------------------------------
    public void takeValueForKeyPath(Object value, String keyPath)
    {
        NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(
                this, value, keyPath);
    }


    // ----------------------------------------------------------
    public Object valueForKeyPath(String keyPath)
    {
        return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(
                this, keyPath);
    }


    // ----------------------------------------------------------
    public void takeValueForKey(Object value, String key)
    {
        NSKeyValueCoding.DefaultImplementation.takeValueForKey(
                this, value, key);
    }


    // ----------------------------------------------------------
    public Object valueForKey(String key)
    {
        return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
    }


    //~ Static/instance fields ................................................

    private User _user;
    private ScheduleSheetSubmission _submission;
}
