package com.otblabs.jiinueboda.dashboard.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SalesSurvey {
    private int groupId;
    private int numberOfMembers;
    private boolean isRegisteredWithSacco;
    private String workingHours;
    private boolean hasRegularMeetings;
    private String meetingDate;
    private String meetingTime;
    private String createdBy;
    private String createdAt;

}
