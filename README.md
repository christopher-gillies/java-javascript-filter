# java-javascript-filter
This program uses the ScriptEngineManager to execute dynamic javascript code as a filter

### Use
Please see App.java or JavascriptFilterTest.java

### Build
First install maven 3.2+

git clone https://github.com/christopher-gillies/java-javascript-filter.git


```
cd ./java-javascript-filter.git
mvn package
```

Result will be:

```
./java-javascript-filter.gittarget/javascript_filter-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Run example
```
cd $DIR; 
java -jar ./java-javascript-filter.git/target/javascript_filter-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Use
Include the jar into your workspace or project and then import the class

```
import java.util.HashMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import com.sampsonlab.filter.JavascriptFilter;

/**
 * Example of how to use Javascript Filter
 *
 */
public class App 
{
    public static void main( String[] args ) throws ScriptException
    {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        	ScriptEngine engine = engineManager.getEngineByName("nashorn");

        	/*
        	 * 
        	 */
        	JavascriptFilter filter = JavascriptFilter.createWithEngine(engine);
        	String filterString = "SAMPLE_MAF < 0.2 && (( (POLYPHEN2_HVAR =~ /D/) + (MUTATIONTASTER =~ /[AD]/) + (SIFT =~ /D/) ) >= 2)";
        	filter.setDefaultFilterStr(filterString);
        	
        	double mafs[] = { 0.0, 0.1, 0.19, 0.2, 0.3 };
    		HashMap<String, Object> values = new HashMap<>();
    		values.put("POLYPHEN2_HVAR", "D,A");
    		values.put("MUTATIONTASTER", "A,.,.");
    		values.put("SIFT", "T");
    		
        	System.out.println("Filter: " + filterString);
        	for(double maf : mafs) {
        		values.put("SAMPLE_MAF", maf);
        		System.out.println("MAF: " + maf);
        		boolean res = filter.apply(values);
        		System.out.println("Result: " + res);
        	}
    }
}

```