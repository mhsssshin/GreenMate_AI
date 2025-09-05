# PRD (Product Requirements Document) - AI Agent 서버 MVP

## 1. 목적 (Objective)

**해결하고자 하는 문제:**
- 사용자의 개인 정보(성별, 나이, 키, 몸무게)를 고려한 맞춤형 걸음 수 추천
- 사용자의 현재 위치와 잔여 걸음 수를 고려한 맞춤형 도보 경로 추천

**AI Agent 서버를 만드는 이유:**
- 개인별 신체 특성을 반영한 기본적인 걸음 수 목표 제공
- 사용자 위치 기반 3가지 도보 코스 옵션 생성
- AI 기반 추천 시스템의 기본 기능 검증

**완성 시 기대되는 결과:**
- 개인 특성 기반 적절한 걸음 수 목표 제공
- 위치 기반 3가지 도보 코스 추천 기능 동작
- Backend 서버와의 기본적인 API 통신 완성

---

## 2. 범위 (Scope)

**포함되는 기능 (MVP 핵심):**
1. AI 기반 개인 맞춤형 일일 걸음 수 추천
- Backend 서버로부터 사용자 프로필 데이터 수신 (성별, 나이, 키, 몸무게)
- OpenAI API를 활용한 일일 걸음 수 계산하여 Backend 서버로 데이터 전달
2. AI 기반 개인 맞춤형 산책 코스 추천
- Backend 서버로부터 사용자 위치정보(위도, 경도)와 잔여 걸음 수 데이터 수신
- OpenAI API를 활용한 3가지 경로 옵션 제공 (Less Walk / Recommended / More Walk) 기반 기본 산책 코스 생성
- 코스는 상세한 단계별 경로 안내
- 각 경로는 거리, 시간, 걸음 수 계산하여 경로와 같이거리, 시간, 걸음 수 계산하여 경로와 같이 생성
- Backend 서버에 결과 데이터 전달
3. AI 기반 위도와 경도 계산
- Backend 서버로 부터 주소나 건물이나 지하철역 이름 등의 키워드를 전달 받아 위도와 경도를 계산하여 전달

**포함되지 않는 기능 (MVP 이후 고려):**
- 
- 지역 특성 및 랜드마크 정보
- 안전성 분석 및 하이라이트 기능
- 경로 난이도 분석
- 지역 정보 데이터베이스
- 복잡한 개인화 알고리즘
- 캐싱 및 성능 최적화
- 에러 처리 및 fallback 로직

---

## 3. 시스템 아키텍처 (System Architecture)

**시스템 구성요소:**

**AI Agent 서버 역할:**
- Backend 서버와 LLM서버와의 인터페이스 중계 역할
- 외부 AI API (OpenAI/Gemini) 연동
- 인터페이스는 JSON으로만 통신

**데이터 플로우:**
- AI 기반 개인 맞춤형 일일 걸음 수 추천
    1. Backend 서버 → AI Agent: 사용자 프로필 데이터 전송
    2. AI Agent → AI API: 개인 맞춤형 걸음 수 계산 요청
    AI Agent → Backend 서버: 추천 걸음 수 응답
- AI 기반 개인 맞춤형 산책 코스 추천
    1. Backend 서버 → AI Agent: 위치정보 + 잔여 걸음 수 + 사용자 프로필 전송
    2. AI Agent → AI API: 위치 기반 상세 산책 코스 생성 요청 (3가지 옵션)
    3. AI Agent → Backend 서버: 3가지 상세 도보 코스 JSON 응답
- AI 기반 위도와 경도 계산
    1. Backend 서버 -> AI Agent: 주소나 건물이나 지하철역 이름 등의 키워드를 전송
    2. AI Agent -> AI API: 전달받은 데이터 기반 위도와 경도 생성 요청
    3. AI Agent -> Backend 서버: 위도와 경도 데이터를 JSON 응답

---

## 4. API 명세 (API Specifications)


### 4.1 걸음 수 추천 API

**Endpoint:** `POST /api/v1/recommend/steps`

**요청 데이터:**
```json
{
  "user_id": "string",
  "gender": "male|female",
  "age": "integer",
  "height": "integer (cm)",
  "weight": "integer (kg)"
}
```

**응답 데이터:**
```json
{
  "status": "success|error",
  "data": {
    "recommended_steps": "integer"
  }
}
```

### 4.2 도보 코스 추천 API

**Endpoint:** `POST /api/v1/recommend/routes`

**요청 데이터:**
```json
{
  "user_id": "string",
  "latitude": "float",
  "longitude": "float",
  "remaining_steps": "integer",
  "user_profile": {
    "gender": "male|female",
    "age": "integer",
    "height": "integer",
    "weight": "integer"
  }
}
```

**응답 데이터 (MVP 버전):**
```json
{
  "status": "success|error",
  "data": {
    "routes": [
      {
        "type": "Less Walk",
        "distance": "float (km)",
        "duration": "integer (minutes)",
        "steps": "integer",
        "description": "string"
      },
      {
        "type": "Recommended",
        "distance": "float (km)",
        "duration": "integer (minutes)",
        "steps": "integer",
        "description": "string"
      },
      {
        "type": "More Walk",
        "distance": "float (km)",
        "duration": "integer (minutes)",
        "steps": "integer",
        "description": "string"
      }
    ]
  },
  "message": "string"
}
```

### 4.3 위도/경도 계산 API ###

**Endpoint:** `POST /api/v1/geocode`

**요청 데이터:**
```json
{
  "keyword": "string",
  "location_type": "address|building|station|landmark",
  "region": "string (optional, default: 대한민국)"
}
```

**응답 데이터:**
```json
{
  "status": "success|error",
  "data": {
    "latitude": "float",
    "longitude": "float",
    "address": "string",
    "confidence": "float (0.0 - 1.0)"
  },
  "message": "string"
}
```

---

## 5. 요구사항 (Requirements)

### 기능 요구사항 (Functional Requirements - MVP)

**걸음 수 추천 기능:**
- [ ] 사용자 프로필(성별, 나이, 키, 몸무게) 기반 기본적인 일일 걸음 수 계산
- [ ] OpenAI API를 활용한 개인별 적정 운동량 분석
- [ ] 추천 걸음 수와 간단한 설명 제공

**도보 코스 추천 기능:**
- [ ] 사용자 현재 위치 기반 기본적인 코스 생성
- [ ] 잔여 걸음 수를 고려한 3가지 경로 길이 계산 (Less/Recommended/More Walk)
- [ ] 각 경로별 기본 정보 계산 (거리, 시간, 걸음 수)
- [ ] 간단한 경로 설명 생성

**API 연동 기능:**
- [ ] OpenAI API 연동 및 기본 프롬프트 구현
- [ ] Backend 서버와의 기본 RESTful API 통신
- [ ] JSON 형식 데이터 파싱 및 생성

### 비기능 요구사항 (Non-Functional Requirements - MVP)

**성능 (기본 수준):**
- API 응답 시간: 걸음 수 추천 5초 이내, 경로 추천 10초 이내
- 기본적인 동시 요청 처리: 10개 동시 요청 지원

**안정성 (기본 수준):**
- 기본적인 에러 처리 및 로깅
- AI API 호출 실패 시 기본 응답 제공

**보안 (기본 수준):**
- API 키 기본 보안 관리
- Backend 서버와의 기본 인증

---

## 6. 기술 스택 (Technology Stack - MVP)

**개발 프레임워크:**
- Backend: SpringBoot
- 데이터베이스: 없음 (상태 비저장)

**AI API:**
- OpenAI API (GPT-4 또는 GPT-3.5-turbo)

**인프라:**
- nginx기반 web/was, port는 9000
- 기본 환경 변수 관리

**개발 도구:**
- 버전 관리: Git
- 테스팅: 기본 단위 테스트

---

## 7. 데이터 모델 (Data Models)

**User Profile:**
```json
{
  "user_id": "string",
  "gender": "male|female",
  "age": "integer",
  "height": "integer",
  "weight": "integer",
  "timestamp": "datetime"
}
```

**Steps Recommendation:**
```json
{
  "user_id": "string",
  "recommended_steps": "integer",
  "rationale": "string",
  "difficulty_level": "string",
  "created_at": "datetime"
}
```

**Route Recommendation (MVP):**
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
      "distance": "float",
      "duration": "integer",
      "steps": "integer",
      "description": "string"
    }
  ],
  "created_at": "datetime"
}
```

**Geocode Request/Response:**
```json
{
  "keyword": "string",
  "location_type": "address|building|station|landmark",
  "region": "string",
  "result": {
    "latitude": "float",
    "longitude": "float",
    "address": "string",
    "confidence": "float"
  },
  "created_at": "datetime"
}
```

---

## 8. 성공 기준 (Success Criteria - MVP)

**기능 충족도:**
- 정의된 MVP API가 명세대로 정상 동작
- 걸음 수 추천 기능 기본 동작
- 3가지 도보 코스 생성 기능 기본 동작

**성능 기준:**
- API 응답 시간 목표 달성
- 기본적인 동시 요청 처리

**품질 기준:**
- 기본 기능 테스트 통과
- Backend 서버와 정상 통신

---

## 9. MVP 개발 우선순위

**Phase 1 (핵심 기능):**
1. 걸음 수 추천 API 구현
2. 기본 도보 코스 추천 API 구현
3. 위도 경도 계산 API 구현 
4. OpenAI API 연동

**Phase 2 (안정화):**
1. Backend 서버와의 API 통합 테스트
2. 기본 에러 처리
3. 간단한 로깅

**MVP 이후 개선사항:**
- 상세 경로 안내 기능
- 지역 특성 분석
- 성능 최적화 및 캐싱
- 고급 개인화 알고리즘

---

## 10. 리스크 및 가정 (Risks & Assumptions - MVP)

**주요 리스크:**
- OpenAI API 서비스 장애 또는 비용 증가
- AI 생성 경로의 기본적인 정확성
- Backend 서버와의 통신 장애

**현재 가정:**
- Backend 서버가 안정적으로 사용자 데이터를 제공한다고 가정
- OpenAI API가 지속적으로 서비스된다고 가정
- 사용자 프로필 데이터가 정확하다고 가정

**완화 방안:**
- 기본적인 에러 처리 구현
- API 사용량 모니터링
- 간단한 fallback 응답 제공