# Task 03 - Place Intent and Search Prototype

## Goal

STT 최종 문장을 OpenAI API로 분석하고, Google Places API(New)로 목적지 후보 1건을 선택한다.

## Scope

- OpenAI 요청/응답 DTO 및 repository 추가
- 장소 질의 구조화 모델 정의
- Places API(New) 텍스트 검색 repository 추가
- 휴대폰 UI에 해석 결과와 선택된 목적지 표시
- 실패 시 재질문 또는 오류 상태 표시

## Suggested Files

- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/ai/OpenAiRepository.kt`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/ai/PlaceIntentParser.kt`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/maps/PlacesRepository.kt`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/navigation/NavigationOrchestrator.kt`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/navigation/NavigationUiState.kt`

## Deliverable

음성 문장으로부터 목적지 이름, 주소, 좌표가 UI에 표시되는 상태.

## Done When

- final transcript를 입력으로 OpenAI 호출이 가능하다.
- OpenAI 응답에서 `placeQuery`를 추출한다.
- Places 검색으로 1개 목적지를 선택해 상태에 저장한다.
- OpenAI 실패와 Places 실패가 구분되어 UI에 노출된다.

## Dependencies

- Task 01
- Task 02

## Checklist

체크리스트: [task-03-place-intent-and-search-prototype-checklist.md](../checklists/task-03-place-intent-and-search-prototype-checklist.md)
