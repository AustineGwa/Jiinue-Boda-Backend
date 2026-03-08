package com.otblabs.jiinueboda.groups;


import com.otblabs.jiinueboda.groups.models.Group;
import com.otblabs.jiinueboda.groups.models.NewGroupRequest;
import com.otblabs.jiinueboda.users.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
public class GroupsController {

    private final GroupsService groupsService;
    private final UserService userService;


    public GroupsController(GroupsService groupsService, UserService userService) {
        this.groupsService = groupsService;
        this.userService = userService;
    }

    @GetMapping("/stage-photos/{groupId}")
    ResponseEntity<List<String>> getGroupPhotos(@PathVariable int groupId){
        try{
            return  ResponseEntity.ok(groupsService.getGroupPhotos(groupId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/{groupId}")
    ResponseEntity<Object> getGroupDetails(@PathVariable int groupId){
        try{

            Group group = groupsService.getGroupDetails(groupId);
            List<String> groupPhotos = groupsService.getGroupPhotos(groupId);
            group.setStagePhotos(groupPhotos);
            return  ResponseEntity.ok(group);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    ResponseEntity<Object> getAllGroups(){
        try{
            return  ResponseEntity.ok(groupsService.getAllGroups());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/wards/{wardId}")
    ResponseEntity<Object> getAllGroupsInWard(@PathVariable int wardId){
        try{
            return  ResponseEntity.ok(groupsService.getAllGroupsInWard(wardId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(value="/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Object> createNewGroup(@ModelAttribute NewGroupRequest  group){

        //check if there is a stage photo provided
        if(group.getStagePhotos().isEmpty()){
            return ResponseEntity.unprocessableEntity().body("please add stage photos");
        }

        try{
            return  ResponseEntity.ok(groupsService.createNewGroup(group));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    @GetMapping("/{groupId}/members")
    ResponseEntity<Object> getAllGroupMembers(@PathVariable int groupId){
        try{
            return  ResponseEntity.ok(userService.getUserForGroup(groupId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/partner/{partnerId}")
    ResponseEntity<Object> getAllGroupsForPartner(@PathVariable int partnerId){
        try{
            return  ResponseEntity.ok(groupsService.getAllGroupsForPartner(partnerId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/update-location-data")
    ResponseEntity<Object> updateGroupLocationDetails(@RequestBody GroupLocationUpdate groupLocationUpdate){
        try{
            return  ResponseEntity.ok(groupsService.updateGroupLocationDetails(groupLocationUpdate));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}

