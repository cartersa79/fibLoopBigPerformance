import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.function.Function;

public class FibLoopBigPerformance {

    static ThreadMXBean bean = ManagementFactory.getThreadMXBean();

    /* define constants */
    static long MAXVALUE = 2000000000;
    static long MINVALUE = -2000000000;
    static int numberOfTrials = 10;      // adjust numberOfTrials and MAXINPUTSIZE based on available
    static int MAXINPUTSIZE = (int) Math.pow(2, 18);  // time, processor speed, and available memory
    static int MININPUTSIZE = 1;

    static String ResultsFolderPath = "/home/steve/Results/"; // pathname to results folder
    static FileWriter resultsFile;
    static PrintWriter resultsWriter;

    public static void main(String[] args) {
        // run the whole experiment at least twice, and expect to throw away the data from the earlier runs, before java has fully optimized
        System.out.println("Running first full experiment...");
        runFullExperiment("FibLoopBig-Exp1-ThrowAway.txt");
        System.out.println("Running second full experiment...");
        runFullExperiment("FibLoopBig-Exp2.txt");
        System.out.println("Running third full experiment...");
        runFullExperiment("FibLoopBig-Exp3.txt");

        // verify that the algorithm works
        System.out.println("");
        System.out.println("----Verification Test----");
        for (int i = 0; i <= 93; i++)
            System.out.println(FibLoopBig(i));
    }

    static void runFullExperiment(String resultsFileName) {
        try {
            resultsFile = new FileWriter(ResultsFolderPath + resultsFileName);
            resultsWriter = new PrintWriter(resultsFile);
        } catch (Exception e) {
            System.out.println("*****!!!!!  Had a problem opening the results file " + ResultsFolderPath + resultsFileName);
            return; // not very foolproof... but we do expect to be able to create/open the file...
        }

        ThreadCpuStopWatch BatchStopwatch = new ThreadCpuStopWatch(); // for timing an entire set of trials
        ThreadCpuStopWatch TrialStopwatch = new ThreadCpuStopWatch(); // for timing an individual trial

        resultsWriter.println("#X           AverageTime"); // # marks a comment in gnuplot data
        resultsWriter.flush();

        /* for each size of input we want to test: in this case starting small and doubling the size each time */
        for (int inputSize = MININPUTSIZE; inputSize <= MAXINPUTSIZE; inputSize *= 2) {
            // progress message...
            System.out.println("Running test for input size " + inputSize + " ... ");

            /* repeat for desired number of trials (for a specific size of input)... */
            long batchElapsedTime = 0;
            // generate a list of random integers in random order to use as test input
            // In this case we're generating one list to use for the entire set of trials (of a given input size)
            //System.out.print("    Generating test data...");
            //long[] testList = createRandomIntegerList(inputSize);
            //System.out.println("...done.");
            //System.out.print("    Running trial batch...");

            /* force garbage collection before each batch of trials run so it is not included in the time */
            //System.gc();

            // instead of timing each individual trial, we will time the entire set of trials (for a given input size)
            // and divide by the number of trials -- this reduces the impact of the amount of time it takes to call the
            // stopWatch methods themselves
            //BatchStopwatch.start(); // comment this line if timing trials individually

            // run the trials
            for (long trial = 0; trial < numberOfTrials; trial++) {
                // generate a random list of integers each trial
                //long testList = createRandomIntegerList(inputSize);

                // generate a random key to search in the range of a the min/max numbers in the list
                // long testSearchKey = (long) (0 + Math.random() * (testList[testList.length - 1]));
                /* force garbage collection before each trial run so it is not included in the time */
                System.gc();

                TrialStopwatch.start(); // *** uncomment this line if timing trials individually
                /* run the function we're testing on the trial input */
                BigInteger FibTest = FibLoopBig(inputSize);
                batchElapsedTime = batchElapsedTime + TrialStopwatch.elapsedTime(); // *** uncomment this line if timing trials individually
            }
            //batchElapsedTime = BatchStopwatch.elapsedTime(); // *** comment this line if timing trials individually
            double averageTimePerTrialInBatch = (double) batchElapsedTime / (double) numberOfTrials; // calculate the average time per trial in this batch

            long N = (long)(Math.floor(Math.log(inputSize)/Math.log(2)));
            /* print data for this size of input */
            resultsWriter.printf("%-12d %-15.2f \n", inputSize, averageTimePerTrialInBatch); // might as well make the columns look nice
            //resultsWriter.printf("%-12d %-6d %-15.2f \n", inputSize, N, averageTimePerTrialInBatch); // might as well make the columns look nice
            resultsWriter.flush();
            System.out.println(" ....done.");
        }
    }

    // Fib Sequence = 0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, ...
    // Fib(92) is the highest we can go with longs. Overflows thereafter.

    // generate Fibonacci sequence using a loop algorithm and Java's
    // built-in BigInteger
    public static BigInteger FibLoopBig(long x){
        // initialize values
        BigInteger minusTwo = BigInteger.valueOf(0);
        BigInteger minusOne = BigInteger.valueOf(1);
        BigInteger current = BigInteger.valueOf(0);

        // loop through until x is reached
        for(int i = 0; i < x; i++){
            minusTwo = minusOne;    // move F(x-1) into F(x-2)
            minusOne = current;     // move F(x) into F(x-1)
            current = minusTwo.add(minusOne);  // F(x) = F(x-2) + F(x-1)
        }
        return current;
    }
}
