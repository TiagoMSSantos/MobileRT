package puscas.mobilertapp.server;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@RestController
public class HomeController {
    @RequestMapping("/")
    public String home() {
        return helloWorld();
    }

    private String helloWorld() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, 200, 200);

        int[]data = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        image.setRGB(0, 0, 200, 200, data, 0, 0);

        try {
            ImageIO.write(image, "bmp", byteArrayOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encoded = Base64.getEncoder().encodeToString(byteArray);

        final String canvasHtml = "var canvas = document.getElementById(\"myCanvas\");\n" +
                "var ctx = canvas.getContext(\"2d\");\n" +
                "\n" +
                "var image = new Image();\n" +
                "image.onload = function() {\n" +
                "    ctx.drawImage(image, 0, 0);\n" +
                "};\n" +
                "image.src = \"data:image/  png;base64," + encoded + "\";";

        return "<html>\n" +
                " <head>\n" +
                " </head>\n" +
                " <body>\n" +
                "   <h1>Hello World<h1>\n" +
                "   <canvas id=\"myCanvas\" width=\"200\" height=\"100\" style=\"border:1px solid #000000;\">\n" +
                " <script>\n" +
                canvasHtml +
                "</script>" +
                "</canvas>" +
                " </body>\n" +
                "</html>";
    }
}
