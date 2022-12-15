package com.cherry;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import java.net.MalformedURLException;
import org.junit.Test;
import com.github.jamesnetherton.zulip.client.Zulip;
import com.github.jamesnetherton.zulip.client.api.user.User;
import com.github.jamesnetherton.zulip.client.exception.ZulipClientException;
import com.github.jamesnetherton.zulip.client.http.ZulipHttpClient;


@Slf4j
public class TestUser extends TestCase {
    private Admin ta = new Admin();

    // 이메일로 사용자 정보 가져오기
    @Test
    public void testGetUserInfo() throws ZulipClientException, MalformedURLException {
        ZulipAdminUtils adminUtils = ta.setAdmin();
        String newUserEmail = "test001@cherrycorp.io"; //가져올 사용자 이메일
        adminUtils.jsonPrint(adminUtils.getUserByEmail(newUserEmail));
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


}
