package cl.duoc.dsy1103.lunari.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {


    @GetMapping("/")
    public String redirectToSwaggerUI() {
        return "redirect:/swagger-ui";
    }

}
