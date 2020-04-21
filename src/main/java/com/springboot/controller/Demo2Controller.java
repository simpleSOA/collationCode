package com.springboot.controller;

import java.io.IOException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Demo2Controller {

  @RequestMapping("dynamicColumn11")
  public void dynamicColumn() throws IOException {
    System.out.println("12314");
  }
}
