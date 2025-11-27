package ru.hipeoplea.is.lab1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InfomSystemsLab1Application {

    private InfomSystemsLab1Application() {
    }

    public static void main(String[] args) {
        SpringApplication.run(InfomSystemsLab1Application.class, args);
    }

}
