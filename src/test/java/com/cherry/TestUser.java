package com.cherry;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import java.net.MalformedURLException;
import java.util.List;

import org.junit.Test;
import com.github.jamesnetherton.zulip.client.Zulip;
import com.github.jamesnetherton.zulip.client.api.user.EmojiSet;
import com.github.jamesnetherton.zulip.client.api.user.User;
import com.github.jamesnetherton.zulip.client.api.user.UserGroup;
import com.github.jamesnetherton.zulip.client.api.user.UserRole;
import com.github.jamesnetherton.zulip.client.api.user.request.CreateUserApiRequest;
import com.github.jamesnetherton.zulip.client.api.user.response.GetUserGroupsApiResponse;
import com.github.jamesnetherton.zulip.client.exception.ZulipClientException;
import com.github.jamesnetherton.zulip.client.http.ZulipHttpClient;

@Slf4j
public class TestUser extends TestCase {
    private Admin ta = new Admin();

    /*
     * 22.12.16 기준 
     * admin - zulip@cherrycorp.io  : "isAdmin": true, "isOwner": false, "deliveryEmail" 값 존재
     * owner - suyeun1215@naver.com  : "isAdmin": true, "isOwner": true,
     * 
     * getOwnUser()가 admin을 가져오는 이슈가 있음.
    */
    // 사용자 정보 가져오기 
    @Test
    public void testGetUserInfo() throws ZulipClientException, MalformedURLException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        Zulip zulipUser = adminUtils.createZulip(ta.ADMIN_EMAIL, ta.USER_PW);

        // 전체 사용자 목록 가져오기
        List<User> all_users = zulipUser.users().getAllUsers().execute();
        adminUtils.jsonPrint(all_users);

        // owner 권한인 사용자 가져오기 == admin을 가져오는 이슈 발생
        // User owner = zulipUser.users().getOwnUser().execute();
        // adminUtils.jsonPrint(owner);

        // 이메일 직접 입력으로 사용자 가져오기 (1)
        // User one_user = zulipUser.users().getUser("test001@cherrycorp.io").execute();
        // adminUtils.jsonPrint(one_user);
        // 이메일 직접 입력으로 사용자 가져오기 (2)
        // User one_user = adminUtils.getUserByEmail("suyeun1215@naver.com");
        // adminUtils.jsonPrint(one_user);
    }

    // owner 정보 가져오기
    @Test
    public void testGetOwnerInfo() throws MalformedURLException, ZulipClientException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        String ownerEmail = "suyeun1215@naver.com"; //owner 이메일
        adminUtils.createConfig(ownerEmail, ta.USER_PW);
        ZulipHttpClient userClient = adminUtils.createClient(ownerEmail, ta.USER_PW);
        Zulip zulipUser = adminUtils.createZulip(ownerEmail, ta.USER_PW);

        User user = zulipUser.users().getUser(ownerEmail).execute();
        adminUtils.jsonPrint(user);
    }

    // 사용자 생성 - 사용자 생성권한이 있는 admin만 가능
    // @Test
    // public void testCreateUser() throws ZulipClientException, MalformedURLException {
    //     ZulipAdminUtils adminUtils = ta.setAdmin();
    //     ZulipHttpClient userClient = adminUtils.createClient(ta.ADMIN_EMAIL, ta.USER_PW);
    //     Long cu = new CreateUserApiRequest(userClient, 
    //                                     "test004@cherrycorp.io", // 생성할 이메일
    //                                     "눈사람", // 생성할 이름
    //                                     "e4net123").execute(); // 생성할 비밀번호
    // }

    // 사용자 생성 권한 주기 - cmd로만 가능, admin 한 사람만 해당 권한을 주면 됨. 

    // 사용자 삭제 - cmd로만 가능?

    // user의 권한 변경
    @Test
    public void testSetUserAuth() throws ZulipClientException, MalformedURLException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        Zulip zulipUser = adminUtils.createZulip(ta.ADMIN_EMAIL, ta.USER_PW); // zulip에 지정된 아이디/비번이 admin일 때
        //User user = zulipUser.users().getOwnUser().execute(); // user == admin
        User user = zulipUser.users().getUser("test003@cherrycorp.io").execute(); // user == member

        // zulip에 지정된 아이디/비번이 member의 것일 때
        Zulip zulip_mem = adminUtils.createZulip("test003@cherrycorp.io", ta.USER_PW);


        // goguma의 권한이 admin으로 변경됨
        // zulipUser.users().updateUser(user.getUserId()).withRole(UserRole.MEMBER)
        // //.withFullName(user.getFullName()) // (TODO) fullName을 하드코딩하지 않는 방안을 고민
        // .withFullName("배고파요").execute();

        // 사용자 이모지 변경
        // zulip_mem.users().updateOwnUserStatus().withEmojiName("happy").execute();

        // 사용자 그룹 조회
        // List<UserGroup> ug = zulip_mem.users().getUserGroups().execute();
        // adminUtils.jsonPrint(ug);

        // 사용자 그룹 수정
        // zulip_mem.users().updateUserGroup("신나는 금요일 그룹", "금요일에 기분이 좋은 사람들 모임", 57).execute();
    }

    // 모든 사용자의 이메일 알림 기능 끄기
    @Test
    public void testTurnOffEmailNoti() throws ZulipClientException, MalformedURLException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        Zulip zulipUser = adminUtils.createZulip(ta.ADMIN_EMAIL, ta.USER_PW);
        List<User> users = zulipUser.users().getAllUsers().execute();

        // 활동하는 사용자이고 + owner가 아니고 + admin도 아닌 사용자만 노티 끔
        for(User user : users){
            System.out.println("활성화 : " + user.isActive() + " , " + user.getEmail());
            if(!user.isAdmin() && user.isActive() && !user.isOwner()){
                adminUtils.turnOffAllEmailNotification(user.getEmail(), ta.USER_PW);
            }
        }
    }




}
