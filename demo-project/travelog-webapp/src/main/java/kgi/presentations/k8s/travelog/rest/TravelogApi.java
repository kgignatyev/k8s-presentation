package kgi.presentations.k8s.travelog.rest;

import com.fasterxml.jackson.databind.JsonNode;
import kgi.presentations.k8s.travelog.svc.TravelogService;
import kgi.presentations.k8s.travelog.vo.TravelogSearchCriteria;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/travelog/")
public class TravelogApi {

    @Resource
    TravelogService travelogService;

    @RequestMapping(path = "/search",
            method = RequestMethod.POST)
    public List<JsonNode> findTravelogs(TravelogSearchCriteria c) throws IOException {
        return travelogService.find(c);
    }


    @RequestMapping(path = "/{id}",
            method = RequestMethod.GET)
    public JsonNode getTravelog(@PathVariable String id) throws IOException {
        return travelogService.getTravelog(id);
    }

    @RequestMapping(path = "/{id}",
            method = RequestMethod.DELETE)
    public JsonNode deleteTravelog(@PathVariable String id) throws IOException {
        return travelogService.deleteTravelog(id);
    }

    @RequestMapping(path = "/{id}",
            method = RequestMethod.PUT)
    public JsonNode createOrUpdateTravelog(@RequestBody JsonNode travelog,@PathVariable String id) throws IOException {
        return travelogService.createOrUpdateTravelog(id, travelog);
    }


}
