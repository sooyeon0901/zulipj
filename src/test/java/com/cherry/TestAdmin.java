package com.cherry;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

import com.github.jamesnetherton.zulip.client.Zulip;
import com.github.jamesnetherton.zulip.client.api.user.User;
import com.github.jamesnetherton.zulip.client.exception.ZulipClientException;
import com.github.jamesnetherton.zulip.client.http.ZulipConfiguration;
import com.github.jamesnetherton.zulip.client.http.ZulipHttpClient;
import com.github.jamesnetherton.zulip.client.http.commons.ZulipCommonsHttpClient;

public class TestAdmin {
  // 공통
  public static final String SITE_URL = "https://ai.e4net.net";
  public static final String USER_PW = "e4net123";

  public static final String ADMIN_EMAIL = "zulip@cherrycorp.io";
  public static final String ADMIN_API_KEY = "YFeqVpDmkco4tXRtdnJezpObp3XZ9fJT";


  // admin 세팅
  @Test
  public void setAdmin() throws MalformedURLException, ZulipClientException {
      URL url;
      try {
          url = new URL(SITE_URL);
      } catch (Exception e) {
          throw new MalformedURLException();
      }
      ZulipConfiguration adminConfig = new ZulipConfiguration(url, ADMIN_EMAIL, ADMIN_API_KEY);
      adminConfig.setInsecure(true); //ssl 통신을 안함

      Zulip z_admin = new Zulip(adminConfig);
      ZulipHttpClient adminClient = new ZulipCommonsHttpClient(adminConfig);
      ZulipAdminUtils adminUtils = new ZulipAdminUtils(adminConfig);

      //return adminUtils;
  }

}
