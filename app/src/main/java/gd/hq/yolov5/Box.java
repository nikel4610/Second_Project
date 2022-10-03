package gd.hq.yolov5;

import android.graphics.Color;
import android.graphics.RectF;

import java.util.Random;

public class Box {
    public float x0,y0,x1,y1;
    private int label;
    private float score;
    private static String[] labels={"사람", "자전거", "자동차", "오토바이", "비행기", "버스", "기차", "트럭", "보트", "신호등",
            "소화전", "멈춤표지판", "주차권자동판매기", "벤치", "새", "고양이", "강아지", "말", "양", "소",
            "코끼리", "곰", "얼룩말", "기린", "가방", "우산", "손가방", "넥타이", "서류가방", "프리스비",
            "스키", "스노우보드", "스포츠공", "연", "야구방망이", "야구장갑", "스케이트보드", "서핑보드",
            "테니스라켓", "병", "와인잔", "컵", "포크", "나이프", "숟가락", "그릇", "바나나", "사과",
            "샌드위치", "오렌지", "브로콜리", "당근", "핫도그", "피자", "도넛", "케이크", "의자", "소파",
            "식물", "침대", "식탁", "변기", "티비", "노트북", "마우스", "리모컨", "키보드", "전화기",
            "전자레인지", "오븐", "토스터", "싱크대", "냉장고", "책", "시계", "화분", "가위", "곰인형",
            "헤어드라이기", "칫솔", "지팡이"};
    public Box(float x0,float y0, float x1, float y1, int label, float score){
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.label = label;
        this.score = score;
    }

    public RectF getRect(){
        return new RectF(x0,y0,x1,y1);
    }

    public String getLabel(){
        return labels[label];
    }

    public float getScore(){
        return score;
    }

    public int getColor(){
        Random random = new Random(label);
        return Color.argb(255,random.nextInt(256),random.nextInt(256),random.nextInt(256));
    }

    public char[] getConfidence() {
        return String.format("").toCharArray();
    }

}
