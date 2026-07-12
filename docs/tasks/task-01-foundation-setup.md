# Task 01 - Foundation Setup

## Goal

음성 내비게이션 프로토타입에 필요한 권한, API 키 주입, 기본 패키지 구조, 공용 모델 뼈대를 추가한다.

## Scope

- `MainActivity.kt` 권한 확장
- `AndroidManifest.xml` 권한 및 DAT 설정 검토
- `app/build.gradle.kts` 의존성/`BuildConfig` 주입 준비
- `local.properties`에서 OpenAI/Google API 키 읽기 구조 추가
- `audio`, `ai`, `maps`, `location`, `navigation` 패키지와 기본 모델 파일 생성

## Suggested Files

- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/MainActivity.kt`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/audio/*`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/ai/*`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/maps/*`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/location/*`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/navigation/*`

## Deliverable

앱이 빌드 가능하고, 이후 태스크가 붙을 최소 골격이 준비된 상태.

## Done When

- 위치/마이크 권한이 선언되고 런타임 요청 위치가 정리된다.
- `BuildConfig` 또는 동등한 방법으로 `openai_api_key`, `google_maps_api_key` 접근 경로가 생긴다.
- 신규 패키지와 기본 데이터 모델이 생성된다.
- 기존 DAT 연결 흐름이 깨지지 않는다.

## Dependencies

- 없음

## Checklist

체크리스트: [task-01-foundation-setup-checklist.md](../checklists/task-01-foundation-setup-checklist.md)
