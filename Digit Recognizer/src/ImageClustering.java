import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;


public class ImageClustering {
	
	private static ArrayList<String> testDoc;
	
	public static final int NUM_LINES_TEST = 10000;
	public static final int NUM_FEATURES = 784;	
	public static final int K = 10;
	
	public static void main(String[] args)
	{
		File testFile = new File("./image_test.dat");
		
		try {
			testDoc = openFile(testFile);
			
			System.out.println("Creating Matrices...");
			ArrayList<ArrayList<Integer>> matrixTest = convertToMatrix(NUM_LINES_TEST, testDoc);
			ArrayList<ArrayList<Integer>> means = new ArrayList<ArrayList<Integer>>(K);
			
			System.out.println("Reducing dimensions...");
			/*double matrixAry[][] = convertArraylistToArray(matrixTest);
			matrixAry = getTruncatedSVD(matrixAry, NUM_FEATURES/2);
			matrixTest = convertArrayToList(matrixAry);*/
			
			System.out.println("Clustering...");
			// initialize k means or k points randomly
			for(int i = 0; i < K; i ++)
			{
				ArrayList<Integer> randomPoint = getRandomPoint(matrixTest);
				means.add(randomPoint);
			}
				
			ArrayList<Integer> nearestCluster = new ArrayList<Integer>();
			ArrayList<Double> similarityMetric = new ArrayList<Double>();
			
			int numIterations = 10;
			for(int q = 0; q < numIterations; q++)
			{
				nearestCluster = new ArrayList<Integer>();

				// We categorize each item to its closest mean. Iterate through total number of items
				for(int i = 0; i < NUM_LINES_TEST; i++)			
				{
					ArrayList<Double> similarity = new ArrayList<Double>(K);
					for(int a = 0; a < K; a++)
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
					
					// internal metric scoring
					if(q == numIterations -1)
					{
						similarityMetric.add(nearest);
					}

					// assign item to mean
					nearestCluster.add(similarity.indexOf(nearest));
					
				}

				// update mean's coordinates. It is the Averages of the items categorized in that mean
				means = getUpdatedPoint(nearestCluster, matrixTest);
				
			}
			//System.out.println(nearestCluster.toString());
			
			double metric = calculateAverageD(similarityMetric);
			System.out.println("Done! Accuracy: "+ metric);
			
			writeToFile(nearestCluster);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public static double calculateCosineSimilarity(ArrayList<Integer> vectorA, ArrayList<Integer> vectorB)
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
	
	public static ArrayList<Integer> getRandomPoint(ArrayList<ArrayList<Integer>> matrix)
	{
		Random r = new Random();
		return matrix.get(r.nextInt(NUM_LINES_TEST));
	}
	
	public static ArrayList<ArrayList<Integer>> getCluster(ArrayList<Integer> list, ArrayList<ArrayList<Integer>> matrix, int index)
	{
		ArrayList<ArrayList<Integer>> cluster = new ArrayList<ArrayList<Integer>>();

		for(int i = 0; i < list.size(); i++)
		{
			if(list.get(i) == index)
			{
				cluster.add(matrix.get(i));
			}
		}
		return cluster;
	}

	public static ArrayList<ArrayList<Integer>> getUpdatedPoint(ArrayList<Integer> nearestCluster, ArrayList<ArrayList<Integer>> matrix)
	{
		ArrayList<ArrayList<Integer>> updatedPoint = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<ArrayList<Integer>>> clusters = new ArrayList<ArrayList<ArrayList<Integer>>>(K);
		
		for(int i = 0; i < K; i++)
		{
			ArrayList<ArrayList<Integer>> cluster = getCluster(nearestCluster, matrix, i);
			clusters.add(cluster);
		}
		
		for(int i = 0; i < K; i++)
		{
			updatedPoint.add(getAverage(clusters.get(i)));
		}
		
		return updatedPoint;
	}
	
	public static ArrayList<String> openFile(File file) throws FileNotFoundException 
	{
		Scanner scan = new Scanner(file);
		ArrayList<String> document = new ArrayList<String>();

		while(scan.hasNextLine())
		{
			String line = scan.nextLine().replaceAll("\\p{Punct}"," ");	
			document.add(line);
		}
		scan.close();
		return document;
	}
	
	public static ArrayList<ArrayList<Integer>> convertToMatrix(int numLines, ArrayList<String> document)
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

		ArrayList<ArrayList<Integer>> matrixInteger = new ArrayList<ArrayList<Integer>>();	
		for(int i = 0; i < numLines; i ++)
		{
			ArrayList<Integer> row = new ArrayList<Integer>();
			for(String s : matrix.get(i))
			{
				row.add(Integer.valueOf(s));

			}
			matrixInteger.add(row);
		}

		return matrixInteger;	
	}
	
	public static double[][] getTruncatedSVD(double[][] matrix, int k)
	{
	    SingularValueDecomposition svd = new SingularValueDecomposition(new Array2DRowRealMatrix(matrix));

	    double[][] truncatedU = new double[svd.getU().getRowDimension()][k];
	    svd.getU().copySubMatrix(0, truncatedU.length - 1, 0, k - 1, truncatedU);

	    double[][] truncatedS = new double[k][k];
	    svd.getS().copySubMatrix(0, k - 1, 0, k - 1, truncatedS);

	    double[][] truncatedVT = new double[k][svd.getVT().getColumnDimension()];
	    svd.getVT().copySubMatrix(0, k - 1, 0, truncatedVT[0].length - 1, truncatedVT);

	    RealMatrix approximatedSvdMatrix = (new Array2DRowRealMatrix(truncatedU)).multiply(new Array2DRowRealMatrix(truncatedS)).multiply(new Array2DRowRealMatrix(truncatedVT));

	    return approximatedSvdMatrix.getData();
	}
	
	public static double[][] convertArraylistToArray(ArrayList<ArrayList<Integer>> list)
	{
		double[][] doubleArray = new double[list.size()][];
		
		for (int i = 0; i < list.size(); i++) 
		{
		    ArrayList<Integer> row = list.get(i);
		    double[] copy = new double[row.size()];
		    for (int j = 0; j < row.size(); j++) 
		    {
		        copy[j] = row.get(j);
		    }
		    doubleArray[i] = copy;
		}
		return doubleArray;
	}
	
	public static ArrayList<ArrayList<Integer>> convertArrayToList(double[][] list)
	{
		ArrayList<ArrayList<Integer>> matrix = new ArrayList<ArrayList<Integer>>();
		
		for(int i = 0; i < list.length; i++)
		{
			ArrayList<Integer> row = new ArrayList<Integer>();
			for(int c = 0; c < list[i].length; c++)
			{
				row.add((int) list[i][c]);
			}
			matrix.add(row);
		}
		
		return matrix;
	}
	
	public static ArrayList<Integer> getAverage(ArrayList<ArrayList<Integer>> list)
	{
		ArrayList<Integer> average = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> averages = new ArrayList<ArrayList<Integer>>();

		for(int c = 0; c < NUM_FEATURES; c++)
		{
			ArrayList<Integer> columns = getColumn(list, c);
			averages.add(columns);
		}

		for(int i = 0; i < averages.size(); i++)
		{
			average.add(calculateAverage(averages.get(i)));
		}
		return average;
	}

	public static ArrayList<Integer> getColumn(ArrayList<ArrayList<Integer>> list, int index)
	{
		ArrayList<Integer> column = new ArrayList<Integer>();

		for(int i = 0; i < list.size(); i++)
		{
			column.add(list.get(i).get(index));
		}
		return column;
	}

	public static int calculateAverage(ArrayList<Integer> list) 
	{
		int sum = 0;
		for (int i : list) 
		{
			sum += i;
		}
		return sum / list.size();
	}
	
	public static double calculateAverageD(ArrayList<Double> list) 
	{
		double sum = 0;
		for (double i : list) 
		{
			sum += i;
		}
		return sum / list.size();
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
				else if(nearestClusters.get(i) == 2)
				{
					printWriter.print("3\n");
				}	
				else if(nearestClusters.get(i) == 3)
				{
					printWriter.print("4\n");
				}	
				else if(nearestClusters.get(i) == 4)
				{
					printWriter.print("5\n");
				}	
				else if(nearestClusters.get(i) == 5)
				{
					printWriter.print("6\n");
				}	
				else if(nearestClusters.get(i) == 6)
				{
					printWriter.print("7\n");
				}	
				else if(nearestClusters.get(i) == 7)
				{
					printWriter.print("8\n");
				}	
				else if(nearestClusters.get(i) == 8)
				{
					printWriter.print("9\n");
				}	
				else
				{
					printWriter.print("10\n");
				}
			}
			printWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	

}
