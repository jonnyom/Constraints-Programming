package my_code.Lab4;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.variables.ImpactBased;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.io.IOException;

public class Meetings113763679 {
    public static void main(String[] args) throws IOException {
        String file = "meetings0.txt";
//        String file = "meetings1.txt";
//        String file = "meetings2.txt";
//        String file = "meetings3.txt";

        // Create the reader object
        MeetingProblemReader reader = new MeetingProblemReader(file);

        int numPeople = reader.getNumPeople();
        int numMeetings = reader.getNumMeetings();
        int maxDays = reader.getmaxDays();
        int maxProj = reader.getMaxProjectors();
        int[] durations = reader.getDurations();
        int[][] meetingPeople = reader.getMeetingPeople();
        int[] earliestStart = reader.getEarliestStart();

        // Create the model
        Model model = new Model("Meeting");

        // Get the makespan of the tasks between 0 and maxDays
        IntVar makespan = model.intVar("makespan", 0, maxDays);

        // Get the heights for task consumption based on the number of meetings
        IntVar[] heights = new IntVar[numMeetings];

        // Array of all the meetings start times
        IntVar[] startOfMeetings = new IntVar[numMeetings];

        // Array for the total number of meetings possible at any given time
        IntVar capacity = model.intVar(maxProj);

        // Task array for each meeting
        Task[] meetings = new Task[numMeetings];

        // Iterate through each meeting
        for(int meeting = 0; meeting < numMeetings; meeting++){

            // Find the people in each meeting, and set the starting day for each meeting to
            // the earliest start day available for the people in the meeting
            int startingPoint = 0;
            for(int teamMember = 0; teamMember<numPeople; teamMember++){
                // If a person is in a meeting
                if(meetingPeople[meeting][teamMember] == 1){
                    // If their earliest start time is greater than the original starting day for the meeting
                    // set starting day to the earliest start.
                    if(earliestStart[teamMember] > startingPoint){
                        startingPoint = earliestStart[teamMember];
                    }
                }
            }

            // Create the new Task object for the meeting, with a starting point, duration and end day
            meetings[meeting] = new Task(model.intVar(startingPoint, maxDays),
                    model.intVar(durations[meeting]), model.intVar(startingPoint, maxDays));

            // Set each height to 1
            heights[meeting] = model.intVar(1);

            // Set the start of each meeting
            startOfMeetings[meeting] = meetings[meeting].getStart();

            // Make sure that the end of every meeting is less than the makespan
            model.arithm(meetings[meeting].getEnd(), "<=", makespan).post();
        }

        // Create a cumulative object and post it using the meetings, heights and capacity arrays
        model.cumulative(meetings, heights, capacity).post();

        // For each meeting and people in the meeting, make sure that no
        // two meetings overlap that have the same person attending both
        for (int meeting = 0; meeting < numMeetings; meeting++) {
            for (int person = 0; person < numPeople-1; person++) {
                if(meetingPeople[meeting][person] == 1){
                    for (int meeting2 = meeting+1; meeting2 < numMeetings; meeting2++) {
                        if(meetingPeople[meeting2][person] == 1){
                            model.or(model.arithm(meetings[meeting].getEnd(),
                                "<=", meetings[meeting2].getStart()),
                            model.arithm(meetings[meeting2].getEnd(),
                                "<=", meetings[meeting].getStart())).post();
                        }
                    }
                }
            }
        }

        // Create the solver object
        Solver solver = model.getSolver();

        // Set up the various search methods
//        solver.setSearch(Search.domOverWDegSearch(startOfMeetings));
//        solver.setSearch(Search.inputOrderLBSearch(startOfMeetings));
//        solver.setSearch(Search.activityBasedSearch(startOfMeetings));
//        solver.setSearch(new ImpactBased(startOfMeetings, true));

        // Set the model to search for the minimum
        model.setObjective(Model.MINIMIZE, makespan);

        int numsolutions = 0;
//      if (solver.solve()) {
        while (solver.solve()) { //print the solution
            numsolutions++;
            System.out.println("Solution " + numsolutions + ":");

            System.out.println("Number of days needed: " + makespan.getValue());
            for (int meeting = 0; meeting < numMeetings; meeting++) {
                int end = meetings[meeting].getEnd().getValue()-1;
                System.out.printf("Meeting %d:\n", meeting);
                System.out.println("Started on day: " + meetings[meeting].getStart().getValue());
                System.out.println(" ended on day: " + end);
                System.out.println("*****************************");
            }
            System.out.println("*****************************");
            System.out.println();
        }
        System.out.println("Total number of solutions found: "+ numsolutions);
        System.out.println();
        solver.printStatistics();

    }
}
