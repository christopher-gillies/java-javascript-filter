package com.sampsonlab.filter;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.junit.Before;
import org.junit.Test;

public class JavascriptFilterTest {
	
	ScriptEngineManager engineManager;
	ScriptEngine engine;
	
	JavascriptFilter filter;
	
	Logger logger;
	
    @Before
    public void setUp() {
         engineManager = new ScriptEngineManager();
         engine = engineManager.getEngineByName("nashorn");
         filter = JavascriptFilter.createWithEngine(engine);
         logger = Logger.getLogger("JavascriptFilterTest");
    }
	
	@Test
	public void testTransform() throws ScriptException {
		
		String filterString = "A =~ /B/ && C =~ /D/";
		
		String transformedFilterString = filter.transformFilterString(filterString);
		
		logger.info(filterString);
		logger.info(transformedFilterString);
		
		assertEquals("(A.match(/B/) !== null) && (C.match(/D/) !== null)",transformedFilterString);
		
		HashMap<String, Object> values = new HashMap<>();
		values.put("A", "B,F");
		values.put("C", "DE");
		
		boolean res = filter.apply(transformedFilterString, values, true);
		
		assertTrue(res);
	}
	
	@Test
	public void testTransform2() throws ScriptException {
		
		String filterString = "SAMPLE_MAF < 0.2 && POLYPHEN2_HVAR =~ /D/";
		
		String transformedFilterString = filter.transformFilterString(filterString);
		
		logger.info(filterString);
		logger.info(transformedFilterString);
		
		assertEquals("SAMPLE_MAF < 0.2 && (POLYPHEN2_HVAR.match(/D/) !== null)",transformedFilterString);
		
	}
	
    
	@Test
	public void testSimpleTrue() throws ScriptException {
		
		String filterString = "1 + 1 == 2";
		HashMap<String, Object> values = new HashMap<>();
		
		boolean res = filter.apply(filterString, values, true);
		
		assertTrue(res);
	}
	
	@Test
	public void testSimpleFalse() throws ScriptException {
		
		String filterString = "1 + 1 != 2";
		HashMap<String, Object> values = new HashMap<>();
		
		boolean res = filter.apply(filterString, values, true);
		
		assertFalse(res);
	}
	
	
	@Test
	public void testEVSOnly() throws ScriptException {
		
		String filterString = "EVS < 0.01";
		HashMap<String, Object> values = new HashMap<>();
		values.put("EVS", 1);
		boolean res = filter.apply(filterString, values, true);
		assertFalse(res);
		
		values.put("EVS", 0);
		res = filter.apply(filterString, values, true);
		assertTrue(res);
	}
	
	@Test
	public void testSampleMafAndPolyPhen() throws ScriptException {
		
		String filterString = "SAMPLE_MAF < 0.2 && POLYPHEN2_HVAR =~ /[DA]/";
		HashMap<String, Object> values = new HashMap<>();
		values.put("SAMPLE_MAF", 0.1);
		values.put("POLYPHEN2_HVAR", "D,A");
		boolean res = filter.transformAndApply(filterString, values, true);
		assertTrue(res);
		
		values.put("SAMPLE_MAF", 0.2);
		res = filter.transformAndApply(filterString, values, true);
		assertFalse(res);
		
		values.put("POLYPHEN2_HVAR", "C");
		res = filter.transformAndApply(filterString, values, true);
		assertFalse(res);
		
	}
	
	@Test
	public void testFullFilter() throws ScriptException {
		String filterString = "SAMPLE_MAF < 0.2 && (( (POLYPHEN2_HVAR =~ /D/) + (MUTATIONTASTER =~ /[AD]/) + (SIFT =~ /D/) ) >= 2)";
		HashMap<String, Object> values = new HashMap<>();
		values.put("SAMPLE_MAF", 0.1);
		values.put("POLYPHEN2_HVAR", "D,A");
		values.put("MUTATIONTASTER", "D");
		values.put("SIFT", "D");
		boolean res = filter.transformAndApply(filterString, values, true);
		assertTrue(res);
	}
	
	@Test
	public void testFullFilterFalseSample() throws ScriptException {
		String filterString = "SAMPLE_MAF < 0.2 && (( (POLYPHEN2_HVAR =~ /D/) + (MUTATIONTASTER =~ /[AD]/) + (SIFT =~ /D/) ) >= 2)";
		HashMap<String, Object> values = new HashMap<>();
		values.put("SAMPLE_MAF", 0.24);
		values.put("POLYPHEN2_HVAR", "D,A");
		values.put("MUTATIONTASTER", "D");
		values.put("SIFT", "D");
		boolean res = filter.transformAndApply(filterString, values, true);
		assertFalse(res);
	}
	
	@Test
	public void testFullFilterFalse1Of3() throws ScriptException {
		String filterString = "SAMPLE_MAF < 0.2 && (( (POLYPHEN2_HVAR =~ /D/) + (MUTATIONTASTER =~ /[AD]/) + (SIFT =~ /D/) ) >= 2)";
		HashMap<String, Object> values = new HashMap<>();
		values.put("SAMPLE_MAF", 0.1);
		values.put("POLYPHEN2_HVAR", "D,A");
		values.put("MUTATIONTASTER", "T,T");
		values.put("SIFT", "T");
		boolean res = filter.transformAndApply(filterString, values, true);
		assertFalse(res);
	}
	
	@Test
	public void testFullFilterFalse1Of3MutationTaster() throws ScriptException {
		String filterString = "SAMPLE_MAF < 0.2 && (( (POLYPHEN2_HVAR =~ /D/) + (MUTATIONTASTER =~ /[AD]/) + (SIFT =~ /D/) ) >= 2)";
		HashMap<String, Object> values = new HashMap<>();
		values.put("SAMPLE_MAF", 0.1);
		values.put("POLYPHEN2_HVAR", "D,A");
		values.put("MUTATIONTASTER", "T,T,.");
		values.put("SIFT", "T");
		boolean res = filter.transformAndApply(filterString, values, true);
		assertFalse(res);
	}
	
	@Test
	public void testFullFilterTrue2Of3MutationTaster() throws ScriptException {
		String filterString = "SAMPLE_MAF < 0.2 && (( (POLYPHEN2_HVAR =~ /D/) + (MUTATIONTASTER =~ /[AD]/) + (SIFT =~ /D/) ) >= 2)";
		HashMap<String, Object> values = new HashMap<>();
		values.put("SAMPLE_MAF", 0.1);
		values.put("POLYPHEN2_HVAR", "D,A");
		values.put("MUTATIONTASTER", "A,.,.");
		values.put("SIFT", "T");
		boolean res = filter.transformAndApply(filterString, values, true);
		assertTrue(res);
	}

}
