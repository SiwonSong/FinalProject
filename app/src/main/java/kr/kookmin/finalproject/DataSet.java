package kr.kookmin.finalproject;


public class DataSet {

    private int id;
    private String title;
    private String time;
    private String location;
    private String memo;


    //DataBaseService에서 데이터를 쉽게 저장하기 위해 생성자를 사용
    public DataSet(int id, String title, String time, String location, String memo) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.location = location;
        this.memo = memo;
    }

    public int getIdSet() {
        return id;
    }
    public String getTimeSet() {
        return time;
    }
    public String getLocationSet() {
        return location;
    }
    public String getTitleSet() {
        return title;
    }
    public String getMemoSet() {
        return memo;
    }

    //모든 값을 초기화
    public void clear() {
        this.id = -1;
        this.title = null;
        this.time = null;
        this.location = null;
        this.memo = null;
    }
}
