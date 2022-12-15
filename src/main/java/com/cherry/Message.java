package com.cherry;

import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;

import com.github.jamesnetherton.zulip.client.Zulip;
import com.github.jamesnetherton.zulip.client.api.user.User;
import com.github.jamesnetherton.zulip.client.exception.ZulipClientException;
import com.github.jamesnetherton.zulip.client.http.ZulipConfiguration;
import com.github.jamesnetherton.zulip.client.http.ZulipHttpClient;
import com.github.jamesnetherton.zulip.client.http.commons.ZulipCommonsHttpClient;

@Slf4j
public class Message {
  public static void testSendStreamMessage(Zulip z_user) throws MalformedURLException, ZulipClientException {
    //ZulipAdminUtils adminUtils = setAdmin();
    //Zulip z_user = adminUtils.createZulip("test001@cherrycorp.io", USER_PW);
    Long messageId = z_user.messages()
                    .sendStreamMessage("메시지테스트33", "단체1-공개", "테스트토픽")
                    .execute();
    log.info("messageId : {}", messageId);
    z_user.close(); // 닫기
    //assertEquals(z_user, messageId);
  }

public static void main(String[] args) throws MalformedURLException, ZulipClientException {
    Admin ta = new Admin();
    ZulipAdminUtils adminUtils = ta.setAdmin();
    Zulip z_user = adminUtils.createZulip("test001@cherrycorp.io", ta.USER_PW);
    testSendStreamMessage(z_user);
  }


}
