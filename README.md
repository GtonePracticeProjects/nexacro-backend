# nexacro-backend (Nexacro 샘플 프로젝트)

화면 → URL → Controller → Service → DAO → SQL 호출 지점을 찾아 연결하는 분석 솔루션의 **Nexacro 샘플** (프론트 + 백엔드).
`base-v1.0` (Spring MVC 5 XML + MyBatis + Oracle, JSP) 에서 분기해 JSP 화면을 Nexacro 화면으로 대체했다.

## 검증 상태 (먼저 읽을 것)

- **프론트엔드(`frontend/`)는 Nexacro Studio(IDE)에서 열어 보지 못했다.** Nexacro IDE/런타임/라이선스가 없는 PC에서
  텍스트로 작성한 소스이며, XML well-formed 여부만 확인했다. Studio 에서 열 때 FDL 버전·속성명·컴포넌트 문법 차이로
  경고/수정이 필요할 수 있다. (가정 버전: **Nexacro N, FDL 2.1 / xscript5.1** — 정확한 빌드 번호는 미확인)
- **백엔드(`backend/`)는 Maven 빌드(컴파일)를 하지 못했다.** Maven 과 Oracle 이 설치되지 않았다. 확인한 것은
  `adapter` 패키지 중 순수 JDK 부분(`NexacroDataset/Request/Response/Adapter`, `XmlNexacroAdapter`)을
  servlet/Spring 스텁으로 `javac` 컴파일 + XML 읽기/쓰기 왕복 실행한 것뿐이다. `BeanDatasets` 와 `/nexacro/**`
  Controller 는 컴파일해 보지 않았다. 실제 Nexacro 클라이언트와의 통신, Oracle 연동도 실행해 보지 않았다.
- 따라서 이 저장소는 "분석 솔루션이 읽을 소스"로서의 샘플이며, 동작 보증은 없다.

## 구조

```
backend/    Spring MVC 5 + MyBatis + Oracle (war).  pom.xml, src, sql/oracle
frontend/nexacro/   Nexacro N 텍스트 소스
  typedefinition.xml        서비스 프리픽스: svc(백엔드 URL) / lib(.xjs) / frm(.xfdl)
  Application_Desktop.xadl  애플리케이션/프레임
  lib/Common.xjs            공통 래퍼 gfnTransaction / gfnCallback
  form/{Menu,Account,Customer,Loan}Form.xfdl
docs/call-points.csv        호출 지점 정답지(ground truth)
```

## 백엔드

- 계층: `Controller → Service(인터페이스) → ServiceImpl → DAO/Mapper`. DB 접근 4가지 방식과 Oracle 스크립트는 원본 그대로
  (account: MyBatis XML+SqlSessionTemplate / customer: 순수 JDBC / loan: SQL 빌더+JdbcTemplate / notice: MyBatis 어노테이션).
- 삭제: `sample` 모듈, 모든 JSP, 기존 JSP용 Controller(`/account`, `/customer`, `/loan`, `/notice`), jQuery/JSTL/Jackson 의존성,
  JSON 기반 예외 처리기. Nexacro 화면은 JSP 가 아닌 Dataset 을 주고받는다.
- 추가: `com.bank.nexacro`
  - `adapter/` — **Dataset 변환은 전부 여기**. `NexacroAdapter` 인터페이스(`read`/`write`)와 벤더 jar 비의존 기본 구현
    `XmlNexacroAdapter`(PlatformXml 직접 읽고 쓰기, SSV/Binary 미지원), 중립 모델 `NexacroDataset/Request/Response`,
    VO↔Dataset 변환 `BeanDatasets`. 벤더 어댑터를 쓰려면 `NexacroAdapter` 구현체를 새로 만들고 `XmlNexacroAdapter`의
    `@Component` 를 대체하면 된다 (Controller 변경 불필요).
  - `controller/` — `/nexacro/**` Controller 4개 + `NexacroExceptionHandler`. 기존 Service 를 그대로 호출한다.
- 오류 규약: HTTP 200 + `ErrorCode`(0 성공, `-1` 업무 오류, `-999` 시스템 오류) / `ErrorMsg`. 업무 에러코드(E###)는 변수 `BizCode`.

### 엔드포인트 (모두 POST, 컨텍스트 경로 `/bank`)

| URL | Service 호출 |
|---|---|
| `/nexacro/account/selectAccountList.do` | `AccountService.getAccountList` |
| `/nexacro/account/selectAccount.do` | `getAccount` + `getTransHistList` |
| `/nexacro/account/transfer.do` | `transfer` |
| `/nexacro/customer/selectCustomerList.do` | `CustomerService.getCustomerList` |
| `/nexacro/customer/registerCustomer.do` | `registerCustomer` |
| `/nexacro/loan/selectLoanList.do` | `LoanService.getLoanList` |
| `/nexacro/loan/applyLoan.do` | `applyLoan` |
| `/nexacro/loan/approveLoan.do`, `rejectLoan.do` | `decideLoan(loanNo, true/false)` |
| `/nexacro/notice/selectNoticeList.do`, `readNotice.do`, `writeNotice.do` | `NoticeService` (프론트 화면 없음) |

### 벤더 라이브러리 (이 저장소에는 포함하지 않음)

운영에서 벤더 어댑터를 쓰려면 Nexacro 제공 서버 라이브러리가 필요하다. **Maven 좌표를 임의로 적지 않았으며**
제품 설치 패키지/벤더 포털에서 받은 파일의 실제 이름·버전을 확인해 `pom.xml` 에 추가해야 한다.
- Nexacro 서버측 데이터 처리 라이브러리(xapi 계열 jar)
- Nexacro UI Adapter(Spring MVC 연동 jar, 사용 중인 Nexacro 버전용)
- 위 jar 의 의존 라이브러리 (벤더 문서 참조)

현재 `pom.xml` 에는 벤더 jar 가 없고, 어댑터 코드는 벤더 클래스를 참조하지 않는다.

## 프론트엔드 (가정: Nexacro N, xscript5.1)

호출 방식을 의도적으로 섞었다 (분석 솔루션의 쉬운/어려운 케이스).

| 방식 | 화면 | 내용 |
|---|---|---|
| direct | AccountForm 목록 조회, CustomerForm 검색, LoanForm 신청 | `this.transaction(id, "svc::…do", …)` 직접 호출 |
| wrapper | AccountForm 상세/이체, CustomerForm 등록, LoanForm 목록 | `this.gfnTransaction(...)` (`lib/Common.xjs`) → 내부에서 `this.transaction` |
| concatenated | LoanForm 승인/거절 | `"svc::nexacro/loan/" + sAct + "Loan.do"` 문자열 연결 |

`svc::` 는 `typedefinition.xml` 의 서비스 프리픽스로 `http://localhost:8080/bank/` 에 대응한다.
Dataset 매핑 문법: 입력 `서버Ds=클라이언트Ds`, 출력 `클라이언트Ds=서버Ds`.

정답지는 `docs/call-points.csv` (화면, 호출 방식, 서비스ID, URL, 입력/출력 Dataset, Controller 메서드).
승인/거절 URL 은 동적이므로 CSV 에 해석된 두 URL 을 각각 한 행으로 적었다.

## Oracle / 실행 (미검증)

1. 계정 `BANK_NX` 로 `backend/sql/oracle/01_create_tables.sql`, `02_seed_data.sql` 실행 (재실행 시 `00_drop_all.sql`)
2. `backend/src/main/resources/config/jdbc.properties` — 사용자 `BANK_NX`, **비밀번호는 비어 있음**(환경에 맞게 입력)
3. `cd backend && mvn jetty:run` → 컨텍스트 `/bank`

데이터 입력 규칙(계좌/대출 상태코드, 고객번호 채번 등)은 원본과 같다: 계좌 `A/C`, 거래 `W/D`, 대출 `R/A/X`, 고객번호 `C000001`~.
