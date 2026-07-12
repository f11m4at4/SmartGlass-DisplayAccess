# Task 05 - Glasses Navigation Integration

## Goal

휴대폰에서 계산한 내비게이션 상태를 DAT Display에 보내 글래스 화면에 방향 화살표, 목적지 정보, 미니맵을 표시한다.

## Scope

- `DisplayViewModel`의 샘플 콘텐츠 중심 구조를 내비게이션 렌더링 중심으로 정리
- `NavigationDisplayRenderer` 추가
- 화살표, 거리, 목적지명, 미니맵 조합 렌더링
- 지도 실패 fallback UI 추가
- 연결 후 자동으로 내비게이션 상태를 전송하는 흐름 연결

## Suggested Files

- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/display/DisplayViewModel.kt`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/display/NavigationDisplayRenderer.kt`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/navigation/NavigationViewModel.kt`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/ui/AppScaffold.kt`

## Deliverable

글래스 연결 상태에서 목적지가 정해지면 글래스 화면에 내비게이션 UI가 표시된다.

## Done When

- 세션과 Display가 준비되면 최신 내비게이션 상태를 렌더링한다.
- 지도 이미지가 없어도 화살표/거리 UI는 유지된다.
- 기존 샘플 화면보다 내비게이션 흐름이 우선한다.
- 휴대폰 UI와 글래스 UI의 상태가 같은 목적지를 가리킨다.

## Dependencies

- Task 01
- Task 02
- Task 03
- Task 04

## Checklist

체크리스트: [task-05-glasses-navigation-integration-checklist.md](../checklists/task-05-glasses-navigation-integration-checklist.md)
