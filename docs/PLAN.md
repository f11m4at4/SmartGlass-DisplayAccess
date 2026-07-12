# Navigation Display Plan

## Goal

Meta DAT `DisplayAccess` 샘플을 기반으로, 사용자가 음성으로 목적지를 말하면 OpenAI API와 Google Places API(New)로 목적지를 해석하고, 스마트폰의 위치/방향 센서를 이용해 스마트글래스 화면에 방향 화살표와 저주기 미니맵을 표시하는 내비게이션 앱으로 확장한다.

## Current Baseline

- 현재 앱은 DAT 초기화, 등록 상태 관찰, 기기 목록 표시, 디스플레이 세션 연결, 샘플 콘텐츠 전송까지 구현되어 있다.
- 음성 입력, TTS, OpenAI 호출, Google Places/Static Maps 호출, GPS/센서 기반 방향 계산, 내비게이션 상태 관리 계층은 아직 없다.
- 기존 핵심 진입점은 아래와 같다.
  - `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/MainActivity.kt`
  - `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/wearables/WearablesRepository.kt`
  - `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/wearables/WearablesViewModel.kt`
  - `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/display/DisplayViewModel.kt`
  - `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/ui/AppScaffold.kt`

## Required Outcomes

- STT로 사용자의 음성을 계속 수신한다.
- 인식된 문구를 휴대폰 UI에 표시한다.
- TTS 재생 중에는 STT를 잠시 멈추고 재생 종료 후 재개한다.
- 장소 인식 분석은 앱이 OpenAI API를 직접 호출한다.
- OpenAI가 추출한 장소 질의를 바탕으로 Google Places API(New)에서 목적지를 찾는다.
- 스마트폰 GPS와 자이로/회전벡터 센서를 사용해 목적지 방향 화살표를 계산한다.
- 스마트글래스에는 방향 인디케이터와 함께 저주기 갱신 미니맵을 표시한다.
- 위치 변화가 작으면 Static Map 재생성을 생략한다.
- 지도 렌더링 실패 시에도 화살표 기반 내비게이션은 계속 동작한다.
- `Places API(New)`와 `Google Maps Static API`는 동일 API Key를 사용한다.

## Proposed Architecture

### Package layout

아래 패키지를 신규 추가한다.

```text
app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/
├── ai/
│   ├── OpenAiRepository.kt
│   ├── PlaceIntentParser.kt
│   └── model/
├── audio/
│   ├── SpeechRecognizerCoordinator.kt
│   ├── TextToSpeechCoordinator.kt
│   └── model/
├── location/
│   ├── LocationRepository.kt
│   ├── SensorRepository.kt
│   ├── DirectionCalculator.kt
│   └── model/
├── maps/
│   ├── PlacesRepository.kt
│   ├── StaticMapRepository.kt
│   ├── StaticMapCachePolicy.kt
│   └── model/
├── navigation/
│   ├── NavigationOrchestrator.kt
│   ├── NavigationViewModel.kt
│   ├── NavigationUiState.kt
│   └── model/
└── display/
    └── NavigationDisplayRenderer.kt
```

### Responsibility split

- `audio/`
  - Android `SpeechRecognizer` 기반 연속 리스닝 관리
  - 최종 인식 문장과 중간 인식 문장을 스트림으로 노출
  - TTS 상태와 연동해 pause/resume 제어
- `ai/`
  - STT 문장에서 장소 요청 여부, 검색 질의, 부가 제약어 추출
  - OpenAI 응답을 앱 내부 모델로 정규화
- `maps/`
  - Places API(New) 텍스트 검색
  - Static Maps URL 생성 및 이미지 fetch/caching
  - 지도 재생성 throttle 정책 적용
- `location/`
  - Fused location 또는 Android location provider로 위치 수집
  - 회전벡터/자이로 기반 heading 계산
  - 사용자 heading과 목적지 bearing 차이로 화살표 방향 계산
- `navigation/`
  - 음성 입력 -> LLM 분석 -> 장소 검색 -> 경로 상태 생성 -> 디스플레이 갱신 전체 오케스트레이션
  - 실패 시 fallback 결정
- `display/`
  - DAT Display DSL로 화살표, 거리, 목적지 이름, 미니맵 조합 렌더링

## End-to-End Flow

1. 앱 시작 후 기존 DAT 초기화 및 등록/기기 연결 흐름을 유지한다.
2. 연결 완료 후 `NavigationViewModel`이 STT 리스닝을 시작한다.
3. 사용자가 "근처 스타벅스 매장 알려줘" 같은 문장을 말한다.
4. STT 최종 문장을 UI에 표시한다.
5. 앱이 OpenAI API에 직접 요청하여 장소 질의를 구조화한다.
6. `Places API(New)`로 후보 목적지를 찾고, 가장 적합한 1건을 선택한다.
7. 현재 위치와 목적지 좌표를 기준으로 bearing/거리 계산을 시작한다.
8. 센서 heading과 bearing 차이로 화살표 방향을 산출한다.
9. DAT Display에 화살표, 목적지명, 남은 거리, 미니맵을 보낸다.
10. 위치 변화가 임계값 이상일 때만 Static Map을 다시 생성한다.
11. 지도 생성이 실패하면 기존 지도 또는 지도 없는 화살표 UI로 계속 안내한다.
12. TTS가 안내 문구를 재생할 때는 STT를 잠시 멈추고, 종료되면 자동 재개한다.

## Screen and UI Plan

### Phone UI

- 연결 화면은 기존 `ConnectScreen`을 유지한다.
- 샘플 목록 대신 내비게이션 메인 화면을 추가하거나 기존 `SamplesListScreen`을 대체한다.
- 표시 항목
  - 현재 등록/연결 상태
  - 마지막 STT 중간 문장
  - 마지막 확정 문장
  - LLM 해석 결과
  - 선택된 목적지명 / 주소 / 좌표
  - 현재 위치 / heading / 목적지 bearing / 각도 차
  - 지도 갱신 시각 / 실패 상태
  - STT 리스닝 상태 / TTS 상태

### Glasses UI

- 항상 우선순위는 방향 화살표다.
- 함께 표시할 정보
  - 목적지 이름
  - 남은 거리
  - 좌/우/정면 방향 인디케이터
  - 저해상도 미니맵 이미지
- 지도 이미지 로드 실패 시
  - 화살표 + 텍스트만 표시
  - 사용자에게 재시도 여부만 내부 상태로 관리하고 안내는 유지

## API and Config Plan

### Local properties / manifest / build config

- `local.properties`
  - `openai_api_key`
  - `google_maps_api_key`
  - 기존 `github_token`, `mwdat_application_id`, `mwdat_client_token`
- 같은 Google API Key를 `Places API(New)`와 `Google Maps Static API`에 함께 사용한다.
- 앱 코드에서 직접 API 호출이 필요하므로 `BuildConfig` 또는 안전한 주입 경로로 키를 주입한다.
- 추가 권한 검토
  - `android.permission.ACCESS_FINE_LOCATION`
  - `android.permission.ACCESS_COARSE_LOCATION`
  - 필요 시 `android.permission.RECORD_AUDIO`

### Network clients

- `OkHttp` + `Kotlinx Serialization` 또는 프로젝트 표준 JSON 파서를 사용한다.
- OpenAI API 호출은 structured JSON 응답을 강제하는 형태로 설계한다.
- Places API(New)도 DTO를 별도로 두고 변환 레이어를 둔다.

## Data Models

- `RecognizedSpeech`
  - `partialText`
  - `finalText`
  - `timestamp`
- `ParsedPlaceIntent`
  - `originalUtterance`
  - `placeQuery`
  - `intentType`
  - `confidence`
- `PlaceCandidate`
  - `name`
  - `address`
  - `lat`
  - `lng`
- `NavigationTarget`
  - `place`
  - `selectedAt`
- `NavigationSnapshot`
  - `currentLat`
  - `currentLng`
  - `headingDegrees`
  - `targetBearingDegrees`
  - `distanceMeters`
  - `turnAngleDegrees`
- `MiniMapState`
  - `imageUrl`
  - `lastRenderLocation`
  - `lastUpdatedAt`
  - `isFallbackMode`

## Key Technical Decisions

### 1. STT lifecycle

- 기본은 continuous listening처럼 보이도록 세션을 자동 재시작한다.
- Android `SpeechRecognizer` 특성상 완전한 무중단 스트리밍은 어렵기 때문에, 에러/종료 시 자동 재개 전략을 둔다.
- TTS 재생 직전 STT를 pause 하고 `UtteranceProgressListener` 종료 콜백에서 resume 한다.

### 2. LLM contract

- OpenAI에는 자유형 텍스트가 아니라 JSON 스키마 기반 응답을 요구한다.
- 예시 응답 필드
  - `intent_type`
  - `place_query`
  - `search_area_hint`
  - `needs_clarification`
- 목적지는 앱이 Places API(New)로 최종 확정한다. 좌표를 LLM이 직접 결정하지 않게 한다.

### 3. Navigation calculation

- 목적지 bearing은 위치 좌표로 계산한다.
- 사용자의 현재 heading은 회전벡터 센서 우선, 불가 시 자이로/자기장 fallback을 검토한다.
- `turnAngleDegrees = normalize(targetBearing - heading)` 로 계산한다.
- UI는 각도 범위를 구간화해 `정면`, `좌측 크게`, `우측 조금` 같은 상태를 만든다.

### 4. Mini-map refresh policy

- 다음 조건일 때만 Static Map을 재생성한다.
  - 사용자가 마지막 지도 생성 위치에서 일정 거리 이상 이동
  - heading 변화가 일정 임계값 이상 발생
  - 목적지가 변경됨
  - 일정 시간 TTL 초과
- 제안 기본값
  - 거리 20~30m
  - heading 20~25도
  - TTL 10~15초

### 5. Failure policy

- OpenAI 실패
  - 마지막 음성 문장을 UI에 유지
  - TTS로 재질문
  - 기존 내비게이션 세션은 종료하지 않음
- Places 실패
  - 모호한 질의로 판단하고 재질문
- Location/Sensor 일시 실패
  - 최근 정상값 유지
  - 화살표 갱신만 보수적으로 유지
- Static Maps 실패
  - 화살표/거리 UI만 유지
  - 지도만 fallback

## Implementation Phases

### Phase 1. Foundation

- 의존성 추가
  - 위치 권한
  - 오디오/STT 권한
  - 네트워크 JSON 라이브러리
- `local.properties`/`BuildConfig` 키 주입
- 패키지 구조 생성

### Phase 2. Voice pipeline

- `SpeechRecognizerCoordinator`
- `TextToSpeechCoordinator`
- 휴대폰 UI에 partial/final transcript 출력
- TTS와 STT 상호 배제 제어

### Phase 3. Intent and place search

- `OpenAiRepository`
- `PlaceIntentParser`
- `PlacesRepository`
- 음성 문장 -> 목적지 후보 1건 선택까지 구현

### Phase 4. Location and direction

- `LocationRepository`
- `SensorRepository`
- `DirectionCalculator`
- 실시간 거리/방향 상태 계산

### Phase 5. Glasses rendering

- `NavigationDisplayRenderer`
- 화살표, 거리, 목적지명, 미니맵 레이아웃 구현
- 지도 fallback UI 구현

### Phase 6. Robustness

- 지도 갱신 throttle
- 네트워크 재시도
- 에러 메시지 및 디버그 정보 정리

## Suggested File-Level Changes

- `MainActivity.kt`
  - 마이크/위치 권한 추가
  - 초기화 순서 재점검
- `AppScaffold.kt`
  - 샘플 화면 대신 내비게이션 화면 추가
- `DisplayViewModel.kt`
  - 샘플 콘텐츠 전송 로직을 `NavigationDisplayRenderer` 중심으로 축소/대체
- 신규
  - `navigation/NavigationViewModel.kt`
  - `audio/*`
  - `ai/*`
  - `maps/*`
  - `location/*`

## Testing Plan

### Unit tests

- 문장 -> 장소 질의 파싱 결과 매핑
- bearing/turn angle 계산
- 지도 재생성 throttle 정책
- Places/OpenAI 응답 DTO 파싱

### Integration tests

- STT 종료 후 자동 재시작
- TTS 재생 시 STT pause/resume
- 목적지 선택 후 내비게이션 상태 생성
- 지도 실패 시 화살표 UI 유지

### Device tests

- 실기기에서 DAT 등록/연결
- 권한 거부/재허용 플로우
- 실외 위치 이동 시 방향 추적
- 헤딩 변화에 따른 화살표 반응 확인
- 저주기 미니맵 갱신 빈도 확인

## Open Questions

- STT 엔진은 Android 기본 `SpeechRecognizer`로 시작할지, 장기적으로 서버 기반 STT를 둘지 결정이 필요하다.
- TTS 안내 문구를 얼마나 자주 재생할지 정책이 필요하다.
- Places 후보가 여러 개일 때 사용자 확인 UI가 필요한지 결정이 필요하다.
- 스마트글래스 화면의 실제 해상도/레이아웃 한계에 맞춘 미니맵 크기 조정이 필요하다.

## Execution Order Recommendation

1. 권한/설정/의존성 정리
2. STT/TTS 파이프라인 구축
3. OpenAI + Places 검색 연결
4. 위치/센서 기반 방향 계산
5. DAT 디스플레이 렌더러 연결
6. 지도 throttle 및 fallback 보강
7. 실기기 검증
