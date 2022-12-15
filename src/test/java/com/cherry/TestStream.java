package com.cherry;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import com.github.jamesnetherton.zulip.client.Zulip;
import com.github.jamesnetherton.zulip.client.api.stream.Stream;
import com.github.jamesnetherton.zulip.client.api.stream.StreamSubscriptionResult;
import com.github.jamesnetherton.zulip.client.api.user.User;
import com.github.jamesnetherton.zulip.client.exception.ZulipClientException;


@Slf4j
public class TestStream extends TestCase {

    private Admin ta = new Admin();

    // 스트림 목록 가져오기
    @Test
    public void testGetStream() throws ZulipClientException, MalformedURLException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        Zulip zulipUser = adminUtils.createZulip(adminUtils.config().getEmail(), ta.USER_PW);

        List<Stream> streamList = zulipUser.streams()
                                .getAll().execute();
        //adminUtils.jsonPrint(streamList); // 그냥 출력

        streamList.size(); // 스트림 개수

        for (Stream s : streamList) { // 구분자 사용 출력
            adminUtils.jsonPrint(s);
            System.out.println("------------------");    
        }
    }

    // 공개 스트림 생성
    @Test
    public void testCreatePubStream() throws ZulipClientException, MalformedURLException {
        String newUserEmail = "test002@cherrycorp.io";
        ZulipAdminUtils adminUtils = ta.setAdmin();
        String pubStreamName = "군고구마방-공개";
        String pubStreamNameDesc = "겨울에 군고구마 먹는 사람 모입입니다.";

        StreamSubscriptionResult stream = adminUtils.createStream(pubStreamName,
                                        pubStreamNameDesc,
                                        false,
                                        Arrays.asList(newUserEmail));
    }

    // 비공개 스트림 생성
    @Test
    public void testCreatePrivStream() throws ZulipClientException, MalformedURLException {
        String newUserEmail = "test002@cherrycorp.io";
        ZulipAdminUtils adminUtils = ta.setAdmin();
        String pubStreamName = "붕어빵방-비공개";
        String pubStreamNameDesc = "겨울에 붕어빵 먹는 사람 모입입니다.";

        StreamSubscriptionResult stream = adminUtils.createStream(pubStreamName,
                                        pubStreamNameDesc,
                                        true,
                                        Arrays.asList(newUserEmail));
    }

    // 공개 스트림에 사용자 추가
    @Test
    public void testAddUserToPubStream() throws ZulipClientException, MalformedURLException {
        List<String> userEmailList = new ArrayList<>();
        ZulipAdminUtils adminUtils = ta.setAdmin();
        Zulip zulipUser = adminUtils.createZulip(adminUtils.config().getEmail(), ta.USER_PW);
        User user = zulipUser.users().getOwnUser().execute();

        userEmailList.add(user.getEmail());
        userEmailList.add(adminUtils.config().getEmail());

        StreamSubscriptionResult stream = adminUtils.addUserToStream("군고구마방-공개", userEmailList);
    }

    // 공개 스트림에 메시지 보내기
    // 새로운 사용자 계정으로 메시지를 보낼 수 있게 관리자는 새로운 사용자의 zulip object 를 생성한다.
    @Test
    public void testSendMsgToStream() throws ZulipClientException, MalformedURLException {
        String newUserEmail = "test002@cherrycorp.io";
        ZulipAdminUtils adminUtils = ta.setAdmin();
        Zulip zulip = adminUtils.createZulip(newUserEmail, ta.USER_PW);

        zulip.messages().sendStreamMessage(
            "군고구마 맛있어요2", "군고구마방-공개", "테스트"
        ).execute();
    }

    // 비공개 스트림에 사용자 추가
    @Test
    public void testAddUserToPrivStream() throws ZulipClientException, MalformedURLException {
        String newUserEmail = "test002@cherrycorp.io"; //비공개방에 존재하는 회원이 직접 추가하는 방식
        List<String> addUserEmail = new ArrayList<>();
        addUserEmail.add("test001@cherrycorp.io");

        ZulipAdminUtils adminUtils = ta.setAdmin();
        StreamSubscriptionResult stream = adminUtils.addUserToStream(newUserEmail, 
                                        ta.USER_PW, 
                                        "붕어빵방-비공개", 
                                        addUserEmail);
    }

}
