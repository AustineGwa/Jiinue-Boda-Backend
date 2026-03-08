package com.otblabs.jiinueboda.dashboard.sales;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class SalesDashboardController {

    private final SalesDashboardService salesDashboardService;

    public SalesDashboardController(SalesDashboardService salesDashboardService) {
        this.salesDashboardService = salesDashboardService;
    }

    @GetMapping("/sales-stats/{branch}/{startDate}/{endDate}")
    ResponseEntity<Object> getSalesStatsForTimeRange(@PathVariable int branch, @PathVariable String startDate, @PathVariable String endDate){
        try{
            return ResponseEntity.ok(salesDashboardService.getSalesStatsForTimeRange(branch,startDate,endDate));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("/leads")
    ResponseEntity<Object> getNewLeads(){
        try{
            return ResponseEntity.ok(salesDashboardService.getAllLeads());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("/quizes")
    ResponseEntity<Object> getMarketingQuizes(){
        try{
            return ResponseEntity.ok(salesDashboardService.getAllQuizes());
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }


}
