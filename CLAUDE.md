# CLAUDE.md – GreenMate AI Agent 서버 MVP

## 📌 에이전트 개요
- **에이전트명**: GreenMate AI Agent (MVP)  
- **소속 시스템**: LifePlus GreenMate  
- **역할**: AI 기반 개인 맞춤형 건강 생활 추천 시스템  
- **목적**: 사용자 개인정보 기반 걸음수 추천, 위치 기반 도보 경로 추천, 키워드 기반 위치 검색 제공  

---

## 🎯 에이전트 역할 정의
당신은 **LifePlus GreenMate**의 AI Agent 서버입니다.  
MVP 단계에서는 **3가지 핵심 기능**에 집중합니다.  

**주요 책임**
1. **걸음수 추천**: Backend서버로부터 사용자 프로필 데이터(성별, 나이, 키, 몸무게) 수신 → OpenAI API로 개인 맞춤형 일일 걸음수 계산 → Backend서버로 결과 송신
2. **도보 경로 추천**: Backend서버로부터 위치정보(위도, 경도), 잔여 걸음수, 사용자 프로필 수신 → OpenAI API로 3가지 도보 코스 생성 → Backend서버로 결과 송신
3. **위치 검색**: Backend서버로부터 키워드(주소, 건물명, 지하철역명 등) 수신 → OpenAI API로 위도/경도 계산 → Backend서버로 결과 송신

---

## 🔗 API 인터페이스

### 1. 걸음수 추천 API
**Endpoint:** `POST /api/v1/recommend/steps`

#### 입력 예시
```json
{
  "user_id": "user123",
  "gender": "male",
  "age": 30,
  "height": 175,
  "weight": 70
}
```

#### 출력 예시
```json
{
  "status": "success",
  "data": {
    "recommended_steps": 8500
  }
}
```

### 2. 도보 경로 추천 API
**Endpoint:** `POST /api/v1/recommend/routes`

#### 입력 예시
```json
{
  "user_id": "user123",
  "latitude": 37.5665,
  "longitude": 126.9780,
  "remaining_steps": 3000,
  "user_profile": {
    "gender": "male",
    "age": 30,
    "height": 175,
    "weight": 70
  }
}
```

#### 출력 예시
```json
{
  "status": "success",
  "data": {
    "routes": [
      {
        "type": "Less Walk",
        "distance": 1.2,
        "duration": 15,
        "steps": 1680,
        "description": "가까운 거리 산책 코스",
        "route_steps": [
          { "instruction": "서울시청에서 남쪽 시청광장 방향으로 시작", "distance_m": 200, "duration_min": 3 },
          { "instruction": "시청광장을 가로질러 세종대로 방향", "distance_m": 400, "duration_min": 5 },
          { "instruction": "세종대로를 따라 남쪽으로 직진", "distance_m": 600, "duration_min": 7 }
        ]
      },
      {
        "type": "Recommended",
        "distance": 2.1,
        "duration": 25,
        "steps": 3000,
        "description": "적정 운동량 산책 코스",
        "route_steps": [
          { "instruction": "서울시청에서 동쪽 덕수궁 방향으로 시작", "distance_m": 250, "duration_min": 3 },
          { "instruction": "덕수궁 둘레길을 반시계방향으로 산책", "distance_m": 800, "duration_min": 10 },
          { "instruction": "정동길을 통해 경복궁 방향으로", "distance_m": 600, "duration_min": 7 },
          { "instruction": "광화문광장을 가로질러", "distance_m": 450, "duration_min": 5 }
        ]
      },
      {
        "type": "More Walk",
        "distance": 3.5,
        "duration": 42,
        "steps": 5000,
        "description": "활발한 운동 산책 코스",
        "route_steps": [
          { "instruction": "서울시청에서 북쪽 덕수궁 방향으로 시작", "distance_m": 300, "duration_min": 4 },
          { "instruction": "덕수궁 돌담길을 따라 산책", "distance_m": 600, "duration_min": 8 },
          { "instruction": "정동길을 통해 서울시립미술관 방향", "distance_m": 500, "duration_min": 6 },
          { "instruction": "청계천 산책로를 따라 동쪽으로", "distance_m": 800, "duration_min": 10 },
          { "instruction": "종로 1가를 거쳐 북쪽으로", "distance_m": 700, "duration_min": 9 },
          { "instruction": "인사동길을 통해 전통문화 체험", "distance_m": 600, "duration_min": 8 },
          { "instruction": "목적지까지 마지막 도보", "distance_m": 300, "duration_min": 2 }
        ]
      }
    ]
  },
  "message": "경로 추천이 완료되었습니다."
}
```

### 3. 위치 검색 API
**Endpoint:** `POST /api/v1/geocode`

#### 입력 예시
```json
{
  "keyword": "서울시청"
}
```

#### 출력 예시
```json
{
  "status": "success",
  "data": {
    "latitude": 37.5665,
    "longitude": 126.9780,
    "address": "서울특별시 중구 세종대로 110",
    "confidence": 0.95
  },
  "message": "위치 검색이 완료되었습니다."
}
```

---

## 🛠️ 기술 스택

### Backend Framework
- **Spring Boot 3.2+** - Java 17/21 기반 애플리케이션 프레임워크
- **Spring Web** - RESTful API 개발
- **Spring Boot Validation** - 요청 데이터 검증

### External APIs
- **OpenAI API** - GPT 기반 AI 추천 로직

### Build & Dependency Management
- **Gradle 8.5+** - 빌드 도구

### API Documentation
- **SpringDoc OpenAPI 3** - API 문서 자동 생성
- **Swagger UI** - API 테스트 인터페이스

### HTTP Client
- **RestTemplate** - 동기 HTTP 클라이언트
- **WebClient** - 비동기 HTTP 클라이언트 (백업)

### JSON Processing
- **Jackson** - JSON 직렬화/역직렬화

### Testing
- **JUnit 5** - 단위 테스트
- **Mockito** - Mock 프레임워크

### Monitoring & Logging
- **SLF4J + Logback** - 로깅 프레임워크

### Development Tools
- **Spring Boot DevTools** - 개발 편의 도구
- **Lombok** - 보일러플레이트 코드 제거

### Deployment
- **nginx** - 웹서버/리버스 프록시 (포트: 9000)

---

## 🧩 AI 추천 로직 (MVP)

### 걸음수 추천 로직
- **개인 특성 분석**: 성별, 나이, 키, 몸무게 기반 기초대사량 계산
- **활동 수준 고려**: 나이대별, 체중별 권장 활동량 반영
- **실현 가능한 목표**: 점진적 증가 가능한 적정 걸음수 제안

### 도보 경로 추천 로직  
- **Less Walk**: 잔여 걸음수의 50% 수준, 짧고 편안한 코스
- **Recommended**: 잔여 걸음수의 90-100% 수준, 적정 운동량 코스
- **More Walk**: 잔여 걸음수의 120-150% 수준, 활발한 운동 코스
- **개인화 요소**: 사용자 프로필 반영하여 코스 강도 조절

### 위치 검색 로직
- **키워드 분석**: 주소, 건물명, 지하철역, 랜드마크 구분
- **지역 컨텍스트**: 한국 지역 정보 우선 검색
- **정확도 평가**: 검색 결과 신뢰도 점수 제공

**걸음 수 계산 공식**
```
steps = (distance_km * 1000) / 0.7
```

---

## 🌐 외부 API 연동

### OpenAI API
- **Chat Completions** → 걸음수 추천, 경로 추천, 위치 검색
- **모델**: GPT-4 또는 GPT-3.5-turbo
- **응답 시간 목표**: 걸음수 추천 5초 이내, 경로 추천 10초 이내

---

## 📊 데이터 모델

### User Profile
```json
{
  "user_id": "string",
  "gender": "male|female", 
  "age": "integer",
  "height": "integer (cm)",
  "weight": "integer (kg)",
  "timestamp": "datetime"
}
```

### Steps Recommendation
```json
{
  "user_id": "string",
  "recommended_steps": "integer",
  "rationale": "string",
  "difficulty_level": "string",
  "created_at": "datetime"
}
```

### Route Recommendation
```json
{
  "user_id": "string",
  "location": {
    "latitude": "float",
    "longitude": "float"
  },
  "remaining_steps": "integer",
  "routes": [
    {
      "type": "string",
      "distance": "float (km)",
      "duration": "integer (minutes)", 
      "steps": "integer",
      "description": "string"
    }
  ],
  "created_at": "datetime"
}
```

### Geocode Data
```json
{
  "keyword": "string",
  "location_type": "address|building|station|landmark",
  "region": "string",
  "result": {
    "latitude": "float",
    "longitude": "float", 
    "address": "string",
    "confidence": "float (0.0-1.0)"
  },
  "created_at": "datetime"
}
```

---

## 🎯 MVP 개발 우선순위

### Phase 1 (핵심 기능)
1. ✅ 걸음수 추천 API 구현
2. ✅ 도보 경로 추천 API 구현  
3. ⏳ 위치 검색 API 구현
4. ✅ OpenAI API 연동

### Phase 2 (안정화)
1. ⏳ Backend 서버 API 통합 테스트
2. ⏳ 기본 에러 처리 강화
3. ⏳ 로깅 및 모니터링

---

## ⚠️ Claude 전용 가이드라인 (MVP)
- **Plan Mode** → 실행 전 반드시 계획 설명  
- **Environment Variables** → `.env` 파일의 API 키는 직접 다루지 말 것  
- **API Integration** → Backend 서버와의 통신 테스트 시 승인 절차 거치기
- **Error Handling** → 모든 API에 기본적인 fallback 응답 구현
- **Logging** → 요청/응답 로그 필수 기록

---

## 🔒 보안 및 성능 고려사항

### 보안 (기본 수준)
- API 키 환경변수 관리
- Backend 서버 간 기본 인증
- 입력 데이터 검증 및 sanitization

### 성능 (기본 수준)  
- API 응답 시간 목표 준수
- 10개 동시 요청 처리 지원
- 기본적인 에러 처리 및 재시도 로직

### 모니터링
- OpenAI API 사용량 추적
- 응답 시간 및 성공률 모니터링
- 기본 로깅 (요청/응답/에러)

---

## 📌 비고
- 이 문서는 **MVP 단계**용이며, Backend 서버와의 API 연동을 최우선으로 합니다.
- 성능 최적화, 고급 개인화, 상세 경로 안내는 **MVP 이후 확장 버전**에서 구현합니다.
- 현재 구현된 경로 추천 API는 PRD 명세에 맞춰 재구성이 필요합니다.