## Features

* 새 레코드 추가
  * 레코드의 날짜 / 시간을 설정할 수 있습니다
  * 이미지 업로드시 자르기 및 회전이 가능합니다.
    * 이미지 파일은 전처리 후 내부 저장소에 저장되어 갤러리 앱에 노출되지 않습니다.
       
* 레코드 정렬
  * 레코드를 이름 / 생성 날짜 순으로 정렬합니다.
  
* 레코드 삭제
  * 해당 레코드를 삭제합니다.
    
* 레코드 일괄 삭제
  * 체크박스를 이용하여 체크된 레코드를 일괄 삭제 할 수 있습니다.
    
* 이미지 상세 보기
  * 이미지를 전체화면으로 출력합니다. 줌 기능을 이용해 이미지를 확대해서 볼 수 있습니다.
    
* 이미지 다운로드
  * 레코드의 이미지를 기기의 /DCIM 하위폴더에 저장합니다.
    
## Development

### Language
* Kotlin

### Libraries

* AndroidX
  * Core
  * Lifecycle & ViewModel Compose
  * Navigation
  * Room

* Kotlin Libraries
  * Coroutines
 
* Compose
  * Material
  * Navigation

* DataBinding
* [Gilde](https://github.com/bumptech/glide)
* [CropperNoCropper](https://github.com/jayrambhia/CropperNoCropper)
* [subsampling-scale-image-view](https://github.com/davemorrissey/subsampling-scale-image-view)

## Architecture
MVVM Based

## Foldering
```
├── app
│   ├── common
│   ├── database
│   │   ├── RecordDAO
│   │   └── RecordDatabase
│   ├── libs
│   ├── model
│   ├── repository
│   │   ├── RecordRocalSource
│   │   └── RecordRepository
│   ├── ui
│   │   ├── common
│   │   ├── datamgnt
│   │   ├── record
│   │   └── recorddetail
│   └── ServiceLocator.kt
└── gradle
```
<br/>

## Screens
<p align="center">
  <img src="https://github.com/user-attachments/assets/1593df53-0b01-4e10-ba19-693d69be0196" width="30%"/>
  <img src="https://github.com/user-attachments/assets/7f9bcea2-8c2d-45e8-a21d-2462da4003f2" width="30%"/>
  <img src="https://github.com/user-attachments/assets/9b981bf6-06e0-4aca-a7c2-4d43cf4a3b48" width="30%"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/7a6a6c5e-098b-4c0a-8939-238c58ce1739" width="22.5%"/>
  <img src="https://github.com/user-attachments/assets/d6095508-0bed-45c1-84a9-544cf36aae12" width="22.5%"/>
  <img src="https://github.com/user-attachments/assets/b2ff53ce-ae95-4aa9-8848-9fa74d4f15f4" width="22.5%"/>
  <img src="https://github.com/user-attachments/assets/5ca71dfc-7b75-4110-9856-04424d16a22a" width="22.5%"/>
</p>

