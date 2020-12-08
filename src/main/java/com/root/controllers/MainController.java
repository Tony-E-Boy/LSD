package com.root.controllers;

import com.root.accessingdatamysql.UserRepository;
import com.root.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/")
public class MainController {

    @Autowired // This means to get the bean called userRepository
    // Which is auto-generated by Spring, we will use it to handle the data
    private UserRepository userRepository;

    @GetMapping()
    public String indexPage(){return "index";}

    @GetMapping("/index")
    public String indexPageToo(){return "index";}

    @GetMapping("/login")
    public String loginPage() {return "login";}

    @GetMapping("/registration")
    public String registrationPage(@ModelAttribute("user") User user){

        return "register";
    }

    @GetMapping("/admin")
    public String adminPage(){return "register";}

    @PostMapping(path = "/create") // Map ONLY POST Requests
    public String addNewUser(@ModelAttribute ("user") User user, BindingResult bindingResult) {

        if (bindingResult.hasErrors()){
            return "/register";
        }

        userRepository.save(user);
        return "admin";
    }

    @GetMapping("/add-api")
    public String addApi(){return "add-api";}
}
