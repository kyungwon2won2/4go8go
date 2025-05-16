package com.example.demo.common.oauth;

import java.util.Map;

public interface OAuth2UserInfo {
    Map<String, Object> getAttributes();
    String getId();
    String getName();
    String getEmail();
    String getImageUrl();
}