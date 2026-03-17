package com.otblabs.jiinueboda.marketing.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MarketingQuestionaire {
    private int groupId;
    private int numberOfStages;
    private int numberOfMembers;
    private boolean registeredWithSacco;
    private String workingHours;
    private boolean hasRegularMeetings;
    private String meetingDate;
    private String meetingTime;
}
