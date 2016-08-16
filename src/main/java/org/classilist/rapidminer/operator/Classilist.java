package org.classilist.rapidminer.operator;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.OperatorProgress;
import com.rapidminer.operator.ProcessStoppedException;
import com.rapidminer.operator.io.AbstractStreamWriter;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.io.Encoding;

public class Classilist extends AbstractStreamWriter {
	
	
	
	/** The parameter name for &quot;The CSV file which should be written.&quot; */
	public static final String PARAMETER_CSV_FILE = "Location";

	/** The parameter name for the column separator parameter. */
	public static final String PARAMETER_COLUMN_SEPARATOR = "column_separator";

	/** Indicates if the attribute names should be written as first row. */
	public static final String PARAMETER_WRITE_ATTRIBUTE_NAMES = "write_attribute_names";

	/**
	 * Indicates if nominal values should be quoted with double quotes. Quotes inside of nominal
	 * values will be escaped by a backslash.
	 */
	public static final String PARAMETER_QUOTE_NOMINAL_VALUES = "quote_nominal_values";

	public static final String PARAMETER_APPEND_FILE = "append_to_file";

	
	public static final String PARAMETER_FORMAT_DATE = "format_date_attributes";

	public Classilist(OperatorDescription description) {
		super(description);
	}


	/**
	 * Writes the exampleSet with the {@link PrintWriter} out, using colSeparator as column
	 * separator.
	 *
	 * @param exampleSet
	 *            the example set to write
	 * @param out
	 *            the {@link PrintWriter}
	 * @param colSeparator
	 *            the column separator
	 * @param quoteNomValues
	 *            if {@code true} nominal values are quoted
	 * @param writeAttribNames
	 *            if {@code true} the attribute names are written into the first row
	 * @param formatDate
	 *            if {@code true} dates are formatted to "M/d/yy h:mm a", otherwise milliseconds
	 *            since the epoch are used
	 * @param opProg
	 *            the {@link OperatorProgress} is used to provide a more detailed progress.
	 *            Within this method the progress will be increased by number of examples times the
	 *            number of attributes. If you do not want the operator progress, just provide
	 *            <code> null <code>.
	 */
	public static void writeCSV(ExampleSet exampleSet, PrintWriter out, String colSeparator, boolean quoteNomValues,
			boolean writeAttribNames, boolean formatDate, OperatorProgress operatorProgress)
					throws ProcessStoppedException {
		writeCSV(exampleSet, out, colSeparator, quoteNomValues, writeAttribNames, formatDate, null, operatorProgress);
	}

	
	/**
	 * Writes the exampleSet with the {@link PrintWriter} out, using colSeparator as column
	 * separator and infinitySybol to denote infinite values.
	 *
	 * @param exampleSet
	 *            the example set to write
	 * @param out
	 *            the {@link PrintWriter}
	 * @param colSeparator
	 *            the column separator
	 * @param quoteNomValues
	 *            if {@code true} nominal values are quoted
	 * @param writeAttribNames
	 *            if {@code true} the attribute names are written into the first row
	 * @param formatDate
	 *            if {@code true} dates are formatted to "M/d/yy h:mm a", otherwise milliseconds
	 *            since the epoch are used
	 * @param infinitySymbol
	 *            the symbol to use for infinite values; if {@code null} the default symbol
	 *            "Infinity" is used
	 * @param opProg
	 *            the {@link OperatorProgress} is used to provide a more detailed progress.
	 *            Within this method the progress will be increased by number of examples times the
	 *            number of attributes. If you do not want the operator progress, just provide
	 *            <code> null <code>.
	 */
	public static void writeCSV(ExampleSet exampleSet, PrintWriter out, String colSeparator, boolean quoteNomValues,
			boolean writeAttribNames, boolean formatDate, String infinitySymbol, OperatorProgress opProg)
					throws ProcessStoppedException {
		String negativeInfinitySymbol = null;
		if (infinitySymbol != null) {
			negativeInfinitySymbol = "-" + infinitySymbol;
		}
		String columnSeparator = colSeparator;
	
		
		// check is predicted column name is correct or not else throw exception
		Iterator<Attribute> b = exampleSet.getAttributes().allAttributes();	
		int i = 0 , predInd = -1;
		boolean correct=false , classCorr = false , fcorr = false , prcorr = false;
		String classCol="";
		while (b.hasNext()) {
			
			Attribute attribute = b.next();
			String clnm = attribute.getName();
        	int j = clnm.indexOf("prediction(");
        	int k = clnm.indexOf(")");
        	if(j != -1 && k != -1 ) 
        		{
        		correct = true;
        		predInd = i;
        		//get the actual class column name
        		for(j += 11;j<k;j++) classCol = classCol.concat(clnm.charAt(j)+"");
        		
        		break;
        		}
        	i++;
        	}
        if (!correct) {
        	LogService.getRoot().log(Level.INFO, "Predicted Column not Defined");
        	throw new ProcessStoppedException();
        }

		// write column names
		// Change column names as per following scheme
        // Actual Class column - A-<classCol> : having column name same as classCol
        // Predicted Class Column - Predicted : column at index predInd
        // Class Probabilities Column - P-<className> : having format "confidence(className)"
        // Features - F-<attributeName> : all others
		if (true) {
			Iterator<Attribute> a = exampleSet.getAttributes().allAttributes();		
			boolean first = true;
			i = 0;
			while (a.hasNext()) {
				if (!first) {
					out.print(columnSeparator);
				}
				Attribute attribute = a.next();
				String name = attribute.getName();
				
				String newC="";
                // assign new column names to newC
                
                if(name.equals(classCol)) //class column
                {
                	
                	newC = "A-"+classCol;
                	classCorr = true;
                }
                else if(i == predInd) //predicted column
                	newC = "Predicted";
                else if(name.contains("confidence(")) //probability column
                {
                	prcorr = true;
                	newC = "";
                	int j = name.indexOf('(');
                	int k = name.indexOf(')');
                	for(j += 1; j<k ;j++)
                		newC = newC.concat(name.charAt(j)+"");
                	
                	newC = "P-"+newC;
                }
                else // feature column
                {
                	fcorr = true;
                	newC = "F-"+name;
                }		
				// Done with column name changes
				
				if (true) {
					newC = newC.replaceAll("\"", "'");
					newC = "\"" + newC + "\"";
				}
				out.print(newC);
				first = false;
				i++;
			}
			out.println();
		}
		if(!classCorr) {
			LogService.getRoot().log(Level.INFO, "Target Class Column not Defined");
        	throw new ProcessStoppedException();		
        }
		if (!fcorr) {
        	LogService.getRoot().log(Level.INFO, "Features not Defined");
        	throw new ProcessStoppedException();
        }
		if (!prcorr) {
        	LogService.getRoot().log(Level.INFO, "Class Probabilities not Defined");
        	throw new ProcessStoppedException();
        }
		
    	LogService.getRoot().log(Level.INFO, "Class columns correctly Defined");


		// write data
		int progressCounter = 0;
		for (Example example : exampleSet) {
			Iterator<Attribute> a = exampleSet.getAttributes().allAttributes();
			boolean first = true;
			while (a.hasNext()) {

				Attribute attribute = a.next();
				if (!first) {
					out.print(columnSeparator);
				}
				if (!Double.isNaN(example.getValue(attribute))) {
					if (attribute.isNominal()) {
						String stringValue = example.getValueAsString(attribute);
						if (true) {
							stringValue = stringValue.replaceAll("\"", "'");
							stringValue = "\"" + stringValue + "\"";
						}
						out.print(stringValue);
					} else {
						Double value = example.getValue(attribute);
						if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), Ontology.DATE_TIME)) {
							if (formatDate) {
								Date date = new Date(value.longValue());
								String s = DateFormat.getInstance().format(date);
								out.print(s);
							} else {
								out.print(value);
							}
						} else {
							if (value.isInfinite() && infinitySymbol != null) {
								if (Double.POSITIVE_INFINITY == value) {
									out.print(infinitySymbol);
								} else {
									out.print(negativeInfinitySymbol);
								}
							} else {
								out.print(value);
							}
						}

					}
				}
				first = false;
			}

			out.println();

			// trigger operator progress every 100 examples
			if (opProg != null) {
				++progressCounter;
				if (progressCounter % 100 == 0) {
					opProg.step(100);
					progressCounter = 0;
				}
			}
		}
	}

	@Override
	public void writeStream(ExampleSet exampleSet, java.io.OutputStream outputStream) throws OperatorException {

		String columnSeparator = ",";
		boolean quoteNominalValues = getParameterAsBoolean(PARAMETER_QUOTE_NOMINAL_VALUES);
		boolean writeAttribNames = getParameterAsBoolean(PARAMETER_WRITE_ATTRIBUTE_NAMES);
		boolean formatDate = getParameterAsBoolean(PARAMETER_FORMAT_DATE);
		PrintWriter out = null;
		try {
			out = new PrintWriter(new OutputStreamWriter(outputStream, Encoding.getEncoding(this)));

			// init operator progress
			getProgress().setTotal(exampleSet.size() * exampleSet.getAttributes().allSize());

			writeCSV(exampleSet, out, columnSeparator, quoteNominalValues, writeAttribNames, formatDate,
					getProgress());
			out.flush();
			getProgress().complete();
		} finally {
			if (out != null) {
				out.close();
			}
		}

	}

	@Override
	protected boolean supportsEncoding() {
		return true;
	}

	@Override
	protected boolean shouldAppend() {
		return getParameterAsBoolean(PARAMETER_APPEND_FILE);
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = new LinkedList<ParameterType>();
		types.add(makeFileParameterType());
		 types.add(new ParameterTypeFile(PARAMETER_CSV_FILE,
		 "Data Export Location", "csv", false));

		types.addAll(super.getParameterTypes());
		return types;
	}

	@Override
	protected String getFileParameterName() {
		return PARAMETER_CSV_FILE;
	}

	@Override
	protected String[] getFileExtensions() {
		return new String[] { "csv" };
	}


	

}
