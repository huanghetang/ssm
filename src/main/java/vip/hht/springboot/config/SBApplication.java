package vip.hht.springboot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;

/**
 * @author zhoumo
 * @datetime 2018/7/4 23:12
 * @desc springboot 启动类必须要有@SpringBootApplication
 */

@SpringBootApplication
@Controller
public class SBApplication {
    @Autowired
    private DataSource dataSource;

    @RequestMapping("hello")
    @ResponseBody
    public String hello(){
        System.out.println("dataSource = " + dataSource);
        return "你好,Spring boot123";
    }
    public static void main(String[] args) {
        SpringApplication.run(SBApplication.class,args);

    }

//    @Override
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
//        //部署到外部tomcat时,tomcat运行的入口
//        return builder.sources(SBApplication.class);
//    }
}
