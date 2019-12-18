import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Collections;
import java.util.Random;

public class Greedy {

    static ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    /* define constants */
    static final int distanceToSelf = 99999999;
    static int numberOfNodes;
    //static int[][] matrix;
    static Stack<Integer> stack;
    static ArrayList<Integer> path;
    static int numberOfTrials = 15;
    static int MAXINPUTSIZE = 10;
    static Random random = new Random();
    static String ResultsFolderPath = "/home/nicolocker/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;

    public static void main(String[] args) {

        verifyWorks();
        System.out.println("\n");

        System.out.println("Running first full experiment...");
        runFullExperiment("Greedy1-CircularCost.txt");   //change all 3 to RandomCost, EuclideanCost or CircularCost depending on which is being used
        System.out.println("Running second full experiment...");
        runFullExperiment("Greedy2-CircularCost.txt");
        System.out.println("Running third full experiment...");
        runFullExperiment("Greedy3-CircularCost.txt");
    }

    public static void verifyWorks() {
        Greedy.GenerateRandomCostMatrix(10);
        Greedy.GenerateRandomEuclideanCostMatrix(10, 20);
        Greedy.GenerateRandomCircularGraphCostMatrix(10, 40);
    }


    public static void runFullExperiment(String resultsFileName) {

        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch (Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file " + ResultsFolderPath + resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial
        double prevTime = 0;

        resultsWriter.println("#NumOfVertices        AvgTime         Ratio"); // # marks a comment in gnuplot data
        resultsWriter.flush();

        for (int inputSize = 2; inputSize <= MAXINPUTSIZE; inputSize++) {
            System.out.println("Running test for input size " + inputSize + " ... ");
            System.gc();

            long batchElapsedTime = 0;

            int[][] matrix;
            for (long trial = 0; trial < numberOfTrials; trial++) {
                //matrix = Greedy.GenerateRandomCostMatrix(inputSize);      //** uncomment to test this one, comment the below
                //matrix = Greedy.GenerateRandomEuclideanCostMatrix(inputSize, 50);   //** uncomment to test this one, comment the above
                matrix = Greedy.GenerateRandomCircularGraphCostMatrix(inputSize, 50); //** uncomment to test this one, comment the above
                TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                Greedy.greedyTsp(matrix);
                batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }

            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double) numberOfTrials;

            double ratio = 0;
            if (prevTime > 0) {
                ratio = averageTimePerTrialInBatch / prevTime;
            }


            prevTime = averageTimePerTrialInBatch;

            /* print data for this size of input */
            resultsWriter.printf("%6d  %20.2f %13.2f\n", inputSize, averageTimePerTrialInBatch, ratio); // might as well make the columns look nice
            resultsWriter.flush();
            System.out.println(" ....done.");

        }
    }

    public static int[][] GenerateRandomCostMatrix (int edgeCost){
        int[][] matrix = new int[edgeCost][edgeCost];

        for (int i = 0; i < edgeCost; i++) {
            //  System.out.println("++++");

            for (int j = 0; j <= i; j++) {
                if (i == j) {
                    matrix[i][j] = 0;
                    //   System.out.println(matrix[i][j]);

                } else {
                    int temp = random.nextInt(100);
                    matrix[i][j] = temp;
                    //    System.out.println(matrix[i][j]);
                }
            }
        }
        System.out.println("Generate Random Cost Matrix:");
        printMatrix(matrix);

        return matrix;
    }

    public static int[][] GenerateRandomEuclideanCostMatrix (int numOfVerticies, int coordinate){
        Vertex[] vertice = new Vertex[numOfVerticies];

        for (int i = 0; i < numOfVerticies; i++) {
            vertice[i] = new Vertex(random.nextInt(coordinate - 1), random.nextInt(coordinate - 1), i);
        }

        int[][] matrix = new int[numOfVerticies][numOfVerticies];

        for (int i = 0; i < numOfVerticies; i++) {
            //System.out.println("++++");
            for (int j = 0; j <= i; j++) {
                if (i == j) {
                    matrix[i][j] = 0;
                    //System.out.println(matrix[i][j]);
                } else {
                    matrix[i][j] = vertice[i].distance(vertice[j]);
                    matrix[j][i] = vertice[j].distance(vertice[i]);
                    //System.out.println(matrix[i][j]);
                }
            }
        }

        System.out.println("Generate Random Euclidean Cost Matrix:");
        printMatrix(matrix);

        return matrix;
    }

    public static int[][] GenerateRandomCircularGraphCostMatrix (int numOfVerticies, int radius){
        Vertex[] vertice = new Vertex[numOfVerticies];

        double angle = 360 / numOfVerticies;
        double curAngle = 0;
        ArrayList<Vertex> sortVertices = new ArrayList<Vertex>();

        for (int i = 0; i < numOfVerticies; i++) {
            double radian = curAngle * Math.PI / 180;
            double x = Math.cos(radian) * radius;
            double y = Math.sin(radian) * radius;
            sortVertices.add(new Vertex(x, y, i));
            curAngle = curAngle + angle;
        }

        Collections.shuffle(sortVertices);
        vertice = sortVertices.toArray(vertice);

        int[][] matrix = new int[numOfVerticies][numOfVerticies];
        for (int i = 0; i < numOfVerticies; i++) {
            //System.out.println("++++");
            for (int j = 0; j <= i; j++) {
                if (i == j) {
                    matrix[i][j] = 0;
                    //System.out.println(matrix[i][j]);
                } else {
                    matrix[i][j] = vertice[i].distance(vertice[j]);
                    matrix[j][i] = vertice[j].distance(vertice[i]);
                    //System.out.println(matrix[i][j]);
                }
            }
        }

        System.out.println("Generate Circular Graph Cost Matrix:");
        printMatrix(matrix);

        return matrix;
    }

    public static void printMatrix (int[][] matrix){
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.printf("%5d", matrix[i][j]);
            }
            System.out.println();
        }
        System.out.println("\n");
    }

    /*https://github.com/EmmaBYPeng/TSP/blob/master/src/greedy/GreedyTSP.java*/
    public static int[] greedyTsp(int[][] matrix){
        matrix = matrix;
        numberOfNodes = matrix.length;
        stack = new Stack<Integer>();
        path = new ArrayList<Integer>();

        int visited[] = new int[numberOfNodes];

        visited[0] = 1;
        stack.push(0);
        path.add(0);

        int currentNode;
        int nextNode = 0;
        boolean minFlag = false;

        while (!stack.isEmpty()) {
            currentNode = stack.peek();
            int min = distanceToSelf; // All distance values should be negative

            for (int i = 0; i < numberOfNodes; i++) {
                int dist = matrix[currentNode][i];

                if (dist < distanceToSelf && visited[i] == 0) {
                    if (dist < min) {
                        min = dist;
                        nextNode = i;
                        minFlag = true;
                    }
                }
            }

            if (minFlag) {
                visited[nextNode] = 1;
                stack.push(nextNode);
                path.add(nextNode);
                minFlag = false;
                continue;
            }
            stack.pop();
        }

        int[] pathArray = new int[path.size()];

        for(int j = 0; j < path.size(); j++){
            if(path.get(j) != null){
                pathArray[j] = path.get(j);
            }
        }

        double bestTourCost = computeTourCost(pathArray, matrix);
        System.out.println("Best Tour Cost:");
        System.out.println(bestTourCost + "\n");;

        return pathArray;
    }


    public static double computeTourCost ( int[] tour, int[][] matrix){
        double cost = 0;

        // find cost of going to each "city"
        for (int i = 1; i < matrix.length; i++) {
            int from = tour[i - 1];
            int to = tour[i];
            cost += matrix[from][to];
        }

        //find cost to return to beginning "city"
        int last = tour[matrix.length - 1];
        int first = tour[0];

        return cost + matrix[last][first];
    }
}


