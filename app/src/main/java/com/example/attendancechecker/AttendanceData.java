package com.example.attendancechecker;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AttendanceData {
    LessonsData lessonData;
    UserData userData;
    //[0] id студента.[2]-адрес устройства [1]-ФИО [3]-"+","-","na"
    List<Object[]> attendanceData;

    List<String[]> parsedForAttendanceTable;
    public AttendanceData(LessonsData lessonData,UserData userData){
        this.lessonData = lessonData;
        this.userData = userData;
        this.attendanceData = new ArrayList<Object[]>();
        //Формируем
        for (Object[] oneStudAtt: userData.studentsData) {
            if ((lessonData.group_id == (int) oneStudAtt[userData.GROUP_ID]) &&
                    ((lessonData.subgroup_id == (int) oneStudAtt[userData.SUB_GROUP_ID]) || (lessonData.subgroup_id == -1))) {
                attendanceData.add(new Object[]{oneStudAtt[userData.STUDENT_ID],
                        oneStudAtt[userData.FIO],
                        oneStudAtt[userData.ADDRESS], "na"});
            }
        }
        this.parsedForAttendanceTable = new ArrayList<>();

    }
    public boolean matchAddressesToStudents(HashSet<String> addresses){
        for (String address: addresses){
            for (Object[] oneStudent: attendanceData){
                if ((oneStudent[2]!="-")&&( oneStudent[2].equals(address))){
                    oneStudent[3] = "+";
                }
                if (oneStudent[3]=="na")
                    oneStudent[3]="-";
            }
        }
        //If successful
        if (true){
            return true;
        }
        return false;
    }

}
