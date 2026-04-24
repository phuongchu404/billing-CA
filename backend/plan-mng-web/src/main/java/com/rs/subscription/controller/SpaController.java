package com.rs.subscription.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Forwards all unmatched SPA routes to index.html so Vue Router's HTML5
 * history mode works when the app is served from the Spring Boot jar.
 *
 * The regex patterns deliberately exclude paths with a dot (static assets)
 * so files like /assets/app.js are served directly by Spring's resource handler.
 * API, actuator, and Swagger endpoints are registered with higher priority and
 * will never reach this controller.
 */
@Controller
public class SpaController {

    @RequestMapping(value = {
        "/",
        "/{path:[^\\.]*}",
        "/{path1:[^\\.]*}/{path2:[^\\.]*}",
        "/{path1:[^\\.]*}/{path2:[^\\.]*}/{path3:[^\\.]*}"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
