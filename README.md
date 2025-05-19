# 4go8go - 중고물품 거래 플랫폼

## 🚀 소개

이 프로젝트는 사용자가 중고 물품을 사고 팔 수 있는 웹 기반의 **중고물품 거래 플랫폼** 입니다.<br>
사용자 친화적인 인터페이스와 여러 기능들을 통해 물품을 거래할 수 있도록 구현하였습니다.

<br>

### 📆 프로젝트 기간
- 2025.05.12 ~ 2025.06.02

<br><br>


## 😎 Members
<table>
  <tbody>
      <td align="center"><a href="https://github.com/K-IMjihun"><img src="https://avatars.githubusercontent.com/u/62210749?v=4" width="50px;" alt=""/><br /><sub><b> 팀장 : 김지훈 </b></sub></a><br /></td>
      <td align="center"><a href="https://github.com/qotkdgus0430"><img src="https://avatars.githubusercontent.com/u/200076152?v=4" width="50px;" alt=""/><br /><sub><b> 팀원 : 배상현 </b></sub></a><br /></td>
      <td align="center"><a href="https://github.com/kyungwon2won2"><img src="https://avatars.githubusercontent.com/u/134581020?v=4" width="50px;" alt=""/><br /><sub><b> 팀원 : 여경원 </b></sub></a><br /></td>
      <td align="center"><a href="https://github.com/kyungwon2won2"><img src="https://avatars.githubusercontent.com/u/134581020?v=4" width="50px;" alt=""/><br /><sub><b> 팀원 : 이지현 </b></sub></a><br /></td>
  </tbody>
</table>

<br><br>

## 📌 주요 기능

- **회원가입/로그인:** 사용자 인증을 통해 안전한 거래 환경 제공
- **물품 등록/검색:** 쉽게 물품을 등록하고 검색할 수 있는 기능
- **카테고리별 물품 조회:** 다양한 카테고리로 물품을 필터링하여 손쉽게 탐색
- **채팅 기능:** 구매자와 판매자 간의 실시간 채팅 지원
- **마이 페이지:** 내 판매/구매 내역 관리 및 개인정보 수정

## 🛠 Tools
### 🖥 Frontend
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=flat&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=flat&logo=css3&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=flat&logo=Thymeleaf&logoColor=white)

### 🔧 Backend
![Java](https://img.shields.io/badge/Java-007396?style=flat&logo=java&logoColor=white)
![Spring](https://img.shields.io/badge/Spring-6DB33F?style=flat&logo=spring&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat&logo=springboot&logoColor=white)

### 🗄 Database
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat&logo=mysql&logoColor=white)

### ☁ Infra / Tools
![Amazon EC2](https://img.shields.io/badge/Amazon%20EC2-FF9900?style=flat&logo=amazon-ec2&logoColor=white)
![Amazon RDS](https://img.shields.io/badge/Amazon%20RDS-527FFF?style=flat&logo=amazon-rds&logoColor=white)
![Amazon S3](https://img.shields.io/badge/Amazon%20S3-569A31?style=flat&logo=amazon-s3&logoColor=white)

![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)
![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=flat&logo=jenkins&logoColor=white)
![Git](https://img.shields.io/badge/Git-F05032?style=flat&logo=git&logoColor=white)

<br>

## E2E 테스트 (Playwright)

- Playwright를 사용한 E2E 테스트는 프로젝트 루트에 playwright.config.js와 tests/ 디렉토리를 생성하여 관리합니다.
- 서버는 반드시 8090 포트에서 실행되어야 합니다.
- 테스트 실행 전, 다음 명령어로 playwright와 브라우저를 설치하세요:

```
npm install @playwright/test
npx playwright install
```

- 테스트 실행:
```
npx playwright test --headed
```

- 테스트 위치: `tests/join.e2e.spec.js` (회원가입/주소 API/validation 등)

<br>
<br>