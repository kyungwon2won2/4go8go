# OAuth 및 사용자 인증 시스템 리팩토링

## 변경 사항 요약

### 1. 카카오 로그인 제거
- KakaoOAuth2UserInfo 클래스 참조 제거
- OAuthAttributes 클래스에서 카카오 처리 로직 제거
- 관련 서비스 및 핸들러에서 카카오 로직 제거

### 2. OAuth 패키지 구조 개선
기존의 평면적인 구조에서 다음과 같은 계층 구조로 개선:

```
oauth/
├── controller/           - 컨트롤러 클래스
│   └── OAuth2RegisterController.java
├── handler/              - 인증 처리 핸들러
│   ├── OAuth2AuthenticationFailureHandler.java
│   └── OAuth2LoginSuccessHandler.java
├── model/                - 모델 클래스
│   ├── GoogleOAuth2UserInfo.java
│   ├── NaverOAuth2UserInfo.java
│   ├── OAuthAttributes.java
│   └── OAuth2UserInfo.java
└── service/              - 서비스 클래스
    ├── CustomOAuth2UserService.java
    ├── OAuth2UserService.java
    └── OAuth2UserServiceImpl.java
```

### 3. 소프트 삭제(Status) 기능 개선
- 사용자 삭제 시 status 필드를 'DELETED'로 설정
- 로그인 시 status 확인하여 탈퇴 회원 로그인 방지
- 소셜 로그인에서도 탈퇴 회원 검사 로직 추가

### 4. 소셜 로그인 필수 정보 확인 로직 구현
- 소셜 로그인 시 필수 정보(phone 등) 누락 확인
- 필수 정보 없는 경우 추가 정보 입력 페이지로 리디렉션
- OAuth2UserService 인터페이스 도입으로 결합도 감소

### 5. 서비스 레이어 개선
- OAuth2UserService 인터페이스 및 구현 분리
- UserService에 상태 확인 메소드 추가
- 예외 처리 및 로깅 강화

## 주요 개선점

1. **구조적 개선**
   - 클래스 역할 분리로 코드 가독성 향상
   - 관심사 분리를 통한 유지보수성 개선
   - 표준 패키지 구조 적용

2. **기능 개선**
   - 소프트 삭제 기능 안정화
   - 소셜 로그인 프로세스 개선
   - 필수 정보 검증 및 추가 정보 입력 프로세스 강화

3. **코드 품질 향상**
   - 적절한 주석 추가
   - 명확한 메소드명 및 변수명 사용
   - 불필요한 코드 제거

## 변경 파일 목록

1. `/src/main/java/com/example/demo/common/oauth/**` - 전체 패키지 구조 변경
2. `/src/main/java/com/example/demo/common/security/config/SecurityConfig.java` - 보안 설정 변경
3. `/src/main/java/com/example/demo/common/security/service/CustomerDetailService.java` - 카카오 참조 제거
4. `/src/main/java/com/example/demo/domain/user/service/UserService.java` - 상태 확인 메소드 추가

## 주의 사항

1. 카카오 로그인 관련 코드는 모두 제거되었으므로, 추후 필요하면 새로 구현해야 함
2. 소셜 로그인 시 필수 정보 확인 로직이 추가되었으므로, 프론트엔드에서 관련 페이지 확인 필요
3. 사용자 상태(status)에 따른 처리 로직이 추가되었으므로, 기존 탈퇴 회원 처리 확인 필요