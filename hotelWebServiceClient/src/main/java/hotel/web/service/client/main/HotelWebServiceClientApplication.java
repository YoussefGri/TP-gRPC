package hotel.web.service.client.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "hotel.web.service.client.services",
        "hotel.web.service.client.ui"

})
public class HotelWebServiceClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelWebServiceClientApplication.class, args);
    }
}
