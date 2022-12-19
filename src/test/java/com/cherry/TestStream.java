package com.cherry;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import com.github.jamesnetherton.zulip.client.Zulip;
import com.github.jamesnetherton.zulip.client.api.stream.Stream;
import com.github.jamesnetherton.zulip.client.api.stream.StreamService;
import com.github.jamesnetherton.zulip.client.api.stream.StreamSubscriptionRequest;
import com.github.jamesnetherton.zulip.client.api.stream.StreamSubscriptionResult;
import com.github.jamesnetherton.zulip.client.api.user.User;
import com.github.jamesnetherton.zulip.client.exception.ZulipClientException;
import com.github.jamesnetherton.zulip.client.http.ZulipHttpClient;


@Slf4j
public class TestStream extends TestCase {

    private Admin ta = new Admin();

    // (해당 멤버가 속한) 스트림 목록 가져오기
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

    // 전체 스트림 목록 가져오기
    @Test
    public void testGetAllStreams() throws ZulipClientException, MalformedURLException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        ZulipHttpClient z_client = adminUtils.createClient(ta.ADMIN_EMAIL, ta.USER_PW);

        adminUtils.jsonPrint(
            new StreamService(z_client).getAll()
            .withIncludeAllActive(true)
            .withIncludeDefault(false)
            .execute()
        );
    }

    // 스트림에서 토픽 불러오기
    // @Test
    // public void testGetAllTopics() throws ZulipClientException, MalformedURLException {
    //     ZulipAdminUtils adminUtils = ta.setAdmin();
    //     ZulipHttpClient z_client = adminUtils.createClient(ta.ADMIN_EMAIL, ta.USER_PW);

    //     adminUtils.jsonPrint(
    //         new StreamService(z_client).getTopics(43)
    //         .execute()
    //     );
        
    //     Zulip zulip = adminUtils.createZulip(ta.ADMIN_EMAIL, ta.USER_PW);
    //     // 토픽 추가 || 특정 토픽으로 메세지 보내기
    //     zulip.messages().sendStreamMessage(
    //         "테슷후222", "붕어빵방-비공개", "새토픽"
    //     ).execute();
    // }

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
    // @Test
    // public void testCreatePrivStream() throws ZulipClientException, MalformedURLException {
    //     String newUserEmail = "test002@cherrycorp.io";
    //     ZulipAdminUtils adminUtils = ta.setAdmin();
    //     String pubStreamName = "붕어빵방-비공개";
    //     String pubStreamNameDesc = "겨울에 붕어빵 먹는 사람 모입입니다.";

    //     StreamSubscriptionResult stream = adminUtils.createStream(pubStreamName,
    //                                     pubStreamNameDesc,
    //                                     true,
    //                                     Arrays.asList(newUserEmail));
    // }
    // 공개 스트림 - 비공개스트림 \


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
    public void testAddUserToPrivStream(String alreadyMember, String newUserEmail) throws ZulipClientException, MalformedURLException {
        //newUserEmail = "test002@cherrycorp.io"; //비공개방에 존재하는 회원이 직접 추가하는 방식
        List<String> addUserEmail = new ArrayList<>();
        addUserEmail.add(newUserEmail);

        ZulipAdminUtils adminUtils = ta.setAdmin();
        StreamSubscriptionResult stream = adminUtils.addUserToStream(alreadyMember, // 추가할 사용자(비공개방의 회원인)
                                        ta.USER_PW, 
                                        "붕어빵방-비공개", 
                                        addUserEmail); // 추가될 사용자
    }

    // 비공개 스트림 멤버가 비공개 스트림에 메시지 보내기
    @Test
    public void testSendMsgToPrivStream() throws ZulipClientException, MalformedURLException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        // 비공개 방에 없는 회원 추가하기 - ex) (1) admin을 비공개방에 추가하기
        // Zulip z_admin = adminUtils.createZulip(ta.ADMIN_EMAIL, ta.USER_PW); // admin
        // testAddUserToPrivStream("test002@cherrycorp.io", ta.ADMIN_EMAIL); // 순서대로 : 비공개방 기존 회원 - 비공개방 초대 대상 
        // // send msg
        // z_admin.messages().sendStreamMessage("비공개방 초대 감사요", "붕어빵방-비공개", "stream events").execute();

        // (2) member 권한이 비공개 방에 없는 회원 추가하기
        Zulip z_member = adminUtils.createZulip("test001@cherrycorp.io", ta.USER_PW);
        testAddUserToPrivStream("test001@cherrycorp.io", "test004@cherrycorp.io");
        z_member.messages().sendStreamMessage("001님 비공개방 초대 감사요22", "붕어빵방-비공개", "stream events").execute();
    }

    // 스트림 공개/비공개 설정
    @Test
    public void testSetStream() throws ZulipClientException, MalformedURLException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        //Zulip z_admin =  adminUtils.createZulip(ta.ADMIN_EMAIL, ta.USER_PW);
        // 테스트를 위해 관리자 삭제(공개/비공개 모두)
        adminUtils.jsonPrint(
            adminUtils.removeUserFromStream("붕어빵방-비공개", Arrays.asList("zulip@cherrycorp.io"))
        );
        adminUtils.jsonPrint(
            adminUtils.removeUserFromStream("군고구마방-공개", Arrays.asList("zulip@cherrycorp.io"))
        );

        // 상태를 변경하려면 해당 스트림에 존재하는 멤버여야 함. 여러개를 한꺼번에 한다면, 공통으로 존재하는 멤버여야 함.
        HashMap<String, Boolean> stream = new HashMap<>();
        // 공개 - 비공개, 비공개 - 공개
        stream.put("군고구마방-공개", true);
        stream.put("붕어빵방-비공개", false);
        // 공개 - 공개, 비공개 - 비공개
        // stream.put("군고구마방-공개", true);
        // stream.put("붕어빵방-비공개", false);

        adminUtils.changeStreamPrivacy("test004@cherrycorp.io", "e4net123", stream);
    }

    // 스트림 구독/구독 취소하기 (에러 나지만 구독/구독 취소는 됨) , description이 틀려도 상관없음.
    @Test
    public void testSetSubscribe() throws ZulipClientException, MalformedURLException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        //Zulip zulipUser = adminUtils.createZulip(adminUtils.config().getEmail(), ta.USER_PW);
        Zulip z_member = adminUtils.createZulip("test003@cherrycorp.io", ta.USER_PW);

        // 스트림 구독 취소
        // List<Stream> streamList = (List<Stream>) z_member.streams().unsubscribe("general").execute();
        // 스트림 구독
        // List<Stream> streamList = (List<Stream>) z_member.streams().subscribe(StreamSubscriptionRequest.of("단체1-공개", "단체공개방입니다")).execute();
        z_member.streams().subscribe(StreamSubscriptionRequest.of("군고구마방-공개", "겨울에 군고구마 먹는 사람 모입입니")).execute();
        z_member.streams().subscribe(StreamSubscriptionRequest.of("general", "Everyone is added to this stream by default. Welcome! :octopus:")).execute();
    }


}
