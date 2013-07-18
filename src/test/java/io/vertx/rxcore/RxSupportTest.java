package io.vertx.rxcore;

import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link io.vertx.rxcore.RxSupport}.
 */
public class RxSupportTest {

	/*
	 * Happy Path. b1 and b2 are non-empty.
	 */
	@Test
	public void testMergeBuffersB1AndB2NonEmpty() {		
		Buffer b1 = new Buffer();
		b1.appendString("b1");
		
		Buffer b2 = new Buffer();
		b2.appendString("b2");
		
		Buffer acc = RxSupport.mergeBuffers.call(b1, b2);
		Buffer expectedAcc = new Buffer("b1b2");
		
		assertEquals(expectedAcc, acc);
	}
	
	/*
	 * Happy Path. b1 is empty. Verify that the result is the same as b2.
	 */
	@Test
	public void testMergeBuffersB1IsEmpty() {		
		Buffer b1 = new Buffer();
		
		Buffer b2 = new Buffer();
		b2.appendString("b2");
		
		Buffer acc = RxSupport.mergeBuffers.call(b1, b2);
		Buffer expectedAcc = new Buffer("b2");
		
		assertEquals(expectedAcc, acc);
	}
	
	/*
	 * Happy Path. b2 is empty.  Verify that the result is the same as b1.
	 */
	@Test
	public void testMergeBuffersB2IsEmpty() {		
		Buffer b1 = new Buffer();
		b1.appendString("b1");
		
		Buffer b2 = new Buffer();
		
		Buffer acc = RxSupport.mergeBuffers.call(b1, b2);
		Buffer expectedAcc = new Buffer("b1");
		
		assertEquals(expectedAcc, acc);
	}
	
	/*
	 * Happy Path. b1 and b2 are empty. Verify that the result is empty too.
	 */
	@Test
	public void testMergeBuffersBothB2AndB1Empty() {		
		Buffer b1 = new Buffer();		
		Buffer b2 = new Buffer();		
		
		Buffer acc = RxSupport.mergeBuffers.call(b1, b2);
		Buffer expectedAcc = new Buffer();
		
		assertEquals(expectedAcc, acc);
	}
}