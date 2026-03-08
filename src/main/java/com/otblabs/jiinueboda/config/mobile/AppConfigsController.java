package com.otblabs.jiinueboda.config.mobile;

import com.otblabs.jiinueboda.config.mobile.models.AppConfig;
import com.otblabs.jiinueboda.exceptions.ExceptionsHandlerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app/config")
public class AppConfigsController {

    private final AppConfigService appConfigService;
    private final ExceptionsHandlerService exceptionsHandlerService;


    public AppConfigsController(AppConfigService appConfigService, ExceptionsHandlerService exceptionsHandlerService) {
        this.appConfigService = appConfigService;
        this.exceptionsHandlerService = exceptionsHandlerService;
    }

    @GetMapping("/android/{appID}")
    ResponseEntity<Object> getAppConfig(@PathVariable int appID){

        try {
            AppConfig appConfig = appConfigService.getAppConfig(appID);
            return ResponseEntity.ok(appConfig);
        } catch (Exception e) {
            exceptionsHandlerService.saveExceptionToDb(e.getMessage(),appID);
            return ResponseEntity.internalServerError().build();

        }
    }

}
