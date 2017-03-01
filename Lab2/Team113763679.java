package my_code.Lab2;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;


import java.io.IOException;

public class Team113763679 {

    public static void main(String[] args) throws IOException{

        // Create choco model and call it Team
        Model model = new Model("Team");

        // Import the data to be used from the three text files
//        TeamData data = new TeamData("C:\\Users\\jonat\\Documents\\College\\CS4093\\Lab1\\src\\main\\resources\\team0.txt");
        TeamData data = new TeamData("C:\\Users\\jonat\\Documents\\College\\CS4093\\Lab1\\src\\main\\resources\\team1.txt");
//        TeamData data = new TeamData("C:\\Users\\jonat\\Documents\\College\\CS4093\\Lab1\\src\\main\\resources\\team2.txt");

        // Initialise the variables using the data from the TeamData class
        int numWorkers = data.getNumWorkers();
        int chargeCap = data.getChargeCap();
        int teamSize = data.getTeamSize();
        int[] charges = data.getCharges();
        int[] leaders = data.getLeaders();
        int[] testers = data.getTesters();
        int[][] badPairs = data.getBadPairs();


        //Assign each member to the team
        IntVar[] team = new IntVar[numWorkers];
        for(int member = 0; member<numWorkers; member++){
            team[member] = model.intVar("team"+member, 0, 1);
        }

        //Get the sum of the whole team to ensure the correct amount of members are placed in the team
        model.sum(team, "=", teamSize).post();

        // Get the total cost range between 0 and the charge CAp
        IntVar totalCost = model.intVar("total cost", 0, chargeCap);

        //Perform a scalar operation on the charges and the team to ensure the charge is equal to the total cost
        model.scalar(team, charges, "=", totalCost).post();

        //Ensure the total cost is less than or equal to the charge cap
        model.arithm(totalCost, "<=", chargeCap).post();

        //Perform a scalar operation to ensure there is a leader
        model.scalar(team, leaders, ">=", 1).post();

        //Perform a scalar operation to ensure there is a tester
        model.scalar(team, testers, ">=", 1).post();


        //Loop through the badpairs matrix to check for any workers that will not work together
        //check the badpairs that if the position at worker1 and worker2 is 1,
        //ensure the sum of both in their positions of team is less than 2
        for(int worker1 = 0; worker1<numWorkers; worker1++){
            for(int worker2 = 0; worker2<numWorkers; worker2++){
                if (badPairs[worker1][worker2] == 1){
                    model.arithm(team[worker1], "+", team[worker2], "<", 2).post();
                }
            }
        }

        Solver solver = model.getSolver();

//        solver.setSearch(Search.domOverWDegSearch(team));
//        solver.setSearch(Search.inputOrderLBSearch(team));
//        solver.setSearch(Search.activityBasedSearch(team));
//        solver.setSearch(new ImpactBased(team, true));
//        solver.setSearch(new ImpactBased(team, 2,3,10, 0, false));


        // Set the model to look for the smalles
//        model.setObjective(Model.MINIMIZE, totalCost);



        int numsolutions = 0;
        //if (solver.solve()) {
        while (solver.solve()) { //print the solution
            numsolutions++;
            System.out.println("Solution " + numsolutions + ":");

            //next code block interrogates the variables and gets the current solution
            System.out.print("Workers:   ");
            for (int worker = 0; worker<numWorkers; worker++) {
                System.out.print("\t" + worker);
            }
            System.out.println();
            System.out.print("Charges: ");
            for (int charge = 0; charge<numWorkers; charge++) {
                System.out.print("\t" + charges[charge]);
            }
            System.out.println();
            System.out.print("Leaders: ");
            for (int leader = 0; leader<numWorkers; leader++) {
                System.out.print("\t" + leaders[leader]);
            }
            System.out.println();
            System.out.print("Testers  : ");
            for (int tester = 0; tester<numWorkers; tester++) {
                System.out.print("\t" + testers[tester]);
            }
            System.out.println();

            System.out.print("Team:   ");
            for (int worker = 0; worker<numWorkers; worker++) {
                System.out.print("\t" + team[worker].getValue());
            }
            System.out.println();

            System.out.println("Team Size = " + teamSize);
            System.out.println("Total Charge = " + totalCost.getValue());
            System.out.println();
        }

        solver.printStatistics();
    }
}