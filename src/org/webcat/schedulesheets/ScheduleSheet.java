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

import java.util.HashMap;
import java.util.Map;
import org.webcat.core.Status;
import org.webcat.core.User;
import org.webcat.grader.SubmissionProfile;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
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
    implements org.webcat.grader.Scorable
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
        org.webcat.grader.SubmissionResult.FORMAT_HTML;
    public static final NSArray<Byte> FORMATS =
        org.webcat.grader.SubmissionResult.formats;
    public static final NSArray<String> FORMAT_STRINGS =
        org.webcat.grader.SubmissionResult.formatStrings;


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
    public NSArray<SheetFeedbackItem> currentFeedback()
    {
        if (currentFeedback == null)
        {
            currentFeedback = ERXArrayUtilities
                .filteredArrayWithQualifierEvaluation(
                    feedbackItems(),
                    SheetFeedbackItem.checkRound.is(numCheckRounds()));
        }
        return currentFeedback;
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
            submission.assignmentOffering().submissionProfile();

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
            submission.assignmentOffering().submissionProfile();

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
                submission().markBestSubmissionForGrading();
            }
        }
    }


    // ----------------------------------------------------------
    public boolean staffFeedbackIsReady()
    {
        return status() == org.webcat.core.Status.CHECK;
    }


    // ----------------------------------------------------------
    public ComponentFeature componentFeatureFor(String name)
    {
        for (ComponentFeature cf : componentFeatures())
        {
            if ((name == null && cf.name() == null)
                || (name != null && name.equals(cf.name())))
            {
                return cf;
            }
        }
        return null;
    }


    // ----------------------------------------------------------
    public SheetEntry entryFor(String name, byte activity)
    {
        for (ComponentFeature cf : componentFeatures())
        {
            if ((name == null && cf.name() == null)
                || (name != null && name.equals(cf.name())))
            {
                return cf.entryFor(activity);
            }
        }
        return null;
    }


    // ----------------------------------------------------------
    public double newEstimatedRemaining()
    {
        double result = 0.0;
        for (ComponentFeature cf : componentFeatures())
        {
            result += cf.newEstimatedRemaining();
        }
        return result;
    }


    // ----------------------------------------------------------
    public double previousEstimatedRemaining()
    {
        double result = 0.0;
        for (ComponentFeature cf : componentFeatures())
        {
            result += cf.previousEstimatedRemaining();
        }
        return result;
    }


    // ----------------------------------------------------------
    public double newEstimatedTotal()
    {
        double result = 0.0;
        for (ComponentFeature cf : componentFeatures())
        {
            result += cf.newEstimatedTotal();
        }
        return result;
    }


    // ----------------------------------------------------------
    public double previousEstimatedTotal()
    {
        double result = 0.0;
        for (ComponentFeature cf : componentFeatures())
        {
            result += cf.previousEstimatedTotal();
        }
        return result;
    }


    // ----------------------------------------------------------
    public double newTimeInvested()
    {
        double result = 0.0;
        for (ComponentFeature cf : componentFeatures())
        {
            result += cf.newTimeInvested();
        }
        return result;
    }


    // ----------------------------------------------------------
    public double newTimeInvestedTotal()
    {
        double result = 0.0;
        for (ComponentFeature cf : componentFeatures())
        {
            result += cf.newTimeInvestedTotal();
        }
        return result;
    }


    // ----------------------------------------------------------
    public double previousTimeInvestedTotal()
    {
        double result = 0.0;
        for (ComponentFeature cf : componentFeatures())
        {
            result += cf.previousTimeInvestedTotal();
        }
        return result;
    }


    // ----------------------------------------------------------
    public boolean isComplete()
    {
        for (ComponentFeature cf : componentFeatures())
        {
            if (!cf.isComplete())
            {
                return false;
            }
        }
        return true;
    }


    // ----------------------------------------------------------
    public boolean previousWasComplete()
    {
        for (ComponentFeature cf : componentFeatures())
        {
            if (!cf.previousWasComplete())
            {
                return false;
            }
        }
        return true;
    }


    // ----------------------------------------------------------
    public NSTimestamp newDeadline()
    {
        NSTimestamp result = null;
        for (ComponentFeature cf : componentFeatures())
        {
            if (result == null
                || (cf.newDeadline() != null
                    && cf.newDeadline().after(result)))
            {
                result = cf.newDeadline();
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    public NSTimestamp previousDeadline()
    {
        NSTimestamp result = null;
        for (ComponentFeature cf : componentFeatures())
        {
            if (result == null
                || (cf.previousDeadline() != null
                    && cf.previousDeadline().after(result)))
            {
                result = cf.previousDeadline();
            }
        }
        return result;
    }


    // ----------------------------------------------------------
    @Override
    public void setNumCheckRounds(int value)
    {
        currentFeedback = null;
        super.setNumCheckRounds(value);
    }


    // ----------------------------------------------------------
    @Override
    public void flushCaches()
    {
        currentFeedback = null;
        super.flushCaches();
    }


    // ----------------------------------------------------------
    public boolean isEmpty()
    {
        for (ComponentFeature cf : componentFeatures())
        {
            if (!cf.isEmpty())
            {
                return false;
            }
        }
        return true;
    }


    // ----------------------------------------------------------
    public boolean isFirstSheet()
    {
        return submission().assignmentOffering().isFirstSheet();
    }


    // ----------------------------------------------------------
    public boolean isLastSheet()
    {
        return submission().assignmentOffering().isLastSheet();
    }


    // ----------------------------------------------------------
    public void runAutomaticChecks(boolean checkEstimatePhase)
    {
        int round = numCheckRounds() + 1;
        setNumCheckRounds(round);
        for (ComponentFeature cf : componentFeatures())
        {
            if (cf.isEmpty())
            {
                cf.editingContext().deleteObject(cf);
            }
            else
            {
                cf.runAutomaticChecks(round, checkEstimatePhase);
            }
        }

        // Sheet-level checks for entry phase

        // Sheet-level checks for estimate phase
        if (checkEstimatePhase)
        {
            if (componentFeatures().count() == 1)
            {
                SheetFeedbackItem.create(editingContext(), round,
                    SheetFeedbackItem.SHEET_ONLY_ONE_FEATURE, this);
            }
            else
            {
                if (submission().assignmentOffering().assignment()
                    .minComponentFeatures() > 0
                    && componentFeatures().count() <
                        submission().assignmentOffering().assignment()
                        .minComponentFeatures())
                {
                    SheetFeedbackItem.create(editingContext(), round,
                        SheetFeedbackItem.SHEET_TOO_FEW_CFS, this);
                }

                double estimatedTotal = newEstimatedTotal();
                int estimatedNcloc = submission().assignmentOffering()
                    .assignment().expectedSize();
                if (estimatedNcloc > 0)
                {
                    if (estimatedTotal < estimatedNcloc / 15.0 * 0.6)
                    {
                        SheetFeedbackItem.create(editingContext(), round,
                            SheetFeedbackItem.SHEET_TOO_FEW_ESTIMATED_HOURS,
                            this);
                    }
                    else if (estimatedTotal > estimatedNcloc / 10.0 * 2)
                    {
                        SheetFeedbackItem.create(editingContext(), round,
                            SheetFeedbackItem.SHEET_TOO_MANY_ESTIMATED_HOURS,
                            this);
                    }
                }

                // Check for components that look suspiciously large
                double totalTime = newEstimatedRemaining();
                for (ComponentFeature cf : componentFeatures())
                {
                    if (cf.newEstimatedRemaining() > 16
                        || (componentFeatures().count() > 3
                            && (cf.newEstimatedRemaining() > totalTime / 2
                                || cf.newEstimatedRemaining() > 5 * totalTime /
                                    componentFeatures().count())))
                    {
                        SheetFeedbackItem.create(editingContext(), round,
                            SheetFeedbackItem.CF_TOO_LARGE, cf);
                    }
                }
            }

            Map<User, Double> load = new HashMap<User, Double>();
            for (ScheduleSheetSubmission s : submissions())
            {
                load.put(s.user(), 0.0);
            }

            NSMutableArray<SheetEntry> myEntries =
                new NSMutableArray<SheetEntry>();
            // build this array the hard way, since we need for this to work
            // even if this object hasn't been saved to the database yet.
            for (ComponentFeature cf : componentFeatures())
            {
                myEntries.addObjectsFromArray(cf.entries());
            }
            ERXArrayUtilities.sortArrayWithKey(myEntries, "currentDeadline");

            for (SheetEntry entry : myEntries)
            {
                if (entry.responsible().count() > 0)
                {
                    double hrs = entry.newEstimatedRemaining()
                        / entry.responsible().count();
                    for (User u : entry.responsible())
                    {
                        if (!load.containsKey(u))
                        {
                            // Already determined this person is over-scheduled
                            continue;
                        }

                        double newLoad = load.get(u) + hrs;
                        load.put(u, newLoad);
                        double hrsRemaining = calculateHoursAvailableBefore(
                            entry.currentDeadline());
                        if (hrsRemaining < 0.1)
                        {
                            // Silently ignore items without deadlines,
                            // which will generate other errors elsewhere
                            // anyway
                        }
                        else if (hrsRemaining / 2 < newLoad)
                        {
                            SheetFeedbackItem.create(editingContext(), round,
                                SheetFeedbackItem.SHEET_NOT_ENOUGH_TIME, entry)
                                .setMessage("This schedule has deadline(s) "
                                    + " that are too soon for "
                                    + u.name()
                                    + " to realistically complete the number "
                                    + "of estimated hours assigned.");
                            load.remove(u);
                            if (load.size() == 0)
                            {
                                break;
                            }
                        }
                        else if (hrsRemaining / 4 < newLoad)
                        {
                            SheetFeedbackItem.create(editingContext(), round,
                                SheetFeedbackItem.SHEET_TIME_TOO_TIGHT, entry)
                                .setMessage("This schedule has deadline(s) "
                                    + " that are too soon for "
                                    + u.name()
                                    + " to complete the number of estimated "
                                    + "hours assigned without serious "
                                    + "overtime.");
                        }
                    }
                }
                else
                {
                    // assume the submitter is the responsible one
                    User u = submission().user();
                    Double uLoad = load.get(u);
                    if (uLoad == null)
                    {
                        uLoad = 0.0;
                    }
                    load.put(u, uLoad + entry.newEstimatedRemaining());
                }
            }
        }

        if (currentFeedback().count() == 0)
        {
            SheetFeedbackItem.create(
                editingContext(), round, SheetFeedbackItem.OK, this);
        }
        currentFeedback = null;
        recalculateAutomatedScore();
    }


    // ----------------------------------------------------------
    public NSTimestamp currentDeadline()
    {
        NSTimestamp deadline = newDeadline();
        if (deadline == null)
        {
            deadline = previousDeadline();
        }
        return deadline;
    }


    // ----------------------------------------------------------
    public void recalculateAutomatedScore()
    {
        ScheduleSheetAssignment assignment =
            submission().assignmentOffering().assignment();
        if (!assignment.usesToolCheckScore())
        {
            return;
        }

        int deductions = 0;  // relative to 100-points
        for (SheetFeedbackItem i : currentFeedback())
        {
            if (i.category() == SheetFeedbackItem.ERROR)
            {
                if (i.sheetEntry() != null)
                {
                    deductions += 4;
                }
                else if (i.componentFeature() != null)
                {
                    deductions += 8;
                }
                else
                {
                    deductions += 50;
                }
            }
        }
        deductions = Math.min(100, deductions);

        setToolScore(submission().assignmentOffering().submissionProfile()
            .toolPoints() * (100 - deductions) / 100.0);
    }


    // ----------------------------------------------------------
    public double calculateHoursAvailableBefore(NSTimestamp target)
    {
        if (target == null)
        {
            return 0;
        }

        NSTimestamp start = submission().submitTime();
        if (start == null)
        {
            start = new NSTimestamp();
        }
        long diff = target.getTime() - start.getTime();

        if (diff <= 0)
        {
            return 0;
        }

        return diff / HOUR;
    }


    //~ Instance/static fields ................................................

    private static final double HOUR = 1000 * 60 * 60;
    private NSArray<SheetFeedbackItem> currentFeedback;
}
