package com.cherry;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jamesnetherton.zulip.client.*;
import com.github.jamesnetherton.zulip.client.api.server.response.*;
import com.github.jamesnetherton.zulip.client.http.ZulipHttpClient;
import com.github.jamesnetherton.zulip.client.http.ZulipConfiguration;
import com.github.jamesnetherton.zulip.client.http.commons.ZulipCommonsHttpClient;
import com.github.jamesnetherton.zulip.client.api.server.request.GetApiKeyApiRequest;
import com.github.jamesnetherton.zulip.client.api.server.response.GetApiKeyApiResponse;
import com.github.jamesnetherton.zulip.client.api.message.request.GetMessagesApiRequest;
import com.github.jamesnetherton.zulip.client.api.message.response.GetMessagesApiResponse;
import com.github.jamesnetherton.zulip.client.api.stream.*;
/*
import com.github.jamesnetherton.zulip.client.api.message.Anchor;
import com.github.jamesnetherton.zulip.client.api.message.Emoji;
import com.github.jamesnetherton.zulip.client.api.message.Message;
import com.github.jamesnetherton.zulip.client.api.message.MessageFlag;
import com.github.jamesnetherton.zulip.client.api.message.MessageHistory;
import com.github.jamesnetherton.zulip.client.api.message.MessageReaction;
import com.github.jamesnetherton.zulip.client.api.message.MessageType;
import com.github.jamesnetherton.zulip.client.api.message.PropagateMode;
*/
import com.github.jamesnetherton.zulip.client.api.narrow.Narrow;
import com.github.jamesnetherton.zulip.client.api.message.*;
import com.github.jamesnetherton.zulip.client.api.core.ZulipApiResponse;
import com.github.jamesnetherton.zulip.client.api.core.ZulipService;
import com.github.jamesnetherton.zulip.client.api.user.UserRole;
import com.github.jamesnetherton.zulip.client.api.user.User;
import com.github.jamesnetherton.zulip.client.api.user.UserService;
import com.github.jamesnetherton.zulip.client.api.user.request.GetUserApiRequest;
import com.github.jamesnetherton.zulip.client.api.user.request.CreateUserApiRequest;
import com.github.jamesnetherton.zulip.client.api.user.response.GetUserApiResponse;
import com.github.jamesnetherton.zulip.client.api.user.response.CreateUserApiResponse;
import com.github.jamesnetherton.zulip.client.api.user.response.UserApiResponse;
// import com.github.jamesnetherton.zulip.client.api.user.request.UserRequestConstants;
import com.github.jamesnetherton.zulip.client.api.server.ServerSettings;

import com.github.jamesnetherton.zulip.client.api.stream.Stream;
import com.github.jamesnetherton.zulip.client.api.stream.StreamSubscriptionResult;
import com.github.jamesnetherton.zulip.client.api.stream.StreamUnsubscribeResult;
import com.github.jamesnetherton.zulip.client.api.stream.StreamService;
import com.github.jamesnetherton.zulip.client.api.stream.StreamSubscriptionRequest;
import com.github.jamesnetherton.zulip.client.api.stream.request.SubscribeStreamsApiRequest;
import com.github.jamesnetherton.zulip.client.api.stream.request.UnsubscribeStreamsApiRequest;
import com.github.jamesnetherton.zulip.client.api.stream.request.UpdateStreamApiRequest;
import com.github.jamesnetherton.zulip.client.api.stream.response.SubscribeStreamsApiResponse;
import com.github.jamesnetherton.zulip.client.api.stream.response.UnsubscribeStreamsApiResponse;


import com.github.jamesnetherton.zulip.client.util.ZulipUrlUtils;
import com.github.jamesnetherton.zulip.client.exception.ZulipClientException;


import com.google.gson.*;
import com.google.gson.reflect.*;

public class ZulipAdminUtils {
    
  private ZulipHttpClient adminClient;
  private ZulipConfiguration adminConfig;
  public ZulipAdminUtils(ZulipConfiguration adminConfig) throws ZulipClientException {
      this.adminConfig = adminConfig;
      this.adminClient = new ZulipCommonsHttpClient(adminConfig);
      User user = new Zulip(adminConfig)
          .users()
          .getOwnUser().execute();
      if (!user.isAdmin() && !user.isOwner()) {
          throw new ZulipClientException("Not Administrator or Owner!");
      }
  }
  
  public ZulipConfiguration createConfig(String userEmail, String userPassword) 
      throws ZulipClientException
  {
      ZulipConfiguration config = new ZulipConfiguration(
                                      this.adminConfig.getZulipUrl(),
                                      userEmail,
                                      this.getUserApiKey(userEmail,userPassword) );
      config.setInsecure(this.adminConfig.isInsecure());
      return config;
  }
  
  // 중요 : 사용자의 API Key 가져오기
  public String getUserApiKey(String userEmail, String userPassword) throws ZulipClientException {
      final String FETCH_API_KEY = "fetch_api_key";    
      Map<String, Object> params = new HashMap();
      params.put("username",userEmail);
      params.put("password",userPassword);
      GetApiKeyApiResponse response = this.client().post(FETCH_API_KEY, params, GetApiKeyApiResponse.class);
      return response.getApiKey();    
  }
  
  //중요: 사용자 생성
  public void createUser(String userEmail, String userFullName, String userPassword)  
      throws ZulipClientException 
  {
      new CreateUserApiRequest(this.client(),userEmail,userFullName,userPassword)
          .execute();
      this.turnOffAllEmailNotification(userEmail,userPassword);
  }
  
    //이슈 : getUser(long) 타입이므로 String으로 안된다는 이슈 발생
    //중요: 이메일로 사용자정보 가져오기
  public User getUserByEmail(String userEmail) throws ZulipClientException {
      /*
      Map<String, Object> params = new HashMap<>();
      GetUserApiResponse response = this.client().get(UserRequestConstants.USERS+"/"+userEmail, params, GetUserApiResponse.class);
      return new User(response.getUserApiResponse());
      */
      return  new UserService(this.client()).getUser(userEmail).execute();
  }
  
  // 중요 :사용자의 이메일 알림 기능을 Off
  public void turnOffAllEmailNotification(String userEmail, String userPassword) 
      throws ZulipClientException
  {
      //zulip 사용자 client 생성
      Zulip zulipUser = this.createZulip(userEmail,userPassword);
      zulipUser.users().updateNotificationSettings()
          .withEnableStreamEmailNotifications(false)
          .withEnableOfflineEmailNotifications(false)
          .withEnableDigestEmails(false)
          .withEnableLoginEmails(false)
          .execute();
      zulipUser.close();
  }
  
  // 단체방(stream)생성 
  public <T> StreamSubscriptionResult createStream(String streamName, String streamDesc,
          boolean isPrivate, List<T> userEmailOrID) 
          throws ZulipClientException
  {
      StreamPostPolicy postPolicy = StreamPostPolicy.ANY;
      if (isPrivate) {
          postPolicy = StreamPostPolicy.NEW_MEMBERS_ONLY;;
      }
      
      StreamService streamService = new StreamService(this.client());
      SubscribeStreamsApiRequest streamRequest= 
          streamService.subscribe(
              StreamSubscriptionRequest.of(streamName, streamDesc))
              .withAnnounce(false)
              .withAuthorizationErrorsFatal(true)
              .withStreamPostPolicy(postPolicy)
              .withInviteOnly(isPrivate)
              .withMessageRetention(RetentionPolicy.REALM_DEFAULT)
              .withHistoryPublicToSubscribers(true);

      streamRequest.execute();
      
      Long streamId = streamService.getStreamId(streamName).execute();
      
      if(userEmailOrID != null && !userEmailOrID.isEmpty()) {
          if (userEmailOrID.get(0) instanceof String) {
              // admin email 추가 : subscription setting을 위해                                
              streamRequest.withPrincipals(
                  userEmailOrID.toArray(new String[0]));
          }
          else if(userEmailOrID.get(0) instanceof Long) {
              // Convert List<Long> to long[]
              streamRequest.withPrincipals(
                  ((List<Long>)userEmailOrID).stream().mapToLong(l -> l).toArray());
          }
      }
      
      StreamSubscriptionResult streamResult = streamRequest.execute();
      
      // Email notification off
      streamService.updateSubscriptionSettings()
          .withEmailNotifications(streamId.intValue(),false)
          .execute();
      
      streamService.unsubscribe(streamName).execute();
      
      return streamResult;
  }    

  
  // Stream 정보 가져오기
  // streamNameOrID : Stream name or ID - ID는 Long타입
  
  public <T> Stream getStream(T streamNameOrID) throws 
      ZulipClientException 
  {
      StreamService streamService = new StreamService(this.client());
      for(Stream stream: streamService.getAll()
                  .withIncludeAllActive(true)
                  .withIncludeDefault(false)
                  .execute()) 
      {
          if (streamNameOrID instanceof String) {            
              if (stream.getName().compareToIgnoreCase(streamNameOrID.toString())==0) {
                  return stream; 
              }
          }
          else if(streamNameOrID instanceof Long) {
              if(stream.getStreamId() ==  ((Long)streamNameOrID).longValue()) {
                  return stream;
              }
          }        
      }
      return null;
  }    
  
  
  //--------------------------------------------------
  //stream을 privacy을 변경
  // userEmail : stream에 있는 사용자
  // userPassword
  // streamMap : userEmail 사용자가 속해 있는 stream 및 공개/비공개여부 (공개:true)
  /**
      String userEmail = "test1@cherry.io";
      String userPassword = USER_PW;

      //Stream name, 공개여부
      HashMap<String,Boolean> streamMap = new HashMap<>();

      streamMap.put("공개테스트방02",false);
      streamMap.put("공개테스트방01",false);
      
      changeStreamPrivacy(userEmail, userPassword, streamMap)
  */
  //--------------------------------------------------
  
  public void changeStreamPrivacy(String userEmail, String userPassword, 
      HashMap<String,Boolean> streamMap)  throws ZulipClientException
  {
      StreamService streamService = new StreamService(this.client());
      
      // 1) stream에 있는 사용자 zulip만들기
      
      Zulip zulipUser = new Zulip(this.createConfig(userEmail,userPassword));    

      for(String streamName: streamMap.keySet()) {            

          // 2) stream에 admin 추가
          
          SubscribeStreamsApiRequest subscribeStreamsApiRequest =
              zulipUser.streams().subscribe(StreamSubscriptionRequest.of(streamName,streamName));
          subscribeStreamsApiRequest
              .withPrincipals(this.config().getEmail())
              .execute();        

          // 4) admin이 stream의 공개/비공개 여부 설정
          
          Long streamId = zulipUser.streams().getStreamId(streamName).execute();
          streamService.updateStream(streamId)
              .withIsPrivate(!streamMap.get(streamName))
             .execute();
             
          // 5) admin이 stream에서 나감
             
          streamService.unsubscribe(streamName).execute();
      }
      zulipUser.close();
  }
  
  // Stream 삭제
  public void deleteStreamByID(Long streamID) throws 
      ZulipClientException 
  {
      StreamService streamService = new StreamService(this.client());
          streamService.delete(streamID).execute();
          return;
  }
  
  // Stream 삭제
  // 전체 stream에서 찾아서 삭제
  // streamNameOrID : stream name or stream id
  public <T> void deleteStream(T streamNameOrID) throws 
      ZulipClientException 
  {
      StreamService streamService = new StreamService(this.client());
      for(Stream stream: streamService.getAll()
                  .withIncludeAllActive(true)
                  .withIncludeDefault(false)
                  .execute()) 
      {
          if (streamNameOrID instanceof String) {
              if (stream.getName().compareToIgnoreCase(streamNameOrID.toString())==0) {
                  streamService.delete(stream.getStreamId()).execute();                
                  return; 
              }
          }
          else if(streamNameOrID instanceof Long) {
              if(stream.getStreamId() ==  ((Long)streamNameOrID).longValue()) {
                  streamService.delete(stream.getStreamId()).execute();
                  return; 
              }
          }        
      }
  }    
  //Stream에 User추가
  //Stream에 있는 사용자가 다른 사용자를 stream에 추가 할 수 있다.
  public <T> StreamSubscriptionResult addUserToStream( String streamName, List<T> userEmailOrID) 
      throws ZulipClientException
  {
      Zulip zulipUser = new Zulip(this.adminConfig);

      SubscribeStreamsApiRequest subscribeStreamsApiRequest = zulipUser.streams().subscribe(
              StreamSubscriptionRequest.of(streamName, streamName));

      if(userEmailOrID != null && !userEmailOrID.isEmpty()) {
          if (userEmailOrID.get(0) instanceof String) {
              // admin email 추가 : subscription setting을 위해                                
              subscribeStreamsApiRequest
                  .withPrincipals(userEmailOrID.toArray(new String[0]));
          }
          else if(userEmailOrID.get(0) instanceof Long) {
              // Convert List<Long> to long[]
              subscribeStreamsApiRequest
                  .withPrincipals(((List<Long>)userEmailOrID).stream().mapToLong(l -> l).toArray());
          }
      }
      StreamSubscriptionResult streamSubscriptionResult = subscribeStreamsApiRequest.execute();
      zulipUser.close();
      return streamSubscriptionResult;
  }  
  //Stream에 User추가
  //Stream에 있는 사용자가 다른 사용자를 stream에 추가 할 수 있다.
  public <T> StreamSubscriptionResult addUserToStream(String userEmail, String userPassword, 
          String streamName, List<T> userEmailOrID) throws ZulipClientException
  {
      Zulip zulipUser = new Zulip(this.createConfig(userEmail,userPassword));

      SubscribeStreamsApiRequest subscribeStreamsApiRequest = zulipUser.streams().subscribe(
              StreamSubscriptionRequest.of(streamName, streamName));

      if(userEmailOrID != null && !userEmailOrID.isEmpty()) {
          if (userEmailOrID.get(0) instanceof String) {
              // admin email 추가 : subscription setting을 위해                                
              subscribeStreamsApiRequest
                  .withPrincipals(userEmailOrID.toArray(new String[0]));
          }
          else if(userEmailOrID.get(0) instanceof Long) {
              // Convert List<Long> to long[]
              subscribeStreamsApiRequest
                  .withPrincipals(((List<Long>)userEmailOrID).stream().mapToLong(l -> l).toArray());
          }
      }
      StreamSubscriptionResult streamSubscriptionResult = subscribeStreamsApiRequest.execute();
      zulipUser.close();
      return streamSubscriptionResult;
  }  
  
  //Stream에서 User 추가
  public <T> StreamSubscriptionResult addUserToStream2(String streamName, List<T> userEmailOrID) 
      throws ZulipClientException
  {
      StreamService streamService = new StreamService(this.client());
      SubscribeStreamsApiRequest subscribeStreamsApiRequest = 
              new StreamService(this.client()).subscribe(
                  StreamSubscriptionRequest.of(streamName, streamName));
      if(userEmailOrID != null && !userEmailOrID.isEmpty()) {
          if (userEmailOrID.get(0) instanceof String) {
              // admin email 추가 : subscription setting을 위해                                
              subscribeStreamsApiRequest
                  .withPrincipals(userEmailOrID.toArray(new String[0]));
          }
          else if(userEmailOrID.get(0) instanceof Long) {
              // Convert List<Long> to long[]
              subscribeStreamsApiRequest
                  .withPrincipals(((List<Long>)userEmailOrID).stream().mapToLong(l -> l).toArray());
          }
      }
      return subscribeStreamsApiRequest.execute();
  }      
 
  //Stream에서 User 삭제
  public <T> StreamUnsubscribeResult removeUserFromStream(String streamName, List<T> userEmailOrID) 
      throws ZulipClientException
  {

      StreamService streamService = new StreamService(this.client());
      UnsubscribeStreamsApiRequest unSubscribeStreamsApiRequest = 
              new StreamService(this.client()).unsubscribe(streamName);

      if(userEmailOrID != null && !userEmailOrID.isEmpty()) {
          if (userEmailOrID.get(0) instanceof String) {
              // admin email 추가 : subscription setting을 위해                                
              unSubscribeStreamsApiRequest
                  .withPrincipals(userEmailOrID.toArray(new String[0]));
          }
          else if(userEmailOrID.get(0) instanceof Long) {
              // Convert List<Long> to long[]
              unSubscribeStreamsApiRequest
                  .withPrincipals(((List<Long>)userEmailOrID).stream().mapToLong(l -> l).toArray());
          }
      }
      return unSubscribeStreamsApiRequest.execute();
  }     
  
  //중요 : Client 생성
  public ZulipHttpClient createClient(String userEmail,String userPassword)
      throws ZulipClientException
  {
      return new ZulipCommonsHttpClient(this.createConfig(userEmail,userPassword));
  }
  //중요 :  Zulip 생성
  public Zulip createZulip(String userEmail,String userPassword)
      throws ZulipClientException
  {
      return new Zulip(this.createConfig(userEmail,userPassword));
  }
  
  // Admin Client return
  public ZulipHttpClient client() {
      return this.adminClient;
  }
  // Admin Config return
  public ZulipConfiguration config() {
      return this.adminConfig;
  }   
  
  
  // Json string으로 출력
  public <T> void jsonPrint(T obj) {
      System.out.println(new GsonBuilder().setPrettyPrinting().create()
          .toJson(obj).toString());
  }
}