package com.otblabs.jiinueboda.fieldapp.geographic;

import com.otblabs.jiinueboda.fieldapp.geographic.models.County;
import com.otblabs.jiinueboda.fieldapp.geographic.models.SubCounty;
import com.otblabs.jiinueboda.fieldapp.geographic.models.Ward;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;

    public Map<String, Object> getLocationDataOptions() {
        List<County> counties = locationRepository.findAllCounties();
        List<SubCounty> subCounties = locationRepository.findAllSubCounties();
        List<Ward> wards = locationRepository.findAllWards();

        // Build output map
        Map<String, Object> response = new HashMap<>();

        response.put("counties", counties);


        Map<String, List<SubCounty>> subCountyMap = new HashMap<>();

        for (County county : counties) {
            List<SubCounty> names = subCounties.stream()
                    .filter(sc -> sc.getCountyId().equals(county.getCountyId()))
                    .toList();
            subCountyMap.put(county.getName(), names);
        }
        response.put("subCounties", subCountyMap);
        Map<Long, String> subCountyIdToName = subCounties.stream()
                .collect(Collectors.toMap(SubCounty::getSubCountyId, SubCounty::getName));

        Map<String, List<Ward>> wardMap = new HashMap<>();
        for (Ward ward : wards) {
            String subCountyName = subCountyIdToName.get(ward.getSubCountyId());
            wardMap.computeIfAbsent(subCountyName, k -> new ArrayList<>()).add(ward);
        }
        response.put("wards", wardMap);

        return response;
    }
}

