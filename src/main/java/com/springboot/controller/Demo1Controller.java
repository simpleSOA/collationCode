package com.springboot.controller;

import java.io.IOException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Demo1Controller {

  @RequestMapping("dynamicColumn11")
  @ApiVersion(2)
  public void dynamicColumn() throws IOException {
    System.out.println("123");
  }
}
