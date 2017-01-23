package kgi.presentations.k8s.assets_transcoder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.AnnotationConfigEmbeddedWebApplicationContext;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class TranscoderService {

    public static void main(String[] args) {
        startApp(args);
    }
    public static ApplicationContext startApp(String[] args) {

        ApplicationContext appContext = SpringApplication.run(TranscoderService.class, args);
        int port = ((AnnotationConfigEmbeddedWebApplicationContext) appContext).getEmbeddedServletContainer().getPort();
        System.out.println("Assets Transcoder service listening at port: " + port);
        return appContext;
    }
}
