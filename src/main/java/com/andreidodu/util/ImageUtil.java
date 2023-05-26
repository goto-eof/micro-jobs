package com.andreidodu.util;

import com.andreidodu.constants.ApplicationConst;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class ImageUtil {


    public static final String IMAGE_FORMAT_NAME = "png";

    public static byte[] convertBase64StringToBytes(final String base64String) throws UnsupportedEncodingException {
        final String dataSegment = base64String.substring(base64String.indexOf(",") + 1);
        byte[] byteData = dataSegment.getBytes("UTF-8");
        return Base64.getDecoder().decode(byteData);
    }

    public static String calculateFileName(final String seed, final String base64ImageFull, final byte[] imageBytesData) throws NoSuchAlgorithmException, IOException {
        final byte[] signedImageBytesData = createNewArrayWithBytesAtTheEnd(imageBytesData, seed.getBytes());
        final String bytesHashString = calculateBytesHashString(signedImageBytesData);
        return bytesHashString + ".png";
    }

    public static byte[] createNewArrayWithBytesAtTheEnd(final byte[] target, final byte[] bytesToBeAdded) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(target);
        output.write(bytesToBeAdded);
        return output.toByteArray();
    }

    public static String calculateBytesHashString(final byte[] data) throws NoSuchAlgorithmException {
        final byte[] hash = MessageDigest.getInstance("MD5").digest(data);
        return new BigInteger(1, hash).toString(16);
    }

    public static String calculateFileExtension(final String base64ImageString) {
        return base64ImageString.substring("data:image/".length(), base64ImageString.indexOf(";base64"));
    }

    public static void writeImageOnFile(final String fileName, final byte[] data) throws IOException {
        final String fullFilePath = ApplicationConst.FILES_DIRECTORY + "/" + fileName;
        FileOutputStream outputStream = new FileOutputStream(fullFilePath);
        outputStream.write(data);
        outputStream.close();
    }

    public static byte[] resizeImage(byte[] originalImage, int targetWidth) {
        try {
            InputStream is = new ByteArrayInputStream(originalImage);
            BufferedImage newBi = ImageIO.read(is);
            BufferedImage bufferedImage = Scalr.resize(newBi, targetWidth);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, IMAGE_FORMAT_NAME, byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            return originalImage;
        }
    }


    public static ImmutablePair<Integer, Integer> retrieveImageSize(byte[] originalImage) throws IOException {
        InputStream is = new ByteArrayInputStream(originalImage);
        BufferedImage newBi = ImageIO.read(is);
        return new ImmutablePair<>(newBi.getWidth(), newBi.getHeight());
    }
}
