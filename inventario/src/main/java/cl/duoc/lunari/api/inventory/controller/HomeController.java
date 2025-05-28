package cl.duoc.lunari.api.inventory.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String redirectToSwaggerUI() {
        return "redirect:/swagger-ui";
    }
}
