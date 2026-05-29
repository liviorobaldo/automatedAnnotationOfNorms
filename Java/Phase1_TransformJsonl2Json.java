import org.apache.jena.atlas.json.*;
import java.io.*;
import java.util.Iterator;

public class Phase1_TransformJsonl2Json 
{
    private static final File CORPUS = new File("../CORPUS");
    private static final String FILE_NAME = "result.jsonl";
    private static final String OUTPUT_FILE_NAME = "result-in-json.json";

    public static void main(String[] args) 
    {
        JsonObject finalResult = new JsonObject() 
        {
            @Override
            public String toString() 
            {
                StringBuilder sb = new StringBuilder();
                sb.append("{\n");
                Iterator<String> keyIterator = this.keySet().iterator();
                while (keyIterator.hasNext()) 
                {
                    String sectionKey = keyIterator.next();
                    JsonArray array = this.get(sectionKey).getAsArray();
                    sb.append("\t\"").append(sectionKey).append("\":\n\t[\n");
                    for(int i=0; i<array.size(); i++) 
                    {
                        JsonObject innerObj = array.get(i).getAsObject();
                        sb.append("\t\t{\n");
                        Iterator<String> fieldIterator = innerObj.keySet().iterator();
                        while (fieldIterator.hasNext()) 
                        {
                            String fieldKey = fieldIterator.next();
                            String fieldValue = innerObj.getString(fieldKey);
                            sb.append("\t\t\t\"").append(fieldKey).append("\": \"").append(fieldValue).append("\"");
                            if(fieldIterator.hasNext())sb.append(",");
                            sb.append("\n");
                        }
                        sb.append("\t\t}");
                        if(i<array.size()-1)sb.append(",");
                        sb.append("\n");
                    }
                    sb.append("\t]");
                    if(keyIterator.hasNext())sb.append(",");
                    sb.append("\n");
                }
                sb.append("}");
                return sb.toString();
            }
        };

        File targetFile = new File(CORPUS,FILE_NAME);
        if(!targetFile.exists())
        {
            System.err.println("Error: File not found at "+targetFile.getAbsolutePath());
            return;
        }

        try(BufferedReader reader=new BufferedReader(new FileReader(targetFile))) 
        {
            String line;
            while((line=reader.readLine())!=null) 
            {
                if(line.trim().isEmpty())continue;
                JsonObject rootNode = JSON.parse(line);
                JsonObject response = rootNode.getObj("response");
                if(response==null)continue;
                JsonObject body = response.getObj("body");
                if(body==null)continue;
                JsonArray choices = body.get("choices").getAsArray();
                if(choices==null||choices.isEmpty())continue;
                JsonObject firstChoice = choices.get(0).getAsObject();
                JsonObject message = firstChoice.getObj("message");
                if(message==null)continue;
                String contentString = message.getString("content");
                if(contentString==null)continue;
                String validJsonContent = "{" + contentString + "}";
                JsonObject contentJson = JSON.parse(validJsonContent);
                for(String sectionKey:contentJson.keySet()) 
                {
                    JsonArray sectionArray = contentJson.get(sectionKey).getAsArray();
                    if(sectionArray!=null) 
                    {
                        if (!finalResult.hasKey(sectionKey))finalResult.put(sectionKey,new JsonArray());
                        JsonArray targetArray = finalResult.get(sectionKey).getAsArray();
                        for(JsonValue item:sectionArray)targetArray.add(item);
                    }
                }
            }
            
            try(PrintWriter writer=new PrintWriter(new FileWriter(new File(CORPUS,OUTPUT_FILE_NAME)))){writer.print(finalResult.toString());}
            System.out.println("Done!");
        }
        catch (Exception e) 
        {
            System.out.println("Exception: "+e.getMessage());
        }
    }
}
