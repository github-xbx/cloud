package com.xbx.study.ai.controller;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.xbx.study.ai.service.ImageService;
import dev.langchain4j.community.model.dashscope.WanxImageStyle;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.output.Response;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("ai/image")
public class ImageModelController {

    private static final Logger logger = LoggerFactory.getLogger(ImageModelController.class);

    @Resource(name = "qwenImageModel")
    private ImageModel qwenImageModel;

    @Resource
    private ImageService imageService;



    @GetMapping("qwen_image_create")
    public String qwenCallIImageCreate() throws IOException {

        Response<Image> response = qwenImageModel.generate("美女");


        logger.info("qwenImageModel result => {}",response.content().url());

        return response.content().url().toString();

    }
    //一位年轻的东亚女性，约20岁，深棕色长波浪卷发，沙漏型身材，纤细的腰，丰满的胸部，修长的双腿，白皙光滑的皮肤。
    //身穿灰色紧身漏脐短袖T恤，搭配同色系包臀超短裙子，腿上穿着黑色丝袜，配着高跟鞋。
    //全身照片，从上道下整个人站在中间，面对镜头。
    //电影级质感画质，8K超高分辨率，超写实真人摄影风格，细腻纹理，柔和的中性光，减少强烈的阴影感，使皮肤看起来更柔和。
    @GetMapping("qwen_image_create_1")
    public String qwenCallIImageCreate1() {
        String prompt = "近景镜头，18岁的中国女孩，古代服饰，圆脸，正面看着镜头"
                + "民族优雅的服饰，商业摄影，室外，电影级光照，半身特写，精致的淡妆，锐利的边缘。";
        ImageSynthesisParam param = ImageSynthesisParam.builder()
                .apiKey(System.getenv("java_qwen_apikey"))
                .model("wanx2.0-t2i-turbo")
                .prompt(prompt)
                .style(WanxImageStyle.CARTOON_3D.getStyle())
                .n(2)
                //.size("1920*1080")
                .build();
        ImageSynthesis imageSynthesis = new ImageSynthesis();
        ImageSynthesisResult result = null;

        try {
            System.out.println("--- sync call, please wait a moment ---");
            result = imageSynthesis.call(param);
        } catch (NoApiKeyException e) {
            throw new RuntimeException(e);
        }
        return  result.toString();
    }


    @GetMapping("createImage")
    public ResponseEntity<org.springframework.core.io.Resource> createImage(@RequestParam("message") String message) throws Exception {

        org.springframework.core.io.Resource image = imageService.createOneImage(message);

        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
    }

    @GetMapping("create")
    public String create(@RequestParam("message") String message) throws Exception {
        if (StringUtils.isEmpty(message)){
            message = """
                一位年轻的东亚女性，约20岁，深棕色长波浪卷发，沙漏型身材，纤细的腰，丰满的胸部，修长的双腿，白皙光滑的皮肤。
                身穿紧身漏胸短袖T恤，搭配包臀超短裙子，腿上穿着肉色丝袜，配着高跟鞋。
                全身照片，从上道下整个人站在中间，面对镜头，做一些简单拍照pose动作。
                电影级质感画质，8K超高分辨率，超写实真人摄影风格，细腻纹理，柔和的中性光，减少强烈的阴影感，使皮肤看起来更柔和。
                """;
        }
        org.springframework.core.io.Resource image = imageService.createOneImage(message);

        return "success";
    }


}
