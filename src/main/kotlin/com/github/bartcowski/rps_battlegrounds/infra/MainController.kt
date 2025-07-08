package com.github.bartcowski.rps_battlegrounds.infra

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class MainController {

    @GetMapping("/")
    fun index(): String = "forward:/index.html"
}
