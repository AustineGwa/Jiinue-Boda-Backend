package com.otblabs.jiinueboda.jifuel.models;
import com.otblabs.jiinueboda.jifuel.petrolstations.PetrolStation;
import com.otblabs.jiinueboda.users.models.SystemUser;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FuelLoan {
    private int id;
    private int userID;
    private int appID;
    private String loanID;
    private int loanPrincipal;
    private double interestPercentage;
    private int fuelLoanPurchased;
    private String createdAt;
    private String disbursedAt;
    private String payedAt;
    private String status;
    private String mpesaDisburseConversationID;
    private PetrolStation petrolStation;
    private int balance;
    private SystemUser user;
}
