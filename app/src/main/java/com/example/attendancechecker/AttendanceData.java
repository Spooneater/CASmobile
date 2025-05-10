package com.example.attendancechecker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class AttendanceData {
    LessonsData lessonsData;
    UserData userData;
    //[0] id студента.[2]-адрес устройства [1]-ФИО [3]-"+","-","na", [4] - id_занятий
    List<Object[]> attendanceData;

    public AttendanceData(LessonsData lessonData,UserData userData){
        this.lessonsData = lessonData;
        this.userData = userData;
        this.attendanceData = new ArrayList<Object[]>();
        boolean notAdded;
        //Формируем
        for (Object[] oneStudAtt: userData.studentsData) {
            notAdded = true;
            for (Object[] oneLessonData: lessonData.lessonsData){
                if (((int)oneLessonData[1] ==(int) oneStudAtt[userData.GROUP_ID]) &&
                        (((int)oneLessonData[1] == (int) oneStudAtt[userData.SUB_GROUP_ID]) || (!(boolean) oneLessonData[4]))) {
                    if (notAdded) {
                        attendanceData.add(new Object[]{
                                oneStudAtt[userData.STUDENT_ID],
                                oneStudAtt[userData.FIO],
                                oneStudAtt[userData.ADDRESS],
                                "na",
                                new ArrayList<Integer>()
                        });

                        ArrayList<Integer> thing = (ArrayList<Integer>) attendanceData.get(attendanceData.size()-1)[4];
                        thing.add((Integer) oneLessonData[0]);
                        notAdded = false;
                    }
                    else{
                        ArrayList<Integer> thing = (ArrayList<Integer>) attendanceData.get(attendanceData.size()-1)[4];
                        thing.add((Integer) oneLessonData[0]);
                    }
                }
            }
        }

    }

    public boolean matchAddressesToStudents(HashSet<String> addresses){
        for (Object[] oneStudent: attendanceData){
            if ((Objects.equals( userData.role, "s"))&&(userData.self_id == (int)oneStudent[0])){
                oneStudent[3] = "+";
                continue;
            }
            for (String address: addresses){

                if (oneStudent[2].equals(address)) {
                    oneStudent[3] = "+";
                    break;
                }

            }
            if (oneStudent[3]=="na")
                oneStudent[3]="-";
        }
        //If successful
        if (true){
            return true;
        }
        return false;
    }
    //TODO Запросы к API
    public boolean sendData(){
        return  true;
    }
}
