package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.StringReader;
import java.io.StringWriter;

public class Converter {
    
    /*
        
        Consider the following CSV data, a portion of a database of episodes of
        the classic "Star Trek" television series:
        
        "ProdNum","Title","Season","Episode","Stardate","OriginalAirdate","RemasteredAirdate"
        "6149-02","Where No Man Has Gone Before","1","01","1312.4 - 1313.8","9/22/1966","1/20/2007"
        "6149-03","The Corbomite Maneuver","1","02","1512.2 - 1514.1","11/10/1966","12/9/2006"
        
        (For brevity, only the header row plus the first two episodes are shown
        in this sample.)
    
        The corresponding JSON data would be similar to the following; tabs and
        other whitespace have been added for clarity.  Note the curly braces,
        square brackets, and double-quotes!  These indicate which values should
        be encoded as strings and which values should be encoded as integers, as
        well as the overall structure of the data:
        
        {
            "ProdNums": [
                "6149-02",
                "6149-03"
            ],
            "ColHeadings": [
                "ProdNum",
                "Title",
                "Season",
                "Episode",
                "Stardate",
                "OriginalAirdate",
                "RemasteredAirdate"
            ],
            "Data": [
                [
                    "Where No Man Has Gone Before",
                    1,
                    1,
                    "1312.4 - 1313.8",
                    "9/22/1966",
                    "1/20/2007"
                ],
                [
                    "The Corbomite Maneuver",
                    1,
                    2,
                    "1512.2 - 1514.1",
                    "11/10/1966",
                    "12/9/2006"
                ]
            ]
        }
        
        Your task for this program is to complete the two conversion methods in
        this class, "csvToJson()" and "jsonToCsv()", so that the CSV data shown
        above can be converted to JSON format, and vice-versa.  Both methods
        should return the converted data as strings, but the strings do not need
        to include the newlines and whitespace shown in the examples; again,
        this whitespace has been added only for clarity.
        
        NOTE: YOU SHOULD NOT WRITE ANY CODE WHICH MANUALLY COMPOSES THE OUTPUT
        STRINGS!!!  Leave ALL string conversion to the two data conversion
        libraries we have discussed, OpenCSV and json-simple.  See the "Data
        Exchange" lecture notes for more details, including examples.
        
    */
    
    @SuppressWarnings("unchecked")
    public static String csvToJson(String csvString) {
        
        String result = "{}"; // default return value; replace later!
        
        try {
        
            // INSERTING MY CODE HERE
            /*CSVReader reader = new CSVReader(new StringReader(csvString)); 
            ArrayList<String[]> rows = (ArrayList<String[]>) reader.readAll();

            if (rows.size() < 1) return result;  // empty CSV

            String[] headers = rows.get(0);

            JsonArray jsonArray = new JsonArray();

            // iterate over data rows
            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                JsonObject obj = new JsonObject();

                for (int j = 0; j < headers.length; j++) {
                    String colName = headers[j];
                    String val = row[j];

                    //convert numeric strings to Integer if possible
                    if (isInteger(val)) {
                        obj.put(colName, Integer.parseInt(val));
                    } else {
                        obj.put(colName, val);
                    }
                }
                jsonArray.add(obj);
            }
                result = Jsoner.serialize(jsonArray);
            */ //I was trying to make something more generic before I realized the prodNums section of the json and that really stumped me (and now I'm finding out I was just being stumped in the other method by the episodes leading 0s for a similar reason)
            CSVReader reader = new CSVReader(new StringReader(csvString)); 
            ArrayList<String[]> rows = new ArrayList<>(reader.readAll());
            if (rows.isEmpty()) return result;

            String[] headers = rows.get(0);  // first row: all headers

            JsonArray colHeadings = new JsonArray();
            for (String h : headers) {
                colHeadings.add(h);
            }

            JsonArray prodNums = new JsonArray(); // first column of each data row
            JsonArray data = new JsonArray();     // all other columns in data rows

            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);

                // Add first column value or null if missing
                prodNums.add(row.length > 0 ? row[0] : null);

                JsonArray rowData = new JsonArray();

                // Add remaining columns (index 1 and beyond)
                for (int j = 1; j < headers.length; j++) {
                    if (j < row.length) {
                        String val = row[j];
                        // Optional: parse integers, else keep string
                        if (isInteger(val)) {
                            rowData.add(Integer.parseInt(val));
                        } else {
                            rowData.add(val);
                        }
                    } else {
                        rowData.add(null);
                    }
                }
                data.add(rowData);
            }

            JsonObject root = new JsonObject();
            root.put("ProdNums", prodNums);
            root.put("ColHeadings", colHeadings);
            root.put("Data", data);

            result = Jsoner.serialize(root);

        

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        return result.trim();
        
    }
    
    @SuppressWarnings("unchecked")
    public static String jsonToCsv(String jsonString) {
        
        String result = ""; // default return value; replace later!
        
        try {
            
            // INSERT YOUR CODE HERE

            // Parse JSON string into object
            JsonObject json = (JsonObject) Jsoner.deserialize(jsonString, new JsonObject());

            JsonArray prodNums = (JsonArray) json.get("ProdNums");
            JsonArray colHeadings = (JsonArray) json.get("ColHeadings");
            JsonArray data = (JsonArray) json.get("Data");

            // Prepare CSV output
            StringWriter sw = new StringWriter();
            CSVWriter writer = new CSVWriter(sw);

            // Write header row
            String[] headers = new String[colHeadings.size()];
            for (int i = 0; i < colHeadings.size(); i++) {
                headers[i] = colHeadings.get(i).toString();
            }
            writer.writeNext(headers);

            for (int i = 0; i < data.size(); i++) {
                JsonArray row = (JsonArray) data.get(i);
                String[] csvRow = new String[headers.length];

                // First value is ProdNum
                csvRow[0] = prodNums.get(i).toString();

                // Fill in rest of the columns from Data (starting at index 1)
                for (int j = 1; j < headers.length; j++) {
                    Object val = row.get(j - 1); // shift left by one

                    if (headers[j].equals("Episode")) {
                        try {
                            int ep = Integer.parseInt(val.toString());
                            csvRow[j] = String.format("%02d", ep); // leading 0 (bane of my existence)
                        }
                        catch (NumberFormatException e) {
                            csvRow[j] = val.toString(); //just in case
                        }
                    }
                    else {
                        csvRow[j] = val.toString();
                    }
                }

                writer.writeNext(csvRow);
            }

            writer.close();
            result = sw.toString();
            
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
        /*System.out.println("===== CSV Output Start =====");
System.out.println(result);
System.out.println("===== CSV Output End =====");*/
        
        return result.trim();
        
    }
    
    //I saw this implementation on stackoverflow and I find it really funny so I wanted to use it
    public static boolean isInteger(String s) {
    try { 
        Integer.parseInt(s); 
    } catch(NumberFormatException e) { 
        return false; 
    } catch(NullPointerException e) {
        return false;
    }
    // only got here if we didn't return false
    return true;
}
    
}
