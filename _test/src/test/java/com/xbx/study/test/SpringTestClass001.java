package com.xbx.study.test;

import com.xbx.study.common._interface.OssService;
import com.xbx.study.common.constants.ALiYunOssFileType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest(classes = TestApplication.class)
public class SpringTestClass001 {


    @Autowired
    private OssService ossService;


    @Test
    public void test(){

        String filePath = "C:\\Users\\admin\\Pictures\\v2-8495b29fb3fc274fb476aec55992c8fd_1440w.webp";

        try (InputStream is2 = Files.newInputStream(Paths.get(filePath))) {
            String s = ossService.uploadFile("xbx-blog", is2, ALiYunOssFileType.IMAGE);
            System.out.println(s);
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

}
