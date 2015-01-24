package io.vertx.rxcore;

import io.netty.buffer.*;
import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.testtools.TestVerticle;
import rx.Observable;

import java.util.Arrays;

import static io.vertx.rxcore.test.integration.java.RxAssert.assertSingleThenComplete;
import static io.vertx.rxcore.test.integration.java.RxAssert.assertErrorThenComplete;
import static org.junit.Assert.*;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * Unit tests for {@link io.vertx.rxcore.RxSupport}.
 */
public class RxSupportTest extends TestVerticle {

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

    testComplete();
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

    testComplete();
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

    testComplete();
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

    testComplete();
  }

  @Test
  public void testReduceMerge() {

    // Create an Observable that starts with a read-only buffer
    Buffer b1=new Buffer(new ReadOnlyByteBuf(new EmptyByteBuf(ByteBufAllocator.DEFAULT)));
    Buffer b2=new Buffer("b1");
    Buffer b3= new Buffer("b2");

    Observable<Buffer> merged=Observable.from(Arrays.asList(b1, b2, b3));

    // If you reduce() without a seed buffer it will fail because the first buffer cannot be appended to
    assertErrorThenComplete(merged.reduce(RxSupport.mergeBuffers), IndexOutOfBoundsException.class);

    // Using a writeable seed buffer will ensure you can reduce() successfully
    assertSingleThenComplete(merged.reduce(new Buffer(),RxSupport.mergeBuffers),new Buffer("b1b2"));
  }
}