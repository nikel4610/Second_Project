<div align="center">

# [시각장애인을 위한 어플](https://righteous-kileskus-db8.notion.site/08817662278542189d87382ec136fec5?p=8370947e434741c5b357d9661a11e0ae&pm=c)

![캡처 PNG](https://user-images.githubusercontent.com/73810942/204089349-fc30336e-bb60-4c52-af23-d7136873fe54.png)
![KakaoTalk_20221121_211135654](https://user-images.githubusercontent.com/73810942/204089699-ad7a4ba4-0c0d-4492-ac9e-513a1fad45a4.jpg)

<yolov4-tiny 모델을 이용해 물건을 인식하고 화면을 터치하면 음성으로 물건을 알려줌>  
<화면을 꾹 눌러 음성으로 원하는 물건을 말하면 전방에 그 물건이 있을 시 음성으로 알려줌>
  </div>


* 앞으로 추가할 기능  
~~1. 원하는 물건 음성으로 받아 찾기 (완료!)  
  -> 화면을 길게 터치하고 음성으로 원하는 물건을 말하면 그 물건을 인식할 때 진동 혹은 알림음 출력~~  
    
    ![제목 없는 다이어그램 drawio](https://user-images.githubusercontent.com/73810942/204089360-a7f24486-3959-42f2-9add-a18af60faa58.png)

~~2. 어플 UI 수정하기 좀 멋있게 + 접근성 좋게 만들기 -> 완료!~~  
  3. 많은 물건을 찾을 수 있도록 다양한 물건 학습시키기 -> 진행중 (오류나서 원인 파악 중)    
  4. 바코드 인식 후 제품 알림 -> 진행중

----

~~지도 위치 좌표얻는건 구현함 -> 얻은 좌표로 어떻게 위치를 알려야 할까?~~  
~~1. 구글맵에 검색하게 해서 파싱으로 주변 건물 알리기~~  
~~2. 혹시 다른 방법있나 고민~~    
~~-> 아이디어 1: geofence 사용~~     
~~-> 아이디어 2: 구글 맵 백그라운드에서 검색 후 파싱~~
### 물건 찾는 기능에 집중 하기로 함  
  
ToDos  
~~1. 어플 디자인 찾아보고 계속 알려주기~~  해결  
~~2. yolo box 오른쪽 왼쪽 위치 알려주기~~ 해결  
3. 바코드 찍어서 제품 알려주기

# Yolov4-tiny NCNN Implementation

This repo provides C++ implementation of [yolov4-tiny model](https://github.com/AlexeyAB/darknet) using
Tencent's NCNN framework.

## APK:https://github.com/dog-qiuqiu/Android_NCNN_yolov4-tiny/blob/master/app/release/yolov4-tiny-ncnn.apk

# Credits
* https://github.com/tencent/ncnn
* https://github.com/WZTENG/YOLOv5_NCNN
