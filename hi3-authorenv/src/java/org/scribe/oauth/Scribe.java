/* 
Copyright 2010 Pablo Fernandez

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.scribe.oauth;

import org.scribe.eq.*;
import org.scribe.http.*;
import org.scribe.http.Request.*;

/**
 * The main class. Provides an access point for all OAuth methods.
 * 
 * Please get an instance normally, using the public constructor.
 * The {@link Scribe#getInstance(Properties)} method is kept for compatibility reasons 
 * but will be removed eventually
 * 
 * @author Pablo Fernandez
 */
public class Scribe {
  
  private static Scribe instance;
  
  private final java.util.Properties config;
  private DefaultEqualizer eq;
  
  /**
   * Factory method
   * 
   * @deprecated Use the default constructor instead. If you need to enforce a single instance do so with your own code.
   * @param Scribe properties
   * @return The Scribe single instance
   */
  @Deprecated
  public static synchronized Scribe getInstance(java.util.Properties props){
    if(instance == null)
        instance = new Scribe(props);
    return instance;
  }
  
  /**
   * Default constructor
   * 
   * @param Scribe properties
   */
  public Scribe(java.util.Properties props){
    this.config = props;
    initEqualizer();
  }
  
  private void initEqualizer(){
    String eqName = config.getProperty(Scribe.Properties.EQUALIZER);
    if(isEmpty(eqName))
      this.eq = new DefaultEqualizer();
    else
      try{
        this.eq = (DefaultEqualizer) Class.forName(eqName).newInstance();
      }catch(Exception e){
        throw new RuntimeException("Could not initalize Scribe equalizer",e);
      }
  }
  
  private boolean isEmpty(String string){
    return (string == null || string.trim().length() <= 0);
  }
  
  /**
   * Obtains the request token and token secret.
   * 
   * @return the request token
   */
  public Token getRequestToken(){
    Request request = getRTRequest();
    OAuthSigner signer = getOAuthSigner();
    signer.signForRequestToken(request, config.getProperty(Scribe.Properties.CALLBACK_URL));
    eq.tuneRequest(request, CallType.REQUEST_TOKEN);
    Response response = request.send();
    return eq.parseRequestTokens(response.getBody());
  }
  
  private Request getRTRequest() {
    return new Request(Verb.valueOf(config.getProperty(Scribe.Properties.REQUEST_TOKEN_VERB)), config.getProperty(Scribe.Properties.REQUEST_TOKEN_URL));
  }

  private OAuthSigner getOAuthSigner() {
    return new OAuthSigner(config.getProperty(Scribe.Properties.CONSUMER_KEY), config.getProperty(Scribe.Properties.CONSUMER_SECRET),eq);
  }

  /**
   * Obtains the access token and access token secret.
   * 
   * @param request token
   * @param verifier
   * @return access token
   */
  public Token getAccessToken(Token token, String verifier){
    Request request = getATRequest();
    OAuthSigner signer = getOAuthSigner();
    signer.signForAccessToken(request, token, verifier);
    eq.tuneRequest(request, CallType.ACCESS_TOKEN);
    Response response = request.send();

    int ii = 4;

    return eq.parseAccessTokens(response.getBody());
  }

  private Request getATRequest() {
    return new Request(Verb.valueOf(config.getProperty(Scribe.Properties.ACCESS_TOKEN_VERB)), config.getProperty(Scribe.Properties.ACCESS_TOKEN_URL));
  }
  
  /**
   * Adds an OAuth header to the {@link Request} 
   * 
   * @param Request to sign
   * @param access token
   */
  public void signRequest(Request request, Token token){
    OAuthSigner signer = getOAuthSigner();
    signer.sign(request, token);
    eq.tuneRequest(request, CallType.RESOURCE);
  }
  
  /**
   * Returns useful debug information about the {@link Request}, JSON style.
   * 
   * @param Request to inspect
   * @param access token
   * @return request information (JSON)
   */
  public String getJsonInfo(Request request, Token token){
	  return RequestInspector.getJsonInfo(getOAuthSigner(),request, token);
  }
  
  /**
   * Inner class that holds the property keys for the configuration file.
   * Useful if you create the {@link java.util.Properties} programmatically
   * 
   * @author Pablo Fernandez
   */
  public static class Properties {
    
    public static final String EQUALIZER = "scribe.equalizer";
    public static final String REQUEST_TOKEN_URL = "request.token.url";
    public static final String CALLBACK_URL = "callback.url";
    public static final String REQUEST_TOKEN_VERB = "request.token.verb";
    public static final String ACCESS_TOKEN_URL = "access.token.url";
    public static final String ACCESS_TOKEN_VERB = "access.token.verb";
    public static final String CONSUMER_SECRET = "consumer.secret";
    public static final String CONSUMER_KEY = "consumer.key"; 
  } 
}