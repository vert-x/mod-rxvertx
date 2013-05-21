package io.vertx.rxcore.java.http;

import java.io.UnsupportedEncodingException;

import io.vertx.rxcore.RxSupport;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.*;
import org.vertx.java.core.json.JsonObject;
import rx.Observable;
import rx.util.functions.Action1;
import rx.util.functions.Func1;

/** Utility methods for RxHttpXXX 
 * @author <a href="http://github.com/petermd">Peter McDonnell</a>
 **/
public class RxHttpSupport {

  // Server
  
  public static Func1<HttpServerRequest,Observable<Buffer>> decodeBody=new Func1<HttpServerRequest,Observable<Buffer>>() {
    public Observable<Buffer> call(HttpServerRequest httpReq) {
      // Must use Rx methods to access stream as Observable
      assert(httpReq instanceof RxHttpServerRequest);
      
      return ((RxHttpServerRequest)httpReq).asObservable().reduce(RxSupport.mergeBuffers);
    }
  };
  
  // Client
  
  // Uploaders

  /** Create uploader for JsonObject */
  public static Action1<HttpClientRequest> uploadJson(JsonObject src) throws UnsupportedEncodingException {
    return uploadJson(src,"utf8");
  }

  /** Create uploader for JsonObject */
  public static Action1<HttpClientRequest> uploadJson(JsonObject src, String charset) throws UnsupportedEncodingException {
    String contentType="text/json;charset="+charset;
    return uploadBody(contentType,src.encode().getBytes(charset));    
  }
  
  /** Create uploader for byte array */
  public static Action1<HttpClientRequest> uploadBody(final String contentType, final byte[] src) {
    return new Action1<HttpClientRequest>() {
      public void call(HttpClientRequest httpReq) {
        httpReq.putHeader("Content-type",contentType);
        httpReq.putHeader("Content-length",Integer.toString(src.length));
        httpReq.write(new Buffer(src));
        httpReq.end();
      }
    };
  }
  
  // Downloads
  
  /** Convert the response to an Observable<Buffer> stream */
  public static Func1<HttpClientResponse, Observable<Buffer>> downloadStream() {
    return new Func1<HttpClientResponse, Observable<Buffer>>() {
      public Observable<Buffer> call(HttpClientResponse httpResp) {
        // Must use Rx methods to access stream as Observable
        assert(httpResp instanceof RxHttpClientResponse);
        
        checkResponse(httpResp);
        
        return ((RxHttpClientResponse)httpResp).asObservable();
      }
    };
  }
  
  /** Convert the response to an Observable<Buffer> with single body */
  public static Func1<RxHttpClientResponse, Observable<Buffer>> downloadBody() {
    return new Func1<RxHttpClientResponse, Observable<Buffer>>() {
      public Observable<Buffer> call(RxHttpClientResponse httpResp) {

        checkResponse(httpResp);
        
        return httpResp.asObservable().reduce(RxSupport.mergeBuffers);
      }
    };
  }
  
  /** Convert the response to an Observable<JsonObject> with single body */
  public static Func1<RxHttpClientResponse, Observable<JsonObject>> downloadJson() {
    return new Func1<RxHttpClientResponse, Observable<JsonObject>>() {
      public Observable<JsonObject> call(RxHttpClientResponse httpResp) {

        checkResponse(httpResp);
        
        // TODO: Extract charset from Content-type
        return httpResp.asObservable().reduce(RxSupport.mergeBuffers).map(RxSupport.decodeJson("utf8"));
      }
    };
  }

  // Utility
  
  /** Validate response */
  public static void checkResponse(HttpClientResponse httpResp) throws RuntimeException {
    // Must use Rx methods to access stream as Observable
    assert(httpResp instanceof RxHttpClientResponse);

    if (httpResp.statusCode()>=400)
      throw new RuntimeException("HTTP request failed (code="+httpResp.statusCode()+",msg="+httpResp.statusMessage()+")");
    else if (httpResp.statusCode()>=300)
      throw new RuntimeException("HTTP redirect not supported (code="+httpResp.statusCode()+",msg="+httpResp.statusMessage()+",location="+httpResp.headers().get("Location")+")");
  }
}
