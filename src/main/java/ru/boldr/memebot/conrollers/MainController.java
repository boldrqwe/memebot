package ru.boldr.memebot.conrollers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.boldr.memebot.dto.MemeBotResponse;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class MainController {

    @GetMapping(path = "/")
    public String hello() {
        return "hello World";
    }






}
