package com.sampsonlab.filter;

import java.util.Map;
import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.stringtemplate.v4.ST;

public class JavascriptFilter {
	
	private ScriptEngine engine;
	
	private final String filterWrapper = "function execute() {" +
	"\nvar res = ( <filterString> );" +
			"\nreturn res;" +
			"\n};" +
			"\nexecute();";
	private final ST template = new ST(filterWrapper);
	private final Logger logger = Logger.getLogger("JavascriptFilter");
	
	private String defaultFilterStr = null;
	private String transformedDefaultFilterStr = null;
	
	private JavascriptFilter() {
		
	}
	
	public static JavascriptFilter createWithEngine(ScriptEngine engine) {
		if(engine == null) {
			throw new IllegalArgumentException("Please specify an engine");
		}
		
		JavascriptFilter filter = new JavascriptFilter();
		
		filter.engine = engine;
		
		return filter;
	}
	
	
	public String getDefaultFilterStr() {
		return defaultFilterStr;
	}


	/**
	 * This function will set a default string that can be reused without re-transforming the string
	 * @param defaultFilterStr
	 */
	public void setDefaultFilterStr(String defaultFilterStr) {
		this.defaultFilterStr = defaultFilterStr;
		this.transformedDefaultFilterStr = transformFilterString(defaultFilterStr);
	}



	public String getTransformedDefaultFilterStr() {
		return transformedDefaultFilterStr;
	}



	/**
	 * 
	 * @param filterString
	 * @return transformedFilterString where perl style regular expressions are converted to javascript
	 */
	public String transformFilterString(String filterString) {
		String res = filterString.replaceAll("([^ ()]+)[ ]*=~[ ]*([/][^/]*[/])", "($1.match($2) !== null)");
		return res;
	}
	
	/** 
	 * This function should be used with defaultFilterString
	 * @param values
	 * @return true of false
	 * @throws ScriptException
	 */
	public boolean apply(Map<String, Object> values) throws ScriptException {
		if(transformedDefaultFilterStr == null) {
			throw new IllegalArgumentException("you must set default string");
		}
		return apply(transformedDefaultFilterStr, values, false);
	}
	
	/**
	 * 
	 * @param filterString (must be valid javascript code)
	 * @param values
	 * @return true or false
	 * @throws ScriptException
	 */
	public boolean apply(String filterString, Map<String, Object> values) throws ScriptException {
		return apply(filterString, values, false);
	}
	
	/**
	 * 
	 * @param filterString (must be valid javascript code, but allowing perl style regular expressions A =~ /B/)
	 * @param values
	 * @return true or false
	 * @throws ScriptException
	 */
	public boolean transformAndApply(String filterString, Map<String, Object> values) throws ScriptException {
		return transformAndApply(filterString,values);
	}
	
	/**
	 * 
	 * @param filterString (must be valid javascript code, but allowing perl style regular expressions A =~ /B/)
	 * @param values
	 * @param log enable logging
	 * @return true or false
	 * @throws ScriptException
	 */
	public boolean transformAndApply(String filterString, Map<String, Object> values, boolean log) throws ScriptException {
		String transformedString = this.transformFilterString(filterString);
		return apply(transformedString, values, log);
	}
	
	/**
	 * 
	 * @param filterString (must be valid javascript code
	 * @param values
	 * @param log enable logging
	 * @return true or false
	 * @throws ScriptException
	 */
	public boolean apply(String filterString, Map<String, Object> values, boolean log) throws ScriptException {
		
		/**
		 * Create a local scope for execution
		 */
		ScriptContext context = new SimpleScriptContext();
		Bindings engineScope = context.getBindings(ScriptContext.ENGINE_SCOPE);
		engineScope.clear();
		
		values.entrySet().forEach( entry -> {
			String key = entry.getKey();
			Object value = entry.getValue();
			engineScope.put(key, value);
		});
		
		if(template.getAttribute("filterString") != null) {
			template.remove("filterString");
		}
		
		template.add("filterString", filterString);
		
		
		
		String wrappedFilterString = template.render();
		

		
		Object resObj = engine.eval(filterString, context);
		
		boolean res;
		if(resObj instanceof Boolean) {
			res = (Boolean) resObj;
		} else {
			logger.info(wrappedFilterString);
			engineScope.entrySet().forEach( entry -> {
				String key = entry.getKey();
				Object value = entry.getValue();
				logger.info(key + " = " + value);
			});
			throw new IllegalStateException("Script error");
		}
		
		if(log) {
			logger.info(wrappedFilterString);
			logger.info(Boolean.toString(res));
		}
		
		return res;
	}
	
}
