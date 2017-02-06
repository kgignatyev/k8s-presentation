package kgi.presentations.k8s.travelog.pages;

import com.fasterxml.jackson.databind.ObjectMapper;
import kgi.presentations.k8s.common.TravelConfigProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Controller
public class IndexPage {


    @Resource
    TravelConfigProperties configProperties;


    @Resource
    ObjectMapper om;


    @GetMapping(path = {"/"})
    String index(Model model) {
        model.addAttribute("initScript", " travelog ={}; " +
                "travelog.in_bucket = '" + configProperties.input_bucket + "';\n" +
                "travelog.output_bucket = '" + configProperties.output_bucket + "';\n" +
                "travelog.region = '" + configProperties.region + "';\n" +
                "travelog.access_key_id = '" + configProperties.access_key_id + "';\n");
        return "index";
    }
}
