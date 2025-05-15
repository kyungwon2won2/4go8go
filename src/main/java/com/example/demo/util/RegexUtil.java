package com.example.demo.util;

public class RegexUtil {

    private RegexUtil() {}

    public static final String REGEXP_PASSWORD = "(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}";
    // 비밀번호 8~16자 영문 대 소문자, 숫자, 특수문자
    // ^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,}$
    public static final String REGEXP_LOGIN_ID = "^[a-z0-9]+$";
    public static final String REGEXP_EMAIL = "^([A-Za-z0-9]+([-_.]?[A-Za-z0-9]+)*)@"
            + "([A-Za-z0-9]+([-]?[A-Za-z0-9]+)*)"
            + "(\\.([A-Za-z0-9]+([-]?[A-Za-z0-9]+)*))?"
            + "(\\.([A-Za-z0-9]+([-]?[A-Za-z0-9]+)*))?"
            + "((\\.[A-Za-z]{2,63})$)";
    public static final String REGEXP_MOBILE = "^\\d{2,3}-\\d{3,4}-\\d{4}$";
    public static final String REGEXP_NICKNAME = "^[a-zA-Z0-9가-힣]+$";
}