package com.otblabs.jiinueboda.dashboard.collection;

import com.otblabs.jiinueboda.dashboard.models.DailyCollection;
import com.otblabs.jiinueboda.dashboard.models.DashboardData;
import com.otblabs.jiinueboda.dashboard.models.MainDashboard;
import com.otblabs.jiinueboda.dashboard.models.MonthlyTotals;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
public class CollectionDashboardController {

    private final CollectionDashboardService collectionDashboardService;

    public CollectionDashboardController(CollectionDashboardService collectionDashboardService) {
        this.collectionDashboardService = collectionDashboardService;
    }


    @GetMapping("/monthly-totals/{year}")
    ResponseEntity<List<MonthlyTotals>> getMonthlyTotals(@PathVariable int year){

        try{
            return ResponseEntity.ok(collectionDashboardService.getMonthlyTotals(year));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }


    @GetMapping("/daily-collections/{year}/{month}")
    ResponseEntity<List<DailyCollection>> getDailyCollection(@PathVariable int year, @PathVariable int month){

        try{
            return ResponseEntity.ok(collectionDashboardService.getDailyCollection(year,month));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

//    @GetMapping("/collection-stats/{branch}/{startDate}/{endDate}")
//    ResponseEntity<Object> getCollectionStatsForTimeRange(@PathVariable int branch, @PathVariable String startDate, @PathVariable String endDate){
//        try{
//            return ResponseEntity.ok(collectionDashboardService.getCollectionStatsForTimeRange(branch,startDate,endDate));
//        }catch (Exception exception){
//            exception.printStackTrace();
//            return ResponseEntity.internalServerError().build();
//        }
//
//    }



    @Deprecated
    @GetMapping("/main/stats/{branch}/{startDate}/{endDate}")
    ResponseEntity<MainDashboard> getMainDashboardData(@PathVariable int branch, @PathVariable String startDate, @PathVariable String endDate){

        try{
            return ResponseEntity.ok(collectionDashboardService.getMainDashboardData(branch, startDate, endDate));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("/admin/stats/{status}")
    ResponseEntity<DashboardData> getDashboardData(@PathVariable int status){

        try{
            return ResponseEntity.ok(collectionDashboardService.getDashboardData(status));
        }catch (Exception exception){
            exception.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }
}
