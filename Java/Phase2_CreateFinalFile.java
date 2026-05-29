import org.apache.jena.atlas.json.*;
import java.io.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;

public class Phase2_CreateFinalFile 
{
    public static void main(String[] args) 
    {
        String partJsonPath = "../CORPUS/Phase1/Children Act 1989/part3.json";
        String resultJsonlPath = "../CORPUS/Phase2/Children Act 1989/result.jsonl";
        String outputJsonPath = "../CORPUS/Phase2/Children Act 1989/final_output.json";

        try 
        {
            executeMerge(partJsonPath, resultJsonlPath, outputJsonPath);
            System.out.println("Done!");
        } 
        catch(Exception e) 
        {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    public static void executeMerge(String partJsonPath, String resultJsonlPath, String outputJsonPath) throws Exception 
    {
        Map<String, JsonValue> masterOrderedMap = new LinkedHashMap<>();
        File partFile = new File(partJsonPath);
        if(partFile.exists()) 
        {
            JsonObject partRoot = JSON.read(partJsonPath);
            for(String key:partRoot.keySet()) 
            {
                if(key.equalsIgnoreCase("to-exclude"))continue;
                masterOrderedMap.put(key, partRoot.get(key));
            }
        }else throw new IOException("Base file " + partJsonPath + " is required.");

        File resultFile = new File(resultJsonlPath);
        if(!resultFile.exists())throw new IOException("Result file " + resultJsonlPath + " is missing.");

        try(BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(resultFile), StandardCharsets.UTF_8))) 
        {
            String line;
            int lineNumber = 0;
            while((line=br.readLine())!=null) 
            {
                lineNumber++;
                if(line.trim().isEmpty())continue;
                
                JsonObject lineNode = JSON.parse(line);    
                if(!lineNode.hasKey("response"))continue;
                JsonValue responseVal = lineNode.get("response");
                if(!responseVal.isObject())continue;

                JsonObject responseObj = responseVal.getAsObject();
                if(!responseObj.hasKey("body"))continue;
                JsonValue bodyVal = responseObj.get("body");
                if(!bodyVal.isObject())continue;

                JsonObject bodyObj = bodyVal.getAsObject();
                if (!bodyObj.hasKey("choices"))continue;
                JsonValue choicesVal = bodyObj.get("choices");
                if (!choicesVal.isArray())continue;

                JsonArray choicesArr = choicesVal.getAsArray();
                if (choicesArr.isEmpty())continue;

                JsonObject firstChoice = choicesArr.get(0).getAsObject();
                JsonObject messageObj = firstChoice.get("message").getAsObject();
                String rawContent = messageObj.get("content").getAsString().value().trim();

                if(!rawContent.startsWith("{"))rawContent="{" + rawContent + "}";

                JsonObject contentJson = JSON.parse(rawContent);
                for(String incomingKey:contentJson.keySet()) 
                {
                    String cleanKey = incomingKey;
                    if(!masterOrderedMap.containsKey(cleanKey) && cleanKey.contains("section-")) 
                    {
                        int sectionIndex = cleanKey.indexOf("section-");
                        cleanKey = cleanKey.substring(sectionIndex);
                    }
                    if(masterOrderedMap.containsKey(cleanKey))masterOrderedMap.put(cleanKey, contentJson.get(incomingKey));
                }
            }
        }

        Map<String, JsonArray> cleanOrderedMap = new LinkedHashMap<>();
        for(Map.Entry<String, JsonValue> entry:masterOrderedMap.entrySet()) 
        {
            String key = entry.getKey();
            JsonValue val = entry.getValue();
            if(val.isArray()) 
            {
                JsonArray originalArray = val.getAsArray();
                JsonArray filteredArray = new JsonArray();
                for(int i=0;i<originalArray.size();i++) 
                {
                    JsonValue element = originalArray.get(i);
                    if(element.isObject()) 
                    {
                        JsonObject obj = element.getAsObject();
                        if(!obj.hasKey("ADD-CONDITIONS-TO"))filteredArray.add(obj);
                    }else filteredArray.add(element);
                }

                if(!filteredArray.isEmpty())cleanOrderedMap.put(key, filteredArray);
            }
        }

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\n");
        int entriesCount = cleanOrderedMap.size();
        int index = 0;
        for(Map.Entry<String,JsonArray> entry:cleanOrderedMap.entrySet()) 
        {
            index++;
            String key = entry.getKey();
            JsonArray arrayValues = entry.getValue();
            jsonBuilder.append("\t\"").append(key).append("\": \n\t[\n");
            for(int i=0;i<arrayValues.size();i++) 
            {
                JsonValue arrayElement = arrayValues.get(i);
                if(arrayElement.isObject()) 
                {
                    jsonBuilder.append("\t\t{\n");
                    JsonObject innerObj = arrayElement.getAsObject();
                    int fieldCount = innerObj.keySet().size();
                    int fieldIdx = 0;
                    for (String fKey : innerObj.keySet()) 
                    {
                        fieldIdx++;
                        String fVal = innerObj.get(fKey).toString();    
                        if(fVal.startsWith("\"") && fVal.endsWith("\""))fVal=fVal.substring(1,fVal.length()-1);
                        fVal = unescapeUnicode(fVal);
                        if(fKey.contains("CONDITIONS"))fVal=fVal.replaceAll("[\\r\\n\\t]+", " ").replaceAll("\\s+,", " ,").replaceAll("\\s+", " ").trim();
                        jsonBuilder.append("\t\t\t\"").append(fKey).append("\": \"").append(fVal).append("\"");
                        if(fieldIdx<fieldCount)jsonBuilder.append(",");
                        jsonBuilder.append("\n");
                    }
                    
                    jsonBuilder.append("\t\t}");
                    if(i<arrayValues.size()-1)jsonBuilder.append(",");
                    jsonBuilder.append("\n");
                }
            }
            jsonBuilder.append("\t]");
            if(index<entriesCount)jsonBuilder.append(",");
            jsonBuilder.append("\n\n");
        }
        
        if(jsonBuilder.toString().endsWith("\n\n"))jsonBuilder.setLength(jsonBuilder.length()-1);
        jsonBuilder.append("}");

        // 5. Output literal UTF-8 string to the file system
        try(BufferedWriter writer=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputJsonPath),StandardCharsets.UTF_8)))
        {writer.write(jsonBuilder.toString());}
    }

    private static String unescapeUnicode(String input) 
    {
        Pattern pattern = Pattern.compile("\\\\u([0-9A-Fa-f]{4})");
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        while(matcher.find())matcher.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf((char)Integer.parseInt(matcher.group(1),16))));
        matcher.appendTail(sb);
        return sb.toString();
    }
}