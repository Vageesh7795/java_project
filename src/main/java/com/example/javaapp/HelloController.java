```java
package com.example.javaapp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "Hello from Java Application on OpenShift!";
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Jenkins CI/CD!";
    }
}
```
