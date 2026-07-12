# Task 02 - Voice Pipeline Prototype

## Goal

휴대폰에서 STT와 TTS를 제어하고, 인식 중간 문장과 최종 문장을 UI에서 확인할 수 있게 만든다.

## Scope

- `SpeechRecognizer` 기반 STT coordinator 추가
- `TextToSpeech` coordinator 추가
- TTS 재생 중 STT pause, 종료 후 resume 제어
- 휴대폰 Compose UI에 partial/final transcript 표시
- 오류 발생 시 자동 재시작 전략 추가

## Suggested Files

- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/audio/SpeechRecognizerCoordinator.kt`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/audio/TextToSpeechCoordinator.kt`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/navigation/NavigationViewModel.kt`
- `app/src/main/java/com/meta/wearable/dat/externalsampleapps/displayaccess/ui/AppScaffold.kt`
- 신규 내비게이션 화면 UI 파일

## Deliverable

사용자가 음성을 말하면 휴대폰 화면에 인식 결과가 표시되고, TTS 재생 중 리스닝이 잠시 멈췄다가 자동 재개된다.

## Done When

- partial transcript와 final transcript가 상태로 노출된다.
- TTS 재생 전후로 STT 상태가 예상대로 바뀐다.
- STT 오류 후 자동 재시작이 동작한다.
- 아직 OpenAI/Places가 없어도 음성 파이프라인 단독 검증이 가능하다.

## Dependencies

- Task 01

## Checklist

체크리스트: [task-02-voice-pipeline-prototype-checklist.md](../checklists/task-02-voice-pipeline-prototype-checklist.md)
