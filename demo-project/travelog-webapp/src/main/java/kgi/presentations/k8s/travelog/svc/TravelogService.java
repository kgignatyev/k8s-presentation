package kgi.presentations.k8s.travelog.svc;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import kgi.presentations.k8s.travelog.vo.TravelogSearchCriteria;
import org.apache.http.client.fluent.Request;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TravelogService {

    @Resource
    ObjectMapper om;


    public String esBase() {
        String hostFromEnv = System.getenv("TRAVELOG_ES_SERVICE_HOST");
        String res = "http://travelog-es:9200";
        if (hostFromEnv != null) {
            res = "http://" + hostFromEnv + ":" + System.getenv("TRAVELOG_ES_PORT_9200_TCP_PORT");
        }
        return res;
    }


    public List<JsonNode> find(TravelogSearchCriteria c) throws IOException {
        ObjectNode r = om.createObjectNode();
        InputStream responseStream = Request.Post(esBase() + "/travelog/_search").bodyByteArray(om.writeValueAsBytes(r)).execute().returnContent().asStream();
        JsonNode n = om.readTree(responseStream);
        ArrayNode an = (ArrayNode) n.get("hits").get("hits");
        ArrayList<JsonNode> res = new ArrayList();
        for (JsonNode node : an) {
            res.add(node.get("_source"));
        }
        return res;
    }

    public JsonNode getTravelog(String id) throws IOException {
        return om.readTree( Request.Get(esBase()+"/travelog/posts/" +id).execute().returnContent().asStream());
    }

    public JsonNode postTravelog(String id, JsonNode travelog) throws IOException {
        return om.readTree( Request.Put(esBase()+"/travelog/posts/" +id).bodyByteArray(om.writeValueAsBytes(travelog)).execute().returnContent().asStream());
    }
}
