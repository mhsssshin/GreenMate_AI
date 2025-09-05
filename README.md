# GreenMate Route Recommendation API

GreenMate의 경로 추천 AI 에이전트 시스템입니다. OpenAI GPT를 활용하여 사용자의 출발지와 목적지를 기반으로 3가지 유형의 도보 경로를 추천합니다.

## 🛠️ 기술 스택

- **Spring Boot 3.2+** - Java 17 기반
- **Spring Web** - RESTful API
- **Spring Boot Validation** - 요청 데이터 검증
- **SpringDoc OpenAPI 3** - API 문서 자동 생성
- **Lombok** - 보일러플레이트 코드 제거
- **OpenAI API** - GPT 기반 경로 추천

## 🚀 실행 방법

### 1. 환경 설정

```bash
# OpenAI API 키 설정
export OPENAI_API_KEY=your-openai-api-key-here
```

### 2. 애플리케이션 실행

```bash
# Gradle로 실행
./gradlew bootRun

# 또는 빌드 후 실행
./gradlew build
java -jar build/libs/route-recommendation-0.0.1-SNAPSHOT.jar
```

### 3. API 문서 확인

브라우저에서 `http://localhost:8080/swagger-ui.html` 접속

## 📡 API 사용법

### POST `/api/v1/routes/recommend`

경로 추천 요청

**Request Body:**
```json
{
  "origin": {"lat": 37.5665, "lng": 126.9780},
  "destination": {"lat": 37.5651, "lng": 126.9895},
  "walkRecommendCount": 3000,
  "timeLimitMinutes": 60
}
```

**Response:**
```json
{
  "success": true,
  "message": "경로 추천이 성공적으로 완료되었습니다.",
  "data": {
    "requestId": "req_20250904_001",
    "timestamp": "2025-09-04T12:00:00",
    "routes": [
      {
        "type": "short",
        "distanceKm": 1.2,
        "durationMinutes": 15,
        "steps": 1680,
        "description": "빠른 이동 경로",
        "routeSteps": [...]
      }
    ]
  }
}
```

## 🧪 테스트

```bash
# 단위 테스트 실행
./gradlew test
```

## 📂 프로젝트 구조

```
src/
├── main/
│   ├── java/com/greenmate/routerecommendation/
│   │   ├── controller/          # REST 컨트롤러
│   │   ├── service/             # 비즈니스 로직
│   │   ├── dto/                 # 데이터 전송 객체
│   │   └── RouteRecommendationApplication.java
│   └── resources/
│       ├── application.yml      # 설정 파일
│       └── .env.example        # 환경변수 예시
└── test/
    └── java/                   # 테스트 코드
```

## 🔧 설정

`application.yml`에서 다음 항목들을 설정할 수 있습니다:

- `openai.api-key`: OpenAI API 키
- `openai.model`: 사용할 GPT 모델 (기본: gpt-4)
- `openai.max-tokens`: 최대 토큰 수 (기본: 2000)
- `openai.temperature`: 응답 창의성 (기본: 0.3)

## 📄 라이센스

MIT License