<div align="center">

# 시각장애인을 위한 어플

![ezgif com-gif-maker](https://user-images.githubusercontent.com/73810942/170853849-e17898a0-97d5-43c2-94d8-d3e9707c12d4.gif)

<yolov4-tiny 모델을 이용해 물건을 인식하고 화면을 터치하면 음성으로 물건을 알려줌>  
<화면을 꾹 눌러 음성으로 원하는 물건을 말하면 전방에 그 물건이 있을 시 음성으로 알려줌>
  </div>


* 앞으로 추가할 기능
  ~~1. 원하는 물건 음성으로 받아 찾기 (완료!)  
  -> 화면을 길게 터치하고 음성으로 원하는 물건을 말하면 그 물건을 인식할 때 진동 혹은 알림음 출력~~  
    
    ![11](https://user-images.githubusercontent.com/73810942/185542244-615558cd-5ba8-44a1-8749-6ba54e00e205.PNG)

2. 어플 UI 수정하기 좀 멋있게 + 접근성 좋게 만들기
3. 많은 물건을 찾을 수 있도록 다양한 물건 학습시키기

----

~~지도 위치 좌표얻는건 구현함 -> 얻은 좌표로 어떻게 위치를 알려야 할까?~~  
~~1. 구글맵에 검색하게 해서 파싱으로 주변 건물 알리기~~  
~~2. 혹시 다른 방법있나 고민~~    
~~-> 아이디어 1: geofence 사용~~     
~~-> 아이디어 2: 구글 맵 백그라운드에서 검색 후 파싱~~
### 물건 찾는 기능에 집중 하기로 함

# Yolov4-tiny NCNN Implementation

This repo provides C++ implementation of [yolov4-tiny model](https://github.com/AlexeyAB/darknet) using
Tencent's NCNN framework.

## APK:https://github.com/dog-qiuqiu/Android_NCNN_yolov4-tiny/blob/master/app/release/yolov4-tiny-ncnn.apk

# Credits
* https://github.com/tencent/ncnn
* https://github.com/WZTENG/YOLOv5_NCNN
