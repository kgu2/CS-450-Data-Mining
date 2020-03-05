import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class IrisClustering {

	private static ArrayList<String> testDoc;

	public static final int NUM_LINES_TEST = 150;	
	public static final int NUM_FEATURES = 4;	

	public static void main(String[] args)
	{
		File testFile = new File("./iris_test.txt");
		try {
			testDoc = openFile(testFile);
			ArrayList<ArrayList<Double>> matrixTest = convertToMatrix(NUM_LINES_TEST, testDoc);

			int k = 3;
			ArrayList<ArrayList<Double>> means = new ArrayList<ArrayList<Double>>(k);
			
			// initialize k means or k points randomly
			for(int i = 0; i < k; i ++)
			{
				ArrayList<Double> randomPoint = getRandomPoint(matrixTest);
				means.add(randomPoint);
			}
			System.out.println(means.toString());

			ArrayList<Integer> nearestCluster = new ArrayList<Integer>();
			
			int numIterations = 100;
			for(int q = 0; q < numIterations; q++)
			{
				nearestCluster = new ArrayList<Integer>();

				// We categorize each item to its closest mean. Iterate through total number of items
				for(int i = 0; i < NUM_LINES_TEST; i++)			
				{
					ArrayList<Double> similarity = new ArrayList<Double>(k);
					for(int a = 0; a < k; a++)
					{
						// find distances to means for every item
						similarity.add(calculateCosineSimilarity(matrixTest.get(i), means.get(a)));
					}

					// Find the mean closest to the item
					double nearest = similarity.get(0);
					for(Double x: similarity)
					{
						if(x > nearest)
						{
							nearest = x;
						}
					}

					// assign item to mean
					nearestCluster.add(similarity.indexOf(nearest));		
				}

				System.out.println(nearestCluster.toString());

				// update mean's coordinates. It is the Averages of the items categorized in that mean
				means = getUpdatedPoint(nearestCluster, matrixTest);
				System.out.println(means.toString());
			}

			writeToFile(nearestCluster);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static double calculateCosineSimilarity(ArrayList<Double> vectorA, ArrayList<Double> vectorB)
	{
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;
		for (int i = 0; i < vectorA.size(); i++) 
		{
			dotProduct += vectorA.get(i) * vectorB.get(i);
			normA += Math.pow(vectorA.get(i), 2);
			normB += Math.pow(vectorB.get(i), 2);
		}   
		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}

	public static ArrayList<Double> getRandomPoint(ArrayList<ArrayList<Double>> matrix)
	{
		Random r = new Random();
		return matrix.get(r.nextInt(NUM_LINES_TEST));
	}

	public static ArrayList<ArrayList<Double>> getUpdatedPoint(ArrayList<Integer> nearestCluster, ArrayList<ArrayList<Double>> matrix)
	{
		ArrayList<ArrayList<Double>> updatedPoint = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> cluster1 = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> cluster2 = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> cluster3 = new ArrayList<ArrayList<Double>>();

		for(int c = 0; c < nearestCluster.size(); c ++)
		{
			if(nearestCluster.get(c) == 0)
			{
				cluster1.add(matrix.get(c));
			}
			else if (nearestCluster.get(c) == 1)
			{
				cluster2.add(matrix.get(c));
			}
			else
			{
				cluster3.add(matrix.get(c));
			}
		}
		updatedPoint.add(getAverage(cluster1));
		updatedPoint.add(getAverage(cluster2));
		updatedPoint.add(getAverage(cluster3));

		return updatedPoint;
	}

	public static ArrayList<Double> getAverage(ArrayList<ArrayList<Double>> list)
	{
		ArrayList<Double> average = new ArrayList<Double>();

		ArrayList<Double> w = new ArrayList<Double>();
		ArrayList<Double> x = new ArrayList<Double>();
		ArrayList<Double> y = new ArrayList<Double>();
		ArrayList<Double> z = new ArrayList<Double>();

		for(int i = 0; i < list.size(); i ++)
		{
			for(int a = 0; a < list.get(i).size(); a++)
			{
				if(a == 0)
				{
					w.add(list.get(i).get(0));
				}
				else if (a== 1)
				{
					x.add(list.get(i).get(1));
				}
				else if (a== 2)
				{
					y.add(list.get(i).get(2));
				}
				else
				{
					z.add(list.get(i).get(3));
				}
			}
		}
		average.add(calculateAverage(w));
		average.add(calculateAverage(x));
		average.add(calculateAverage(y));
		average.add(calculateAverage(z));
		return average;
	}

	public static double calculateAverage(ArrayList<Double> list) 
	{
		double sum = 0;
		for (double i : list) 
		{
			sum += i;
		}
		return sum / list.size();
	}


	public static ArrayList<String> openFile(File file) throws FileNotFoundException 
	{
		Scanner scan = new Scanner(file);
		ArrayList<String> document = new ArrayList<String>();

		while(scan.hasNextLine())
		{
			String line = scan.nextLine().replaceAll("\\s+"," "); 	
			document.add(line);
		}
		scan.close();
		return document;
	}

	public static ArrayList<ArrayList<Double>> convertToMatrix(int numLines, ArrayList<String> document)
	{
		ArrayList<ArrayList<String>> matrix = new ArrayList<ArrayList<String>>();	

		for(int i = 0; i < numLines; i ++)
		{
			ArrayList<String> wordArrayList = new ArrayList<String>();
			for(String word : document.get(i).split(" ")) 
			{
				if(word.equals(""))
				{
					continue;
				}
				wordArrayList.add(word);
			}
			matrix.add(wordArrayList);
		}

		ArrayList<ArrayList<Double>> matrixDouble = new ArrayList<ArrayList<Double>>();	
		for(int i = 0; i < numLines; i ++)
		{
			ArrayList<Double> row = new ArrayList<Double>();
			for(String s : matrix.get(i))
			{
				row.add(Double.valueOf(s));

			}
			matrixDouble.add(row);
		}

		return matrixDouble;	
	}

	public static void writeToFile(ArrayList<Integer> nearestClusters)
	{
		FileWriter fileWriter;
		try {
			fileWriter = new FileWriter("Result.dat");
			PrintWriter printWriter = new PrintWriter(fileWriter);

			for(int i = 0; i < nearestClusters.size(); i ++)
			{
				if(nearestClusters.get(i) == 0)
				{
					printWriter.print("1\n");
				}
				else if(nearestClusters.get(i) == 1)
				{
					printWriter.print("2\n");
				}	
				else
				{
					printWriter.print("3\n");
				}
			}
			printWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

}
