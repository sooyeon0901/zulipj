package com.cherry;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import java.net.MalformedURLException;
import org.junit.Test;
import com.github.jamesnetherton.zulip.client.Zulip;
import com.github.jamesnetherton.zulip.client.exception.ZulipClientException;


@Slf4j
public class TestMessage extends TestCase {

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

    

}
