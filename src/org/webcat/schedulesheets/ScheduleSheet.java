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

import org.webcat.core.Status;
import org.webcat.core.User;
import org.webcat.grader.SubmissionProfile;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSTimestamp;
import er.extensions.foundation.ERXArrayUtilities;

//-------------------------------------------------------------------------
/**
 * Represents the result produced by completing a schedule, together with
 * all feedback and scoring associated with it.  It includes a set of
 * SheetEntry objects representing the data corresponding to each
 * ComponentFeature in the schedule.  Analogous to the SubmissionResult
 * for a regular program assignment.
 *
 * @author  Stephen Edwards
 * @author  Last changed by $Author$
 * @version $Revision$, $Date$
 */
public class ScheduleSheet
    extends _ScheduleSheet
{
    //~ Constructor ...........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new ScheduleSheet object.
     */
    public ScheduleSheet()
    {
        super();
    }


    //~ Constants .............................................................

    public static final byte FORMAT_TEXT =
        org.webcat.grader.SubmissionResult.FORMAT_TEXT;
    public static final byte FORMAT_HTML =
        org.webcat.grader.SubmissionResult.FORMAT_TEXT;


    //~ Public Methods ........................................................

    // ----------------------------------------------------------
    public void setupFrom(ScheduleSheet other, boolean newAssignment)
    {
        for (ComponentFeature cf : other.componentFeatures())
        {
            ComponentFeature myCf = ComponentFeature.create(editingContext());
            myCf.setupFrom(cf, newAssignment);
            addToComponentFeaturesRelationship(myCf);
        }
    }


    // ----------------------------------------------------------
    @SuppressWarnings("unchecked")
    public NSArray<SheetFeedbackItem> nontransientFeedback()
    {
        return ERXArrayUtilities.filteredArrayWithQualifierEvaluation(
            feedbackItems(),
            SheetFeedbackItem.isTransient.isFalse());
    }


    // ----------------------------------------------------------
    public void moveNonTransientToTransient()
    {
        for (SheetFeedbackItem i : nontransientFeedback())
        {
            i.setIsTransient(true);
        }
    }


    // ----------------------------------------------------------
    public boolean isOverdue()
    {
        for (ComponentFeature cf : componentFeatures())
        {
            if (cf.isOverdue())
            {
                return true;
            }
        }
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the primary submission associated with this result.
     * The primary submission is the one associated with the student
     * who actually made the submission, as opposed to one of the
     * partners associated with this submission.
     * @return The submission from the primary submitter.
     */
    public ScheduleSheetSubmission submission()
    {
        ScheduleSheetSubmission result = null;
        NSArray<ScheduleSheetSubmission> mySubmissions = submissions();
        if (mySubmissions != null && mySubmissions.count() > 0)
        {
            result = mySubmissions.objectAtIndex(0);
            ScheduleSheetSubmission primary = result.primarySubmission();

            if (primary != null)
            {
                result = primary;
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the submission associated with this result for the
     * given user.
     * @param partner The user whose submission should be retrieved--either
     *                the primary submitter or one of the partners.
     * @return The submission associated with the given user (partner) and
     *         this result, or the primary submission if this user does
     *         not have any submission for this result.
     */
    public ScheduleSheetSubmission submissionFor(User partner)
    {
        for (ScheduleSheetSubmission sub : submissions())
        {
            if (sub.user() == partner)
            {
                return sub;
            }
        }
        return submission();
    }


    // ----------------------------------------------------------
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accessibleByUser(User user)
    {
        for (ScheduleSheetSubmission sub : submissions())
        {
            if (sub.accessibleByUser(user))
            {
                return true;
            }
        }
        return false;
    }


    // ----------------------------------------------------------
    /**
     * Computes the early bonus for this submission.  The bonus
     * is a positive amount added to the raw score.
     *
     * @return the bonus amount, or 0.0 if none
     */
    public double earlyBonus()
    {
        double earlyBonus = 0.0;
        ScheduleSheetSubmission submission = submission();
        long submitTime = submission.submitTime().getTime();
        long dueTime = submission.assignmentOffering().dueDate().getTime();
        SubmissionProfile profile =
            submission.assignmentOffering().assignment().submissionProfile();

        if ( profile.awardEarlyBonus()
             && dueTime > submitTime
             && profile.earlyBonusUnitTimeRaw() != null)
        {
            // Early bonus
            //
            long  earlyBonusUnitTime = profile.earlyBonusUnitTime();
            long  earlyTime  = dueTime - submitTime;
            float earlyUnits = earlyTime / earlyBonusUnitTime;
            earlyBonus = earlyUnits * profile.earlyBonusUnitPts();
            if ( profile.earlyBonusMaxPtsRaw() != null
                 && earlyBonus > profile.earlyBonusMaxPts() )
            {
                earlyBonus = profile.earlyBonusMaxPts();
            }
        }
        return earlyBonus;
    }


    // ----------------------------------------------------------
    /**
     * Computes the late penalty for this submission.  The penalty
     * is a positive amount subtracted from the raw score.
     *
     * @return the penalty amount, or 0.0 if none
     */
    public double latePenalty()
    {
        double latePenalty = 0.0;
        ScheduleSheetSubmission submission = submission();
        long submitTime = submission.submitTime().getTime();
        long dueTime = submission.assignmentOffering().dueDate().getTime();
        SubmissionProfile profile =
            submission.assignmentOffering().assignment().submissionProfile();

        if ( profile.deductLatePenalty()
             && dueTime < submitTime
             && profile.latePenaltyUnitTimeRaw() != null)
        {
            // Late penalty
            //
            long latePenaltyUnitTime = profile.latePenaltyUnitTime();
            long lateTime  = submitTime - dueTime;
            long lateUnits = (long)java.lang.Math.ceil(
                ( (double)lateTime ) / (double)latePenaltyUnitTime );
            latePenalty = lateUnits * profile.latePenaltyUnitPts();
            if ( profile.latePenaltyMaxPtsRaw() != null
                 && latePenalty > profile.latePenaltyMaxPts() )
            {
                latePenalty = profile.latePenaltyMaxPts();
            }
        }
        return latePenalty;
    }


    // ----------------------------------------------------------
    /**
     * Computes the excess submission penalty for this submission.  The penalty
     * is a positive amount subtracted from the raw score.
     *
     * @return the penalty amount, or 0.0 if none
     */
    public double excessSubmissionPenalty()
    {
        double penalty = 0.0;
        ScheduleSheetSubmission submission = submission();
        int number = submission.submitNumber();
        SubmissionProfile profile =
            submission.assignmentOffering().assignment().submissionProfile();

        if (profile.deductExcessSubmissionPenalty()
            && profile.excessSubmissionsUnitPtsRaw() != null
            && number > profile.excessSubmissionsThreshold())
        {
            int over = number - profile.excessSubmissionsThreshold();
            int unitSize = profile.excessSubmissionsUnitSize();
            if (unitSize == 0)
            {
                unitSize = 1;
            }
            int units = over / unitSize;
            if (unitSize > 1 && over % unitSize > 0)
            {
                units++;
            }
            penalty = units * profile.excessSubmissionsUnitPts();
            if (profile.excessSubmissionsMaxPtsRaw() != null
                && penalty > profile.excessSubmissionsMaxPts())
            {
                penalty = profile.excessSubmissionsMaxPts();
            }
        }
        return penalty;
    }


    // ----------------------------------------------------------
    /**
     * Computes the combined bonus and/or late penalty for this
     * submission, if any.
     *
     * @return the bonus/penalty adjustment amount
     */
    public double scoreAdjustment()
    {
        return earlyBonus() - latePenalty() - excessSubmissionPenalty();
    }


    // ----------------------------------------------------------
    /**
     * Computes the raw score for this submission, viewable by students.
     * The raw score is the correctnessScore() plus the toolScore() plus
     * (if grading results are viewable by students) the taScore().
     *
     * @return the raw score
     */
    public double rawScoreForStudent()
    {
        double result = toolScore();
        if (status() == Status.CHECK)
        {
            result += taScore();
        }
        return (result >= 0.0) ? result : 0.0;
    }


    // ----------------------------------------------------------
    /**
     * Computes the raw score for this submission, viewable by staff.
     * The raw score is the correctnessScore() plus the toolScore() plus
     * the taScore().
     *
     * @return the raw score
     */
    public double rawScore()
    {
        double result = toolScore() + taScore();
        return (result >= 0.0) ? result : 0.0;
    }


    // ----------------------------------------------------------
    /**
     * Computes the raw score for this submission, as viewable by the
     * given user (either course staff or a student).
     *
     * @param user the user
     * @return the final score
     */
    public double rawScoreVisibleTo(User user)
    {
        if (user.hasAdminPrivileges() ||
            submission().assignmentOffering().courseOffering().isStaff(user))
        {
            return rawScore();
        }
        else
        {
            return rawScoreForStudent();
        }
    }


    // ----------------------------------------------------------
    /**
     * Computes the final score for this submission, viewable by students.
     * The final score is the rawScore() plus the earlyBonus() minus the
     * latePenalty().
     *
     * @return the final score
     */
    public double finalScoreForStudent()
    {
        double result = rawScoreForStudent() + earlyBonus() - latePenalty()
            - excessSubmissionPenalty();
        return (result >= 0.0) ? result : 0.0;
    }


    // ----------------------------------------------------------
    /**
     * Computes the final score for this submission, viewable by staff.
     * The final score is the rawScoreForStaff() plus the earlyBonus()
     * minus the latePenalty().
     *
     * @return the final score
     */
    public double finalScore()
    {
        double result = rawScore() + earlyBonus() - latePenalty()
            - excessSubmissionPenalty();
        return (result >= 0.0) ? result : 0.0;
    }


    // ----------------------------------------------------------
    /**
     * Computes the final score for this submission, viewable by the
     * given user (either course staff or a student).
     *
     * @param user the user
     * @return the final score
     */
    public double finalScoreVisibleTo(User user)
    {
        if (user.hasAdminPrivileges()
            || submission().assignmentOffering().courseOffering().isStaff(user))
        {
            return finalScore();
        }
        else
        {
            return finalScoreForStudent();
        }
    }


    // ----------------------------------------------------------
    public String scoreModifiers()
    {
        String result = null;
        double rawScore = finalScore();

        double earlyBonus = earlyBonus();
        if (earlyBonus > 0.0)
        {
            rawScore -= earlyBonus;
            result = " + " + earlyBonus + " early bonus";
        }

        double latePenalty = latePenalty();
        if (latePenalty > 0.0)
        {
            rawScore += latePenalty;
            result = (result == null ? "" : result)
                + " - " + latePenalty + " late penalty";
        }

        double excessSubmissionPenalty = excessSubmissionPenalty();
        if (excessSubmissionPenalty > 0.0)
        {
            rawScore += excessSubmissionPenalty;
            result = (result == null ? "" : result)
                + " - " + excessSubmissionPenalty
                + " excess submission penalty";
        }

        if (result != null)
        {
            result = "" + rawScore + result + " = ";
        }
        return result;
    }


    // ----------------------------------------------------------
    /**
     * Get the corresponding icon URL for this file's grading status.
     *
     * @return The image URL as a string
     */
    public String statusURL()
    {
        return Status.statusURL(status());
    }


    // ----------------------------------------------------------
    /**
     * Retrieve the score for this result that is used in graphs.  The score
     * for graphing includes the raw correctness score plus the raw static
     * analysis score, without any late penalty or TA manual grading
     * included.
     * @return the score
     */
    public double automatedScore()
    {
        return toolScore();
    }


    // ----------------------------------------------------------
    public void addCommentByLineFor(User commenter, String priorComments)
    {
        String newComments = comments();
        if (newComments != null
            && (newComments.trim().equals("<br />") || newComments.equals("")))
        {
            setComments(null);
            newComments = null;
        }
        if (status() == Status.TO_DO && newComments != null)
        {
            setStatus(Status.UNFINISHED);
        }
        if (newComments != null
            && newComments.indexOf("<") < 0
            && newComments.indexOf(">") < 0)
        {
            setCommentFormat(FORMAT_TEXT);
        }
        if (newComments != null && !newComments.equals(priorComments))
        {
            // update author info:
            String byLine = "-- last updated by " + commenter.name();
            if (commentFormat() == FORMAT_HTML)
            {
                byLine = "<p><span style=\"font-size:smaller\"><i>"
                    + byLine + "</i></span></p>";
            }
            if (log.isDebugEnabled())
            {
                log.debug("new comments ='" + newComments + "'");
                log.debug("byline ='" + byLine + "'");
            }
            if (!newComments.trim().endsWith(byLine))
            {
                log.debug("byLine not found");
                if (commentFormat() == FORMAT_TEXT)
                {
                    byLine = "\n" + byLine + "\n";
                }
                if (!(newComments.endsWith( "\n")
                      || newComments.endsWith("\r")))
                {
                    byLine = "\n" + byLine;
                }
                setComments(newComments + byLine);
            }
        }
    }


    // ----------------------------------------------------------
    /**
     * Change the value of this object's <code>lastUpdated</code>
     * property.
     *
     * @param value The new value for this property
     */
    @Override
    public void setLastUpdated(NSTimestamp value)
    {
        super.setLastUpdated(value);

        if (value != null)
        {
            boolean isNewest = true;
            for (ScheduleSheetSubmission sub : submissions())
            {
                if (sub.sheet() != null
                    && sub.sheet().lastUpdated() != null
                    && sub.sheet().lastUpdated().after(value))
                {
                    isNewest = false;
                    break;
                }
            }
            if (isNewest)
            {
                submission().markAsSubmissionForGrading();
            }
        }
    }


    //~ Instance/static fields ................................................

}
