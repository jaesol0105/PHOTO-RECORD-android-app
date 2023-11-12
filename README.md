# commit 내역

## 2023-06-14
#### ● 리소스 네이밍
&ensp; IDs ㅡ WHAT_WHERE_DESCRIPTION<br>
&ensp; string.xml ㅡ WHERE_DESCRIPTION

## 2023-06-14
#### ● item_record.xml ㅡ UI 변경

#### ● recyclerView item longclick ㅡ fab 비활성화, appBar에 선택 개수 표시

#### ● datepicker/timepicker ㅡ 날짜/시간 텍스트로 변환하여 출력, UI 변경

#### ● 모든 dialog ㅡ bottomSheetDialog로 변경

#### ● photo dialog ㅡ 두 손가락으로 zoom-in 기능
&ensp; implementation 'com.davemorrissey.labs:subsampling-scale-image-view:3.10.0'

## 2023-06-04
#### ● recycleView ㅡ 다중삭제 기능
&ensp; recycleView의 item을 길게 누를경우(<b>LongClick</b>) 체크박스가 활성화되며, 우측 상단 menu가 삭제 아이콘으로 변경됨

## 2023-05-25
#### ● 레코드 생성 FAB
&ensp; menu를 통해 Record 생성 -> fab을 통해 Record 생성으로 변경

#### ● 레코드 정렬 기능
&ensp; menu를 통해 정렬 기준을 설정하는 dialog 출력, 정렬 기준은 <b>sharedPreference</b>로 저장

#### ● 날짜 설정 기능
&ensp; DateTimePickerFragment ㅡ Record의 날짜정보를 변경할 수 있도록 하는 custom dialog를 생성<br>
&ensp; spinner 형태의 <b>datePicker와 timePicker를 결합</b> (bottomSheetDialog)

#### ● 일괄 삭제 기능
&ensp; 좌측 상단 Navigation Menu 데이터 관리 ㅡ record 일괄 삭제
