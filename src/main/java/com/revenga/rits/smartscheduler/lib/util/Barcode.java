package com.revenga.rits.smartscheduler.lib.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;

import lombok.NonNull;

public class Barcode {

	private Barcode() {
		throw new IllegalStateException(this.getClass().getSimpleName());
	}

	@NonNull
	public static File getImage(BarcodeFormat barcodeFormat, String data, String fileName, int width, int height)
			throws IOException {

		Code128Writer barcodeWriter = new Code128Writer();
		BitMatrix bitMatrix = barcodeWriter.encode(data, barcodeFormat, width, height);

		BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
		File outputFile = new File(fileName);
		
		ImageIO.write(image, "png", new File(fileName));
		
		return outputFile;
	}
}
