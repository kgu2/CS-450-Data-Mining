import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AmazonReviewClassifcation {

	private static ArrayList<String> trainingDoc;
	private static ArrayList<String> testDoc;
	private static ArrayList<String> labels;
	public static final int NUM_LINES = 18506;		

	public static void main(String[] args) throws IOException
	{
		File testFile = new File("./test.dat");
		File trainingFile = new File("./train_file.dat");
		
		try {
			// gets labels from training set and adds them to a list
			labels = getLabel(trainingFile);
			
			// Does PreProcessing on training doc  
			trainingDoc = openFile(trainingFile);		
			
			// Does PreProcessing on test doc
			testDoc = openFile(testFile);			

			ArrayList<ArrayList<String>> matrixTraining = new ArrayList<ArrayList<String>>();				
			ArrayList<ArrayList<String>> matrixTest= new ArrayList<ArrayList<String>>();					

			// Create an ArrayList matrix or list of lists for training and test documents.
			// Creates a list of documents. Each line in the file represents one document. 
			// Each document contains a list of words
			for(int i = 0; i < NUM_LINES; i ++)
			{
				ArrayList<String> wordArrayList = new ArrayList<String>();
				for(String word : trainingDoc.get(i).split(" ")) 
				{
					wordArrayList.add(word);
				}
				matrixTraining.add(wordArrayList);
			}

			for(int i = 0; i < NUM_LINES; i ++)
			{
				ArrayList<String> wordArrayList = new ArrayList<String>();
				for(String word : testDoc.get(i).split(" ")) 
				{
					wordArrayList.add(word);
				}
				matrixTest.add(wordArrayList);
			}

			// Computes tfIDF for each term and create a tfIDF matrix for training data
			ArrayList<ArrayList<Double>> tfIdfTraining = new ArrayList<ArrayList<Double>>();
			for(int i = 0; i < NUM_LINES; i ++)
			{
				ArrayList<Double> numList = new ArrayList<Double>();
				for(int word = 0; word < matrixTraining.get(i).size(); word ++)
				{
					numList.add(calculateTfIdf(matrixTraining.get(i), matrixTraining, matrixTraining.get(i).get(word)));
				}
				tfIdfTraining.add(numList);
			}

			// Computes tfIDF for each term and create a tfIDF matrix for test data
			ArrayList<ArrayList<Double>> tfIdfTest = new ArrayList<ArrayList<Double>>();
			for(int i = 0; i < NUM_LINES; i ++)
			{
				ArrayList<Double> numList = new ArrayList<Double>();
				for(int word = 0; word < matrixTest.get(i).size(); word ++)
				{
					numList.add(calculateTfIdf(matrixTest.get(i), matrixTest, matrixTest.get(i).get(word)));
				}
				tfIdfTest.add(numList);
			}
			
			// the max number of columns or features for tfIdf matrix is 1000
			int maxDimensions = 1000;
			
			// append 0's to both matrices to ensure equal dimensions
			tfIdfTraining = appendToLength(tfIdfTraining, maxDimensions);
			tfIdfTest = appendToLength(tfIdfTest, maxDimensions);
			
			// used to output results to file
			FileWriter fileWriter = new FileWriter("Result.dat");
			PrintWriter printWriter = new PrintWriter(fileWriter);
			
			// KNN implementation
			for(int a = 0; a < NUM_LINES; a++)
			{
				ArrayList<Element> cosineSimilarity = new ArrayList<Element>();
				
				// creates the test vector
				double[] vectorTest = listToArray(tfIdfTest.get(a));

				// compute cosine similarity for test vector to all training vectors
				for(int i = 0; i < NUM_LINES; i ++)
				{
					// creates training vector
					double[] vectorTraining = listToArray(tfIdfTraining.get(i));
					
					// computes cosine similarity and adds it to the list, also keeping track of the index location
					double t = calculateCosineSimilarity(vectorTest, vectorTraining);
			    	if(Double.isInfinite(t) || Double.isNaN(t))
			    	{
			    		t = 0.0;	
			    	}
					Element e = new Element(i, t);
					cosineSimilarity.add(e);
				}

				// Sort the list in descending order
				Collections.sort(cosineSimilarity);
				Collections.reverse(cosineSimilarity);

				// Choose K nearest neighbors to be 5
				int k = 5;
				
				// counter for positive or negative labels
				int countResult = 0;
				
				// gets the top 5 similarity values and determines whether majority is positive or negative
				for(int i = 0; i < k; i ++)
				{
					// gets location from cosine similarity list prior to being sorted
					int index = cosineSimilarity.get(i).getindex();
					
					// finds the location in training set and gets the corresponding label,
					// adding 1 to counter if positive, or -1 if negative
					countResult += getLabelAtIndex(labels, index);	
				}

				// label is positive if there are more positive neighbors
				if(countResult > 0)
				{
					printWriter.print("+1\n");
				}
				else
				{
					printWriter.print("-1\n");
				}
			
			}
			printWriter.close();	
			fileWriter.close();	
			
		} catch (FileNotFoundException e) {
			System.out.println("Can not find file");
		}
	}

	// This method opens the given file, does PreProcessing and puts the file into an 
	// arrayList of documents with each line representing a separate document.
	// All PreProcessing operations uses regular expressions
	public static ArrayList<String> openFile(File file) throws FileNotFoundException 
	{
		Scanner scan = new Scanner(file);
		ArrayList<String> document = new ArrayList<String>();

		while(scan.hasNextLine())
		{
			// removes punctuation
			String line = scan.nextLine().replaceAll("\\p{Punct}","");		
			
			// removes numbers
			line = line.replaceAll("\\d","");						
			
			// lower case
			line = line.toLowerCase();	
		
			// removes words 3 characters or less
			line = line.replaceAll("\\b\\w{1,3}\\b\\s?", "");				
			
			// removes stop words
			line = removeStopwords(line);								
			
			// removes any remaining whitespace
			line = line.replaceAll("\\s+"," ");  							
			
			document.add(line);
		}
		scan.close();
		return document;
	}


	// This method returns the first element of each line in a file.
	// This is used to get a list of labels from the training file
	public static ArrayList<String> getLabel(File file) throws FileNotFoundException
	{
		Scanner scan = new Scanner(file);
		ArrayList<String> labels = new ArrayList<String>();
		while(scan.hasNextLine())
		{
			labels.add(scan.next());
			scan.nextLine();
		}		

		scan.close();
		return labels;
	}

	// This method takes in a list of labels and an index. 
	// It returns 1 if the label is '+1', otherwise it returns -1 
	public static int getLabelAtIndex(ArrayList<String> labels, int index)
	{
		if(labels.get(index).equals("+1"))
		{
			return 1;
		}
		else return -1;
	}

	// This method is used to remove stop words. Using a predetermined string of stop words,
	// removes all instances of those words in a given string
	public static String removeStopwords(String line)	
	{
		Pattern p = Pattern.compile("\\b(this|they|them|their|what|here|been|such|with|when|that|"
				+ "then|those|myself|ours|ourselves|your|yours|yourself|yourselves|himself|herself|)\\b\\s?");		
		
		Matcher m = p.matcher(line);
		String s = m.replaceAll("");
		return s;
	}

	// This method calculates the term frequency of a given term. The term frequency represents the total
	// number of times it appears in a given document
	public static double calculateTermFrequency(ArrayList<String> document, String terms) 
	{
		double result = 0;
		for (String word : document) 
		{
			if(terms.equals(word))
			{
				result++;
			}
		}
		return (result / document.size());
	}

	// This method computes the IDF. The IDF represents the significance of the term in the entire set of documents
	public static double calculateIdf(ArrayList<ArrayList<String>> documents, String term)
	{
		double n = 0;
		for (List<String> doc : documents)
		{
			for (String word : doc) 
			{
				if (term.equals(word))
				{
					n++;
					break;
				}
			}
		}
		
		if(n == 0)
		{
			return 0.0;
		}
		return Math.log(documents.size() / n);
	}

	// This method calculates the TFIDF of a term. It is equal to term frequency * IDF
	public static double calculateTfIdf(ArrayList<String> document, ArrayList<ArrayList<String>> documents, String term) 
	{
		return calculateTermFrequency(document, term) * calculateIdf(documents, term);
	}

	// This method computes the cosine similarity given two vectors of equal length
	public static double calculateCosineSimilarity(double[] vectorA, double[] vectorB)
	{
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;
		for (int i = 0; i < vectorA.length; i++) 
		{
			dotProduct += vectorA[i] * vectorB[i];
			normA += Math.pow(vectorA[i], 2);
			normB += Math.pow(vectorB[i], 2);
		}   
		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}

	// This method takes in a list and converts that list into an array
	public static double[] listToArray(ArrayList<Double> list)
	{
		double[] target = new double[list.size()];
		for (int i = 0; i < list.size(); i++)
		{
			target[i] = list.get(i);               
		}
		return target;
	}

	// This method takes an arrayList and appends 0's to a given length
	// It will keep removing the last element if the list is bigger than the given length
	public static ArrayList<ArrayList<Double>> appendToLength(ArrayList<ArrayList<Double>> list, int length)
	{

		for(int i = 0; i < list.size(); i ++)
		{
			while(list.get(i).size() < length)
			{
				list.get(i).add(0.0);
			}
			while(list.get(i).size() > length)
			{
				list.get(i).remove(list.get(i).size());
			}
		}
		return list;
	}

	// This method prints the label to an output file
	public static void printLabel(String label)
	{

		try {
			FileWriter fileWriter = new FileWriter("Result.dat");
			PrintWriter printWriter = new PrintWriter(fileWriter);

			if(label.equals("+1"))
			{
				printWriter.print("+1\n");
			}
			else 
			{
				printWriter.print("-1\n");
			}

			printWriter.close();	
			fileWriter.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
