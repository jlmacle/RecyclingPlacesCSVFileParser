package parser;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;





/**
 * @author Jean-Louis
 * The parser takes a CSV file as input, parses the fields and then stores the data in a database using Hibernate.
 * 
 * */
public class CSVFileParser {
	
	public static final Logger logger = LogManager.getLogger(CSVFileParser.class); 
	private boolean debugFields = true;
	private boolean debugEntry = true;

	
	public void parseCSV() {
		System.out.println("Entering parseCSV");
		Path csvFile = FileSystems.getDefault().getPath("src/main/resources", "recyclingPlaces.csv");
		try(BufferedReader reader = Files.newBufferedReader(csvFile, StandardCharsets.UTF_8))
		{
			String line = null;
			boolean firstLine = true;
			//The parser reads every line in the file (except for the first line that contains the fields names)
			if(debugEntry) {logger.debug("Getting hibernate.cfg.xml: "+FileSystems.getDefault().getPath("hibernate.cfg.xml").toString());}
			HibernateConnectionUtil.configureHibernate();
			Session session = HibernateConnectionUtil.getSessionFactory().openSession();
			Transaction tx = session.beginTransaction();
			
			while ((line=reader.readLine()) != null)
			{
				if(debugEntry) {logger.debug("loop");}
				
				if(firstLine) {firstLine=false; continue;}
				//The parser maps a field in the file with the corresponding field in the Title object
				Entry extractedDataEntry = this.parseLine(line);
				if(debugEntry) {logger.debug(extractedDataEntry.toString());};
				
				//The Title object is mapped to the database
				
				session.persist(extractedDataEntry);				
			}
			tx.commit();
		} 
		
		catch (IOException e) {
			logger.debug(e.getMessage());
		}
		finally {			
			HibernateConnectionUtil.shutdownSession();
			
		}
	}
		
		/**
		 * public ArrayList<String> splitData(String line)
		 * Parses the comma separated fields and stores the values in an ArrayList
		 * @param line the String to be parsed
		 * @return An ArrayList with all the fields parsed
		 */
		public ArrayList<String> splitData(String line) {
			
			String field = "";
			ArrayList<String> fields = new ArrayList<String>();	
			
				
			//get beginning of String until you find ',' or a 2nd '"'
			while (!line.equals(""))
			{
				//if no ','exists in the line  ,  there is only one field (that might need to be stripped from ")
				if (line.indexOf(',') == -1) {
					field  = line;
					if (field.indexOf('"') != -1) field = field.substring(1,field.length()-1);
					line = "";
					if (debugFields) {logger.debug(field);logger.debug(line);}
					fields.add(field);
				}
				else //else if ',' exists in the line
				{	
					//if '"' does not exist in the line
					if(line.indexOf('"') == -1) {
						//if no '"', the ',' cannot be inside a "......"; the string before ',' is therefore a field
						//substring(int beginIndex, int endIndex)
						//Returns a new string that is a substring of this string.
						field = line.substring(0, line.indexOf(','));
						//substring(int beginIndex)
						line = line.substring(line.indexOf(',')+1);	
						if (debugFields) {logger.debug(field);logger.debug(line);}
						fields.add(field);
					}
					else {	//if '"" does exist in the line (and ',' exists as well in the line	(no index value can be -1)	
						//either  ',' is before '"' , get the string before ',', that's a field
						if (line.indexOf(',') < line.indexOf('"')) {
							field = line.substring(0, line.indexOf(','));
							line = line.substring(line.indexOf(',')+1);
							if (debugFields) {logger.debug(field);logger.debug(line);}
							fields.add(field);
						}
						else {//or '"' is before ','   ,   get the String between the 2 '"', remember the ',' to remove from line
							int indexQuote1 = line.indexOf('"');
							//indexOf(int ch, int fromIndex)
							int indexQuote2 = line.indexOf('"',indexQuote1+1);
							field = line.substring(indexQuote1+1, indexQuote2);//Suppression of the quotes from the final string
							//extra scenario: =""0399231161""
							//if (field.indexOf('=')==0) field = field.substring(3, field.length()-3);
							
							line = line.substring(indexQuote2+1);
							//suppression of ',' if first character
							if (line.indexOf(',')==0) line = line.substring(1);
							if (debugFields) {logger.debug(field);logger.debug(line);}
							fields.add(field);
							
						}				
					}
				}
			}
				
			return fields;		
		}
			
		/**
		 * private Title parseLine(String line)
		 * Parses a line of the export file and store the chosen information in an instance of Title.
		 * [Not all information being of interest at the moment, only selected fields such as isbn, title, and author are being kept]
		 * @param A line of the csv file
		 * @return an instance of the Title class
		 */
		public Entry parseLine(String line) {
			
			Entry entry = new Entry();
			ArrayList<String> splitData = this.splitData(line);
			
			
			entry.setField1(splitData.get(0));
			entry.setField2(splitData.get(1));
			entry.setField3(splitData.get(2));
			entry.setField4(splitData.get(3));
			entry.setField5(splitData.get(4));
			entry.setField6(splitData.get(5));
			entry.setField7(splitData.get(6));
		
			return entry;
		}
	
	
	
	public static void main(String[] args) {
		CSVFileParser parser = new CSVFileParser();
		parser.parseCSV();
	}
}
