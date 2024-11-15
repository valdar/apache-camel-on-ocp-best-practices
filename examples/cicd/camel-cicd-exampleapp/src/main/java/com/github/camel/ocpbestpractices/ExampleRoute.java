package com.github.camel.ocpbestpractices;

import org.apache.camel.builder.RouteBuilder;

public class ExampleRoute extends RouteBuilder {
  @Override
  public void configure() throws Exception {

      from("timer:java?period=1000")
        .setHeader("example")
          .constant("Java")
        .setBody()
          .simple("Hello World! Camel route written in ${header.example} running on ocp!")
        .to("log:info");
      
  }
}
