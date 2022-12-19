package com.cherry;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import com.github.jamesnetherton.zulip.client.Zulip;
import com.github.jamesnetherton.zulip.client.api.message.response.GetMessagesApiResponse;
import com.github.jamesnetherton.zulip.client.api.narrow.Narrow;
import com.github.jamesnetherton.zulip.client.api.common.Operation;
import com.github.jamesnetherton.zulip.client.api.message.Anchor;
import com.github.jamesnetherton.zulip.client.api.message.Message;
import com.github.jamesnetherton.zulip.client.api.message.MessageFlag;
import com.github.jamesnetherton.zulip.client.api.message.request.GetMessagesApiRequest;
import com.github.jamesnetherton.zulip.client.exception.ZulipClientException;
import com.github.jamesnetherton.zulip.client.http.ZulipHttpClient;
import com.google.gson.Gson;


@Slf4j
public class TestMessage extends TestCase {
    final static String MESSAGES_API_PATH = "messages";
    final static String PUB_STREAM_NAME = "단체1-공개";
    private Admin ta = new Admin();
    
    //메시지 보내기
    @Test
    public void testSendStreamMessage() throws MalformedURLException, ZulipClientException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        //ZulipAdminUtils adminUtils = setAdmin();
        Zulip z_user = adminUtils.createZulip("test001@cherrycorp.io", ta.USER_PW);
        Long messageId = z_user.messages()
                        .sendStreamMessage("메시지테스트44", "단체1-공개", "테스트토픽")
                        .execute();
        log.info("messageId : {}", messageId);
        z_user.close(); // 닫기
    }

    //메시지 가져오기 (1)
    @Test
    public void testGetMsg1() throws MalformedURLException, ZulipClientException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        ZulipHttpClient userClient = adminUtils.createClient(ta.ADMIN_EMAIL, ta.USER_PW);

        List<Map<String, Object>> narrowList = new ArrayList<>();
        // narrowList.add(
        //     new HashMap<String, Object>() {{
        //         put("operator", "stream");
        //         put("operand", pubStreamName);
        //     }}
        // );
        Map<String, Object> h = new HashMap<String, Object>();
        h.put("operator", "stream");
        h.put("operand", PUB_STREAM_NAME);
        narrowList.add(h);

        String narrow = new Gson().toJson(narrowList);
        //adminUtils.jsonPrint(user);
        log.info(narrowList.toString());

        Map<String, Object> params = new HashMap<>();
        params.put("anchor", "newest");
        params.put("apply_markdown", false); //마크다운 적용해서 가져올 건지, 결과를 가져옴 
        params.put("num_before", 100);
        params.put("num_after", 0);
        params.put("narrow", narrow);

        GetMessagesApiResponse res = userClient.get(MESSAGES_API_PATH, params, GetMessagesApiResponse.class);
        List<Message> msg = res.getMessages();
        for (Message m : msg) {
            adminUtils.jsonPrint(m);
            System.out.println("------------------");
        }

    }

    //메시지 가져오기 (2)
    @Test
    public void testGetMsg2() throws MalformedURLException, ZulipClientException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        ZulipHttpClient userClient = adminUtils.createClient(ta.ADMIN_EMAIL, ta.USER_PW);
        List<Message> msgList = (List<Message>) new GetMessagesApiRequest(userClient)
                                .withAnchor(Anchor.NEWEST)
                                .withMarkdown(false) //true 이면 content부분에 utf-8이 미적용됨
                                .withNarrows(Narrow.of("stream", PUB_STREAM_NAME))
                                .withNumAfter(0) //?? 큰 의미가 없는 것으로 보여서 0으로 고정하는 게 나을듯
                                .withNumBefore(1) // 1 == 가장 최신에서 + 1, 0지정하면 안됨
                                .execute();
        for (Message m : msgList) {
            adminUtils.jsonPrint(m);
            System.out.println("------------------");    
        }   
    }

    //메시지 가져오기 (3)
    @Test
    public void testGetMsg3() throws MalformedURLException, ZulipClientException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        ZulipHttpClient userClient = adminUtils.createClient(ta.ADMIN_EMAIL, ta.USER_PW);
        Zulip z_admin = adminUtils.createZulip(ta.ADMIN_EMAIL, ta.USER_PW);

        List<Message> messageList = (List<Message>) z_admin.messages()
                                    .getMessages(100, 0, Anchor.NEWEST)
                                    .withMarkdown(false)
                                    .withGravatar(true)
                                    .withNarrows(Narrow.of("stream", PUB_STREAM_NAME))
                                    .execute();
        for(Message m : messageList){
            adminUtils.jsonPrint(m);
            System.out.println("------------------");
        }

    }

    // 개인 메시지 보내기 dm
    @Test
    public void testSendPrivMsg() throws MalformedURLException, ZulipClientException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        Zulip z_member = adminUtils.createZulip("test001@cherrycorp.io", ta.USER_PW);
        Long messageId = z_member.messages().sendPrivateMessage("안녕하세요. 반갑습니다.", "test004@cherrycorp.io").execute();

        //System.out.println(messageId);
    }

    // 이모지 생성/삭제
    @Test
    public void testEmoji() throws MalformedURLException, ZulipClientException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        Zulip z_admin = adminUtils.createZulip(ta.ADMIN_EMAIL, ta.USER_PW);
        /* [이모지 추가]
         * addEmojiReaction(메시지 id, "이모지name")
         * (TODO) 이모지 NAME 어떻게 불러와야 할지
        */
        // z_admin.messages().addEmojiReaction(227, "innocent").execute();

        /* [이모지 삭제]
         * deleteEmojiReaction(메시지 id, "이모지name")
        */
        // z_admin.messages().deleteEmojiReaction(227, "innocent").execute();
        
        // (TODO) Flag가 어떤 건지 파악 필요
        z_admin.messages().updateMessageFlags(MessageFlag.COLLAPSED, Operation.ADD, 227).execute();

    }

    
    /* [파일 업로드]
     * sendStreamMessage("메시지 내용 + 파일명 + 파일 경로", "스트림명", "토픽명")
     * ex) [cherry.png](/user_uploads/8/85/G2j_8ovjXqniutnz_92oET2l/cherry.png) 
    */
    @Test
    public void testUploadFile() throws MalformedURLException, ZulipClientException {
        String f_path = "/home/shpark/zulip/kimsy/zulipj/src/test/java/com/cherry/file/cherry.png";
        ZulipAdminUtils adminUtils = ta.setAdmin();
        Zulip z_admin = adminUtils.createZulip(ta.ADMIN_EMAIL, ta.USER_PW);
        // 파일 업로드
        File f = new File(f_path);
        String fu = z_admin.messages().fileUpload(f).execute();

        // 파일명 추출
        String[] names = f_path.split("/");
        String name = names[names.length-1];
        z_admin.messages().sendStreamMessage("파일 테스트 성공33 ["+name+"]("+fu+")", "단체1-공개", "재밌는토픽01").execute();
        
    }
    
    /* [읽음으로 표시하기]
     * markTopicAsRead(스트림id, 토픽명)
     * markStreamAsRead(스트림id)
     * markAllAsRead() --> 실행시 오류는 없지만 읽음 표시가 안 먹힘
     * matchMessages() --> 메시지 검색 기능과 비슷한 것 같은데 deprecated 된 것 같음
     *  boolean b = z_admin.messages().matchMessages().equals("ddd");
        log.info("b=={}", b);
    */
    @Test
    public void testMatch() throws MalformedURLException, ZulipClientException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        Zulip z_admin = adminUtils.createZulip(ta.ADMIN_EMAIL, ta.USER_PW);
        z_admin.messages().markTopicAsRead(39, "테스트토픽").execute();
        // z_admin.messages().markStreamAsRead(39).execute();
        // z_admin.messages().markAllAsRead().execute();
    }


    /* [메시지 수정]
     * editMessage(msgId).withContent("수정텍스트")
     * return void
    */
    @Test
    public void testEditMsg() throws MalformedURLException, ZulipClientException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        Zulip z_admin = adminUtils.createZulip(ta.ADMIN_EMAIL, ta.USER_PW);
        // 메시지 수정
        // z_admin.messages().editMessage(249).withContent("수정텍스트").execute();

        // [ topic link test ] --> (TODO)
        // Message m = z_admin.messages().getMessage(251).execute();
        // List<String> topicLinks = m.getTopicLinks();
        // log.info("topicLink =={}", topicLinks.get(0));
    }

    /* [메시지 Flag]
     * getFlags() --> flag 추출 후 작업  
     * (1) 언급된 메시지 파악 : mentioned 키워드
     * (2) 중요 표시된 메시지 파악 : starred 키워드
     *      비중요 -> 중요로 바꾸는 api 미완성. List에 STARRED 키워드 추가했지만 본 메시지는 수정되지 않음.
    */
    @Test
    public void testFlagMsg() throws MalformedURLException, ZulipClientException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        Zulip z_admin = adminUtils.createZulip(ta.ADMIN_EMAIL, ta.USER_PW);
        Message m = z_admin.messages().getMessage(251).execute();
        List<MessageFlag> flags = m.getFlags();

        log.info("flags={}", flags);

        // for(MessageFlag flag : flags){
        //     log.debug("flag==", flag.toString());
        //     if(flag.toString().equals("mentioned")){
        //         log.info("멘션됨");
        //     }
        // }

        // List에 해당 문구 있는지 체크 
        flags.add(MessageFlag.STARRED);
        log.info("flags={}", flags);
        adminUtils.jsonPrint(m);
    }
}
