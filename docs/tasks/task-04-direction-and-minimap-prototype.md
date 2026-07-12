# Task 04 - Direction and Mini-map Prototype

## Goal

현재 위치와 센서 heading을 기반으로 목적지 방향, 거리, 화살표 상태를 계산하고 저주기 미니맵 갱신 정책을 검증한다.

## Scope

- 위치 수집 repository 추가
- 회전벡터 또는 대체 센서 heading 계산 추가
- bearing, turn angle, 거리 계산기 구현
- Static Maps URL 생성과 갱신 throttle 정책 추가
- 휴대폰 UI에서 방향 상태와 지도 갱신 상태 표시

## Suggested Files

- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/location/LocationRepository.kt`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/location/SensorRepository.kt`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/location/DirectionCalculator.kt`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/maps/StaticMapRepository.kt`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/maps/StaticMapCachePolicy.kt`

## Deliverable

휴대폰 화면에서 현재 위치 대비 목적지 방향과 거리, 지도 갱신 여부를 확인할 수 있다.

## Done When

- 목적지 좌표와 현재 위치로 bearing/거리 계산이 된다.
- 센서 heading을 받아 turn angle이 계산된다.
- 작은 위치 변화에서는 지도 URL 재생성이 생략된다.
- 지도 실패 시에도 방향 상태는 유지된다.

## Dependencies

- Task 01
- Task 03

## Checklist

체크리스트: [task-04-direction-and-minimap-prototype-checklist.md](../checklists/task-04-direction-and-minimap-prototype-checklist.md)
